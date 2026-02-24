# Scala Native SDL3 OpenGL — Hello Triangle

A minimal "hello triangle" demo using **Scala Native**, **SDL3**, and **OpenGL** (legacy immediate mode). Renders an RGB-colored triangle in a 400x400 window.

## Prerequisites

- **JDK 17+**
- **Mill** (via included `mill.bat` / `mill` launcher)
- **LLVM/Clang** — required by Scala Native (e.g. `scoop install llvm`)
- **SDL3 development libraries** — e.g. `scoop install sdl3` (Windows) or `brew install sdl3` (macOS)

## Build & Run

```bash
mill run
```

This compiles Scala to native code via LLVM and links against SDL3 and OpenGL.

To compile without running:

```bash
mill nativeLink
```

## What It Does

Opens a 400x400 window and draws a triangle with red, green, and blue corners using OpenGL immediate mode. Close the window or press the X button to quit.

## Project Structure

```
build.mill                         # Mill build config with Scala Native
src/main/scala/
  sdl/SDL.scala                    # Minimal SDL3 C bindings
  gl/GL.scala                      # Minimal OpenGL C bindings
  Main.scala                       # Application entry point
```
