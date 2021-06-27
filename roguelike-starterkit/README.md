# Roguelike Starter Kit for Indigo

A starter project for Indigo to provide some rendering functionality specifically for ASCII art style roguelike games.

![Roguelike ascii art in Indigo](/roguelike.gif "Roguelike ascii art in Indigo")

## What are roguelikes?

[Roguelike](https://en.wikipedia.org/wiki/Roguelike)'s are a type of game that get their name because they are ...wait for it ..._like_ an 80s game called _Rogue_!

They typically use ASCII art for graphics, generated levels / dungeons and feature things like perma-death.

## Indigo vs Roguelike's

A few people have asked about using Indigo for Roguelike game building over the years, and it has come up again in response to the annual [r/roguelikedev - RoguelikeDev Does The Complete Roguelike Tutorial](https://www.reddit.com/r/roguelikedev/comments/o5x585/roguelikedev_does_the_complete_roguelike_tutorial/).

There are some specific challenges with regards to Indigo rendering the seemingly straightforward graphics of a roguelike that I won't go into now, but I've generally had to caution people that Indigo _might not be good at this..._

This base project is an attempt to open up roguelikes to Indigo game builders by going some way towards solving some of the rendering issues and providing out-of-the-box support for a standard artwork format, from Dwarf Fortress.

## Finding artwork

One of the great things about roguelikes is that they're usually ASCII art, and there is a wealth of available art "packs" that were created for the well known roguelike, [Dwarf Fortress](https://en.wikipedia.org/wiki/Dwarf_Fortress).

**This is excellent news for programmers!**

You can go ahead and build a game and it will look ...exactly like all the other ones! The quality of your game will be judged on the strength of your ability to code up a world, not on your ability to draw trees and people. Perfect!

This starter pack takes a Dwarf Fortress image that looks like this:

![A dwarf fortress tile map](/assets/Anikki_square_10x10.png "A dwarf fortress tile map")

([There are lots more of them to choose from!](https://dwarffortresswiki.org/Tileset_repository))

The project then uses custom shaders that allow you to set the foreground and background colours to render your world based on any of the standard format tile sheets you can find / make.

> It appears the the only graphical requirements are that you can set the foreground and background colors. If this isn't true please raise any issue!

An important aspect of this is the compile time generated source code, here is the bit you need to care about from the `build.sbt` file:

```scala
.settings(
  Compile / sourceGenerators += Def.task {
    TileCharGen
      .gen(
        "DfTiles", // Class/module name.
        "roguelike", // fully qualified package name
        (Compile / sourceManaged).value, // Managed sources (output) directory for the generated classes
        10, // Character width, depends on which tile sheet you are using!
        10 // Character height, depends on which tile sheet you are using!
      )
  }.taskValue
)
```

If you look inside `TileCharGen` lower down you'll see something like:

```scala
object CharMap {

  val chars = List(
// CharDetail(0,0x00,'\x0',"NULL"),
    CharDetail(1, 0x263a, '☺', "WHITE_SMILING_FACE"),
    CharDetail(2, 0x263b, '☻', "BLACK_SMILING_FACE"),
    CharDetail(3, 0x2665, '♥', "BLACK_HEART_SUIT"),
    CharDetail(4, 0x2666, '♦', "BLACK_DIAMOND_SUIT"),
    CharDetail(5, 0x2663, '♣', "BLACK_CLUB_SUIT"),
    CharDetail(6, 0x2660, '♠', "BLACK_SPADE_SUIT"),
```

These are the values used to generate the class information and correspond to the standard dwarf fortress layout (which itself is the "IBM Code Page 437" or "Extended ASCII" table). If you want to change a symbol, or the name used to reference a symbol, this is the place to do it. Next time you compile (may need to reload sbt) the new values will be present. Note that the effect of changes this values varies depending on which rendering approach you're using. Probably best to leave them alone initially.

## A Tale of Two ASCII Rendering Approaches

Rendering ASCII art means setting three things:

1. The character
1. The foreground color (implemented as a tint to retain shading)
1. The background color (which overrides a "mask" color with another color value - the mask color is magenta by default)

There are two ways to render ASCII art with this project. Both have upsides and downsides, and are demonstrated in the project repo, one in each of the two scenes.

**NOTE: You can use both together!**

> Neither has been tested in anger! Please report your experience!

### Method 1: `Text`

This uses the standard `Text` primitive with a custom shader, it's really convenient, and allows you to do this:

```scala
  def message: String =
    """
    |╔═════════════════════╗
    |║ Hit Space to Start! ║
    |╚═════════════════════╝
    |""".stripMargin

  def present(context: FrameContext[Unit], model: Unit, viewModel: Unit): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Text(message, DfTiles.Fonts.fontKey, TerminalText(Assets.tileMap, RGB.Cyan, RGBA.Blue))
      )
    )
```

Looks great! ...but has two problems:

1. Changing colors mid-artwork (e.g. setting the text red and the border blue) is a pain, you need to use another `Text` instance and make them line up!.
2. This process allocates a lot during the rendering process, and probably won't scale very well.

Great for pop-up menus, and monochrome sections of ASCII art, or maps that aren't too big. After that, a new strategy may be needed.

### Method 2: `TerminalEmulator`

The `TerminalEmulator` works in a completely different way. Here a special shader is going to draw all the ASCII characters out in a continuous image, and you can interleave colors any time you like with no performance cost. This moves processing costs away from the rendering pipeline but incurs a penalty on the CPU side.

The terminal emulator... emulates a simple terminal interface allowing you to put and get characters. Terminals can be merged and drawn.

It's used like this:

```scala
  val terminal: TerminalEmulator =
    TerminalEmulator(Size(3, 3))
      .put(
        Point(0, 0) -> MapTile(DfTiles.Tile.`░`, RGB.Cyan, RGBA.Blue),
        Point(1, 0) -> MapTile(DfTiles.Tile.`░`, RGB.Cyan, RGBA.Blue),
        Point(2, 0) -> MapTile(DfTiles.Tile.`░`, RGB.Cyan, RGBA.Blue),
        Point(0, 1) -> MapTile(DfTiles.Tile.`░`, RGB.Cyan, RGBA.Blue),
        Point(1, 1) -> MapTile(DfTiles.Tile.`@`, RGB.Magenta),
        Point(2, 1) -> MapTile(DfTiles.Tile.`░`, RGB.Cyan, RGBA.Blue),
        Point(0, 2) -> MapTile(DfTiles.Tile.`░`, RGB.Cyan, RGBA.Blue),
        Point(1, 2) -> MapTile(DfTiles.Tile.`░`, RGB.Cyan, RGBA.Blue),
        Point(2, 2) -> MapTile(DfTiles.Tile.`░`, RGB.Cyan, RGBA.Blue)
      )

  def present(context: FrameContext[Unit], model: Unit, viewModel: Unit): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        terminal.draw(Assets.tileMap, Size(10, 10), MapTile(DfTiles.Tile.SPACE))
      )
    )
```

The trade off here is that it's more powerful but less friendly. You just give it a list of tiles to draw and it will lay them out in the grid specified, here a 3x3.

The `Size(10, 10)` is the size, 10x10 pixels, of the characters/tiles on the source texture, i.e. how much space `@` takes up. This is a key factor in choosing a tile sheet.

A word on the performance of this solution, by default, this version is configured to render up to a maximum of 4096 tiles and _just_ manages to run at 60fps (for me), but with no business logic. 4000 tiles is an 80x50 which is one of the standard roguelike game grid sizes. However, performance will varying from platform to platform and browser to browser. The performance problem here is a that your allocating a couple of massive arrays every frame, and the GC has to keep up. Two ways to reduce GC pressure:

1. Lower your FPS! Do you need 60 fps for an ASCII game? Probably not! 30 fps would likely be fine. As you lower FPS what you get (aside from less frequent graphics updates) is input lag. So another way to go is to artificially lower fps: Leave your game running at 60fps, but put in a throttle that only redraws the view every 15-30 fps based on time since last draw.
2. Lower the max array size. If you can get away with a smaller grid, lower the array size to reduce the amount of clean up needed each frame. This must be done in two places (**AND they must be the same value!!**):

  a. `TerminalEntity.scala` - `private val total = 4096` - change 4096 to, say, 1024.
  b. `map.frag` - `#define MAX_TILE_COUNT 4096` - change 4096 to, say, 1024.

> **IMPORTANT** - the max size _will_ be allocated whether your grid is 80x50 or 3x3, so if you only need 9 tiles, lower the value to 9! You can have more than one `TerminalEntity` if that is useful...

If this proves insufficient I can look into other ways of speeding it up. Please report an issue.

## How to run and package up this game

By default you can just use the build in sbt command aliases, `sbt runGame` and `sbt buildGame`.

However, with some minor tweaks to the `build.sbt` file (see the comments in the file), you can also use parcel.js to run (including some hot-reloading) or bundle up your game for web deployment.

[Parcel instructions can be found here.](https://github.com/PurpleKingdomGames/indigo-examples/blob/master/howto/parcel/README.md)

