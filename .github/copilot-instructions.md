# Copilot Instructions for Indigo Engine Monorepo

## Repository Overview

This is a **Mill-based monorepo** containing Scala libraries and frameworks by Purple Kingdom Games:

- **Indigo**: Functional game engine built on Scala.js + WebGL (300+ files)
- **Tyrian**: Elm-inspired Scala UI library for Scala 3 (100+ files)
- **Ultraviolet**: Scala 3 to GLSL transpiler (50+ files)
- **Roguelike Starter Kit**: ASCII art roguelike rendering for Indigo (50+ files)

**Languages**: Scala 3.8.0, targeting both JVM and JavaScript (via Scala.js 1.20.2)  
**Build Tool**: Mill 1.1.0-RC4 (launcher script: `./mill` on Unix, `.\mill.bat` on Windows)  
**Testing**: MUnit framework  
**Java Version**: Java 11+ required (CI uses Java 11; Java 17 also works)

## Critical Build Information

### Resource-Intensive Operations

**⚠️ ALWAYS limit concurrency for Scala.js linking operations** - full parallelism exhausts system resources:

```bash
# CORRECT - limit concurrency to 2 jobs
./mill -j2 __.fastLinkJS
./mill -j2 __.fastLinkJSTest
./mill -j2 __.fix

# INCORRECT - will likely hang or fail
./mill __.fastLinkJS
```

For other operations, full parallelism is fine:
```bash
./mill __.compile     # Full parallelism OK
./mill __.test        # Full parallelism OK
```

### Essential Build Commands

**Bootstrap**: No special bootstrap needed. Mill launcher downloads Mill automatically on first run.

**Full Build Sequence** (validated by `build.sh`):
```bash
# 1. Compile all modules (2-5 minutes)
./mill --no-server __.compile

# 2. Format code
./mill --no-server __.reformat

# 3. Run scalafix (limited concurrency - 3-5 minutes)
./mill --no-server -j2 __.fix

# 4. Link Scala.js (limited concurrency - 5-10 minutes)
./mill --no-server -j2 __.fastLinkJS
./mill --no-server -j2 __.fastLinkJSTest

# 5. Run tests (5-15 minutes)
./mill --no-server __.test

# 6. Publish locally
./mill --no-server __.publishLocal
```

**Quick validation script**: `bash build.sh` runs the full sequence above.

**CI Validation** (what GitHub Actions runs):
```bash
bash ci.sh
```

This runs the same steps as `build.sh` but with additional flags (`clean`, `--disable-ticker`, `checkFormat` instead of `reformat`, `fix --check` instead of `fix`).

### Individual Commands

**Compile**: `./mill __.compile` (compiles all modules)  
**Test All**: `./mill __.test` (runs all tests)  
**Test Single Module**: `./mill indigo.js.test` or `./mill ultraviolet.jvm.test`  
**Test Specific Class**: `./mill indigo-core.js.test.testOnly indigo.core.SomeTests`  
**Format Code**: `./mill __.reformat` (applies formatting)  
**Check Format**: `./mill __.checkFormat` (validates formatting without changes)  
**Run Scalafix**: `./mill -j2 __.fix` (applies scalafix rules with limited concurrency)  
**Check Scalafix**: `./mill -j2 __.fix --check` (validates scalafix without changes)

### --no-server Flag

The `--no-server` flag is recommended for stability. If you encounter `NoClassDefFoundError` in Scala.js testing classes (e.g., `Serializer$SelectorSerializer$`), this is a Mill server communication issue. Running with `--no-server` avoids it.

### Windows Considerations

On Windows, use `.\mill.bat` instead of `./mill` for all commands. The PowerShell build script `build.ps1` uses `-j1` (single-threaded) concurrency for all operations for maximum stability.

## Code Style and Quality

### Formatting and Linting

- **Scalafmt**: Version 3.8.5, configured in `.scalafmt.conf`
  - Dialect: `scala3`
  - Max column: 120
  - Always run before committing: `./mill __.reformat`

- **Scalafix**: Configured in `.scalafix.conf`
  - Rules: `DisableSyntax`, `OrganizeImports`
  - **Enforces functional style**: No vars, throws, nulls, returns, while loops, XML, default args
  - Always run before committing: `./mill -j2 __.fix`

### Strict Compilation

- **Scala 3.8.0** with strict equality enabled
- Compiler treats warnings as errors (`-Werror`)
- Uses typelevel scalacoptions for consistent, strict compilation flags

## Project Architecture

### Module Structure

Most modules have `js` and optionally `jvm` sub-modules (e.g., `ultraviolet.js`, `ultraviolet.jvm`). Module definitions are in `package.mill` files within each module directory.

### Module Dependency Chain

**Indigo Stack**:
```
indigoengine-shared (base)
  ↓
indigo-core
  ↓
ultraviolet (jvm + js) ──────────┐
  ↓                              │
indigo-shaders                   │
  ↓                              │
indigo-scenegraph                │
  ↓                              │
indigo-physics                   │
  ↓                              │
indigo ←─────────────────────────┘
  ↓
indigo-extras
  ↓
roguelike-starterkit
```

**Tyrian Stack**:
```
indigoengine-shared
  ↓
tyrian-tags
  ↓
tyrian (js only)
  ↓
tyrian-io, tyrian-zio, tyrian-htmx
```

### Key Directories

- **`build.mill`**: Root build configuration with shared traits (`SharedJS`, `SharedJVM`, `SharedPublish`)
- **`mill-build/build.mill`**: Mill meta-build configuration
- **`indigo/`, `tyrian/`, `ultraviolet/`**: Core library modules
- **`indigo-sandboxes/`, `tyrian-sandboxes/`**: Testing and development playgrounds
- **`indigo-plugin/`, `mill-indigo/`, `sbt-indigo/`**: Build plugins for game projects
- **`.indigo-version`**: Version file (e.g., `0.22.1-SNAPSHOT`)
- **`.mill-version`**: Mill version (e.g., `1.1.0-RC4`)

### Configuration Files

- **`.scalafmt.conf`**: Scalafmt configuration (dialect: scala3, max column: 120)
- **`.scalafix.conf`**: Scalafix rules (DisableSyntax, OrganizeImports)
- **`.gitignore`**: Excludes `out/` (Mill output), `target/`, `node_modules/`, `.metals/`, etc.

## Testing

**Test Framework**: MUnit  
**Test Location**: Each module has a `test/` subdirectory (e.g., `indigo/test/`, `ultraviolet/test/`)

**Run All Tests**:
```bash
./mill __.test
```

**Run Single Module Tests**:
```bash
./mill indigo.js.test
./mill ultraviolet.jvm.test
```

**Run Specific Test Class**:
```bash
./mill indigo-core.js.test.testOnly indigo.core.SomeTests
```

**Run Tests Matching Pattern**:
```bash
./mill indigo.js.test.testOnly '*AnimationTests*'
```

## CI and GitHub Actions

**CI Workflow**: `.github/workflows/ci.yml`  
**Trigger**: Pushes to `main` and pull requests to `main`  
**Environment**: Ubuntu latest, Java 11 (adopt@1.11)  
**JAVA_OPTS**: `-Xmx4G -Xss8m -XX:+UseG1GC`

**CI Command**: `bash ci.sh`

CI runs the same steps as the build script but with validation flags:
- `clean` before building
- `checkFormat` instead of `reformat`
- `fix --check` instead of `fix`
- `--disable-ticker` for cleaner CI output

## Entry Points and Game Development

For game development with Indigo, use these entry points:

- **`IndigoGame`**: Full game with scenes support
- **`IndigoDemo`**: Simpler game without scenes
- **`IndigoSandbox`**: Quick prototyping
- **`IndigoShader`**: Shader development/testing

## Common Pitfalls and Solutions

1. **Scala.js linking hangs or fails**: Use `-j2` to limit concurrency (`./mill -j2 __.fastLinkJS`)
2. **NoClassDefFoundError in tests**: Run with `--no-server` flag
3. **Build inconsistencies**: Run `./mill clean` and rebuild
4. **Format/fix failures**: Run `./mill __.reformat` then `./mill -j2 __.fix` before testing
5. **Windows build issues**: Use `.\mill.bat` and consider `-j1` concurrency

## Publishing and Versioning

- **Version**: Controlled by `.indigo-version` file in root
- **Organization**: `io.indigoengine`
- **Local Publishing**: `./mill __.publishLocal`
- **Remote Publishing**: `./mill __.publishSonatypeCentral` (requires credentials)

## Additional Tools and Dependencies

**Optional JS Tools**: Node.js, Yarn, Electron (for demo/sandbox projects)  
**Nix Support**: `flake.nix` provides a reproducible dev environment with JDK 17, Mill, Node.js, Yarn, Electron

## Quick Reference

**Before Committing**:
1. `./mill __.compile` - Ensure it compiles
2. `./mill __.reformat` - Format code
3. `./mill -j2 __.fix` - Apply scalafix
4. `./mill -j2 __.fastLinkJS` - Link Scala.js (if changed JS code)
5. `./mill __.test` - Run tests
6. `./mill __.checkFormat` - Verify formatting
7. `./mill -j2 __.fix --check` - Verify scalafix

**Or simply run**: `bash build.sh` to validate everything.

**To Replicate CI**: `bash ci.sh`

---

**Trust these instructions**. Only search for additional information if these instructions are incomplete or found to be incorrect.
