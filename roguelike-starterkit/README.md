# Roguelike Starter-Kit for Indigo

A library for use with [Indigo](https://indigoengine.io/) to provide some terminal-like rendering functionality specifically for ASCII art style games, and roguelike games in particular.

An early version of this code was used to build the [follow along examples](https://github.com/davesmith00000/roguelike-tutorial) to the [Roguelike tutorials](http://rogueliketutorials.com/). I'm still using this library in my revised [Roguelike demo game](https://github.com/davesmith00000/roguelike).

> Please note that follow along tutorials will be upgraded to use this library at some point.

![Roguelike ascii art in Indigo](/roguelike.gif "Roguelike ascii art in Indigo")

## Installation

Add the following dependency to your Indigo based game project (alongside the usual indigo ones):

`"io.indigoengine" %%% "roguelike-starterkit" % "0.3.0"`

Everything can be accessed via this import:

```scala
import io.indigoengine.roguelike.starterkit.*
```

## Running the demo

Download the repo, `cd` into the demo directory and (assuming you have electron installed), run `sbt runGame`.

You can hit the space bar to switch between the rendering modes.

Please note that the `TerminalText` scene has two examples on it, first the standard component and then a second component that uses a custom shader.

## What this library is

If you want to build a roguelike game, then you need to be able to render something that looks like a terminal with ASCII art.

In Python, people use a library called [tcod](https://python-tcod.readthedocs.io/en/latest/). The roguelike-starterkit is not a full implementation of tcod, but it provides some of the key functionality you will need in a purely functional form.

## What roguelikes are

[Roguelike](https://en.wikipedia.org/wiki/Roguelike)'s are a type of game that get their name because they are ...wait for it ..._like_ an 80s game called _Rogue_!

They typically use ASCII art for graphics, generated levels / dungeons and feature things like perma-death.

## Indigo vs Roguelike's

A few people have asked about using Indigo for Roguelike game building over the years, and it has come up again in response to the annual [r/roguelikedev - RoguelikeDev Does The Complete Roguelike Tutorial](https://www.reddit.com/r/roguelikedev/comments/o5x585/roguelikedev_does_the_complete_roguelike_tutorial/).

There are some specific challenges with regards to Indigo rendering the seemingly straightforward graphics of a roguelike that I won't go into here, but I've generally had to caution people that Indigo _might not be good at this..._

This library is an attempt to open up roguelikes to Indigo game builders by going some way towards solving some of the rendering issues and providing out-of-the-box support for a standard roguelike artwork format, namely the Dwarf Fortress tile sets.

## Finding artwork

One of the great things about roguelikes is that they're usually ASCII art, and there is a wealth of available art "packs" that were created for the well known roguelike, [Dwarf Fortress](https://en.wikipedia.org/wiki/Dwarf_Fortress).

**This is excellent news for programmers!**

You can go ahead and build a game and it will look ...exactly like all the other ones! The quality of your game will be judged on the strength of your ability to code up a world, not on your ability to draw trees and people. Perfect!

This starter pack takes a Dwarf Fortress image that looks like this:

![A dwarf fortress tile map](demo/assets/Anikki_square_10x10.png "A dwarf fortress tile map")

([There are lots more of them to choose from!](https://dwarffortresswiki.org/Tileset_repository))

The project then uses custom shaders that allow you to set the foreground and background colours to render your world based on any of the standard format tile sheets you can find / make.

> It appears the the only graphical requirements are that you can set the foreground and background colors. If this isn't true please raise any issue!

## Rendering ASCII Art

Rendering ASCII art means setting three things:

1. The character
1. The foreground color (implemented as a tint to retain shading)
1. The background color (which overrides a "mask" color with another color value - the mask color is magenta by default)

This library provides three mechanisms to do that:

### `TerminalText`

This is a material for use with Indigo's standard `Text` primitive.

Looks great but has two problems:

1. Changing colors mid-artwork (e.g. setting the text red and the border blue) is a pain, you need to use another `Text` instance and make them line up!.
2. This process allocates a lot during the rendering process, and probably won't scale very well.

Great for pop-up menus, and monochrome sections of ASCII art, or maps that aren't too big. After that, a new strategy may be needed.

[Example](https://github.com/PurpleKingdomGames/roguelike-starterkit/blob/main/demo/src/main/scala/demo/TerminalTextScene.scala)

### `TerminalEmulator` with `TerminalEntity`

This is the most flexible way to render, but not the fastest.

The `TerminalEmulator` in conjunction with the `TerminalEntity` works in a completely different way. Here a special shader is going to draw all the ASCII characters out in a continuous image, and you can interleave colors any time you like with no performance cost. This moves processing costs away from the rendering pipeline but incurs a penalty on the CPU side.

The terminal emulator... emulates a simple terminal interface allowing you to put and get characters. Terminals can be merged and drawn.

The trade off here is that it's more powerful but less friendly. You just give it a list of tiles to draw and it will lay them out in the grid specified.

A word on the performance of this solution, by default, this version is configured to render up to a maximum of 4096 tiles and _just_ manages to run at 60fps (for me), but with no business logic. 4000 tiles is an 80x50 which is one of the standard roguelike game grid sizes. However, performance will varying from platform to platform and browser to browser. The performance problem here is a that your allocating a couple of massive arrays every frame, and the GC has to keep up.

Three ways to improve performance / reduce GC pressure:

1. Lower your FPS! Do you need 60 fps for an ASCII game? Probably not! 30 fps would likely be fine. As you lower FPS what you get (aside from less frequent graphics updates) is input lag. So another way to go is to artificially lower fps: Leave your game running at 60fps, but put in a throttle that only redraws the view every 15-30 fps based on time since last draw.
2. Only call `draw`, when the map changes, which is on key stroke not on every frame. This will reduce the number of times the massive arrays are produced and discarded. Cache the drawn `TerminalEntity` in the view model.
3. Lower the max array size.

[Example](https://github.com/PurpleKingdomGames/roguelike-starterkit/blob/main/demo/src/main/scala/demo/TerminalEmulatorScene.scala)

### `TerminalEmulator` with `CloneTiles`

Another way to use the `TerminalEmulator` is to have is output a `CloneTiles`.

This is as capable as the previous method, but does not have the 4096 tile limit. Performance will vary by scene complexity, specifically how many unique colour combinations you have in place. Please note that this method allows much more flexible rendering, but will not work well with primitives of difference sizes as it is designed to render a grid.

[Example](https://github.com/PurpleKingdomGames/roguelike-starterkit/blob/main/demo/src/main/scala/demo/CloneTilesScene.scala)

## Extras

The starter kit also provides:

1. An implementation of Bresenham's Line algorithm, used in the tutorial for line of sight across a grid.
2. A very, very simplistic path finding algorithm (just enough for the roguelike-tutorials).
