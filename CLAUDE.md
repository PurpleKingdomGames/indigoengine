# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a monorepo containing Scala libraries and frameworks by Purple Kingdom Games:
- **Indigo**: A functional game engine built on Scala.js + WebGL
- **Tyrian**: An Elm-inspired Scala UI library for Scala 3
- **Ultraviolet**: A Scala 3 to GLSL transpiler library
- **Roguelike Starter Kit**: ASCII art roguelike game rendering for Indigo

## Build System

This is a **Mill** build. The `./mill` script in the root is the Mill launcher (version in `.mill-version`).

**Windows:** Use `.\mill.bat` instead of `./mill` for all commands.

**Windows (Claude Code):** Run Mill via PowerShell: `powershell.exe -Command "& { .\mill.bat <target> }"`

### ⚠️ Target Specific Modules - Avoid Wasting Time

**IMPORTANT:** This is a large monorepo with many modules. **Always target compilation and builds to the specific modules you're working on** rather than using general commands like `__.compile`.

```bash
# ✅ GOOD - Target specific modules
./mill indigo-core.js.compile
./mill ultraviolet.jvm.compile
./mill tyrian.js.compile

# ❌ AVOID - Compiles ALL modules (slow and wasteful)
./mill __.compile

# ✅ GOOD - Test specific modules
./mill indigo-core.js.test
./mill ultraviolet.jvm.test

# ❌ AVOID - Tests ALL modules
./mill __.test
```

**When to use general builds:**
- Only use `__.compile`, `__.test`, `__.reformat`, etc. when you need to validate the entire repository (e.g., before committing, for CI, or when uncertain about impact)
- For day-to-day development, always target specific modules

### Essential Commands

```bash
# Compile all modules
./mill __.compile

# Run all tests
./mill __.test

# Run a single module's tests
./mill indigo.js.test
./mill ultraviolet.jvm.test

# Format code
./mill __.reformat

# Check formatting
./mill __.checkFormat

# Run scalafix
./mill __.fix

# Scala.js linking (use -j2 to limit concurrency - full parallelism may exhaust resources)
./mill -j2 __.fastLinkJS

# Publish locally
./mill __.publishLocal

# Full build script
bash build.sh
```

### Important Note on Scala.js Linking

Running Scala.js linking on all modules simultaneously is resource-intensive. Always use limited concurrency:
```bash
./mill -j2 __.fastLinkJS    # Good
./mill __.fastLinkJS        # May exhaust resources
```

### Troubleshooting

If tests fail with `NoClassDefFoundError` in Scala.js testing classes (e.g., `Serializer$SelectorSerializer$`), try running with `--no-server` flag. This is a Mill server communication issue that may be fixed in versions newer than 1.1.0-RC2.

## Code Style

- **Scala 3** with strict equality enabled (version defined in `build.mill`)
- **Scalafmt** with `runner.dialect = scala3` (config in `.scalafmt.conf`)
- **Scalafix** rules: DisableSyntax, OrganizeImports (config in `.scalafix.conf`)
- Scalafix enforces: no vars, throws, nulls, returns, while loops, XML, default args

## Architecture

### Module Dependency Graph

```
indigoengine-shared (base module)
    ↓
indigo-core
    ↓
ultraviolet (jvm & js)
    ↓
indigo-shaders
    ↓
indigo-scenegraph
    ↓
├── indigo-physics
│       ↓
│   indigo-platform-api (platform abstraction)
│       ↓
│   indigo-js-platform (JS/browser implementation)
│       ↓
└──→ indigo ←───────────────────────────────────
    ↓
indigo-extras
    ↓
roguelike-starterkit
```

### Platform Abstraction

The engine uses a platform abstraction layer to separate platform-agnostic code from platform-specific implementations:

- **indigo-platform-api**: Defines the `Platform` trait and related types (renderer API, display objects, asset mapping). This module is platform-agnostic.
- **indigo-js-platform**: The JavaScript/browser implementation using Scala.js, WebGL, and DOM APIs. Implements `JsPlatform` which provides animation frames, storage, audio, networking, and WebGL rendering.

This architecture enables future support for alternative platforms (native, server-side, etc.) by implementing the `Platform` trait for different runtimes.

Tyrian has a separate dependency chain:
```
indigoengine-shared
    ↓
tyrian-tags
    ↓
tyrian (js only)
    ↓
tyrian-io, tyrian-zio, tyrian-htmx
```

### Key Patterns

- Most modules have `js` and optionally `jvm` sub-modules (e.g., `ultraviolet.js`, `ultraviolet.jvm`)
- Shared build traits in `build.mill`: `SharedJS`, `SharedJVM`, `SharedPublish`
- Module definitions in `package.mill` files within each module directory
- Version controlled via `.indigo-version` file in root

### Entry Points for Game Development

- `IndigoGame` - Full game with scenes support
- `IndigoDemo` - Simpler game without scenes
- `IndigoSandbox` - Quick prototyping
- `IndigoShader` - Shader development/testing

### Sandbox Projects

Located in `indigo-sandboxes/` and `tyrian-sandboxes/` for testing and development.

## Testing

Uses MUnit test framework:
```bash
# Run specific test class
./mill indigo-core.js.test.testOnly indigo.core.SomeTests

# Run tests matching pattern
./mill indigo.js.test.testOnly '*AnimationTests*'
```

## Publishing

Version is read from `.indigo-version` file. Organization is `io.indigoengine`.
