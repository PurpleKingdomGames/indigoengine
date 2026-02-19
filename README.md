[![MIT License](https://img.shields.io/github/license/PurpleKingdomGames/indigoengine?color=indigo)](https://github.com/tterb/atomic-design-ui/blob/master/LICENSEs)
[![Latest Tagged Release](https://img.shields.io/badge/dynamic/json?color=purple&label=latest%20release&query=%24%5B0%5D.name&url=https%3A%2F%2Fapi.github.com%2Frepos%2FPurpleKingdomGames%2Findigoengine%2Ftags)](https://github.com/PurpleKingdomGames/indigoengine/releases)
[![Discord Chat](https://img.shields.io/discord/716435281208672356?color=blue&label=discord)](https://discord.gg/3E2r9FwwHu)
[![CI](https://github.com/PurpleKingdomGames/indigoengine/actions/workflows/ci.yml/badge.svg)](https://github.com/PurpleKingdomGames/indigoengine/actions/workflows/ci.yml)

# Projects by Purple Kingdom Games

This repository contains the source code of all the officially supported Scala libraries and frameworks supported by Purple Kingdom Games, which includes:

### Indigo

Indigo is a game engine written in Scala for functional programmers. It allows game developers to build games using a set of purely functional APIs that are focused on productivity and testing.

Indigo is built entirely on Scala.js + WebGL, but it's sbt and Mill plugins will export games for web, desktop (via Electron), and mobile (via Cordova).

Documentation can be found at: [indigoengine.io](https://indigoengine.io).

### Tyrian

An Elm-inspired Scala UI library for Scala 3.

The main documentation site, complete with installation instructions, is available here:
[tyrian.indigoengine.io/](https://tyrian.indigoengine.io/)

### Ultraviolet

Ultraviolet is a Scala 3 to GLSL (versions 100 and 300) transpiler library built on top of Scala 3 inline macros.

Please visit the official [documentation site](https://ultraviolet.indigoengine.io/) for more details.

### Roguelike Starter Kit for Indigo

A starter project for Indigo to provide rendering functionality specifically for ASCII art style roguelike games.

Documentation for the roguelike starterkit can be found at: [rlsk.indigoengine.io](https://rlsk.indigoengine.io).

## Thank you, to our sponsors! 💜

Thank you to all our wonderful sponsors, and particularly to [dedipresta](https://www.dedipresta.com/) for their generous support.

![dedipresta](static/sponsors/dedipresta.png)

If you'd like to help advance our work, we are ever grateful for all forms of contribution, either by volunteering time or [financial backing](https://github.com/sponsors/PurpleKingdomGames?o=esb). 

### Module Dependency Diagram

![Module dependency diagram](diagrams/indigoengine.png)

## Developers & Contributors

This is a predominately [Mill](https://mill-build.org/) based monorepo.

Aside from your usual Scala set up, the tools you may need are JS tools: Node.js, Yarn, and potentially Electron. There is a Nix flake provided, if you like that sort of thing.

You are highly encouraged to look at the very simple `build.sh` script, which you can run with `bash build.sh`.

***The important thing to note*** is that running Scala.js linking on all of the modules at once is a _very_ heavy operation that may grind your build to a halt.

So instead of doing `./mill __.fastLinkJS`, you should do something like `./mill -j2 __.fastLinkJS` to limit the concurrency. Other operations like compiling can by run at full parallelism, i.e. `./mill __.compile`.
