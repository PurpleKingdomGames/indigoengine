[![MIT License](https://img.shields.io/github/license/PurpleKingdomGames/ultraviolet?color=indigo)](https://github.com/tterb/atomic-design-ui/blob/master/LICENSEs)
[![Latest Tagged Release](https://img.shields.io/badge/dynamic/json?color=purple&label=latest%20release&query=%24%5B0%5D.name&url=https%3A%2F%2Fapi.github.com%2Frepos%2FPurpleKingdomGames%2Fultraviolet%2Ftags)](https://github.com/PurpleKingdomGames/ultraviolet/releases)
[![Discord Chat](https://img.shields.io/discord/716435281208672356?color=blue&label=discord)](https://discord.gg/b5CD47g)
[![CI](https://github.com/PurpleKingdomGames/ultraviolet/actions/workflows/ci.yml/badge.svg)](https://github.com/PurpleKingdomGames/ultraviolet/actions/workflows/ci.yml)

# 🚧 Ultraviolet 🚧

> Under construction!

Ultraviolet is a Scala 3 to GLSL transpiler library.

## Motivation

This project is motivated from two needs:

1. The most pressing need is that GLSL tooling is patchy, and I'd like to have a much better shadering writing experience both for myself and any other Scala devs whether they're writing shaders for [Indigo](https://indigoengine.io/), [ShaderToy](https://www.shadertoy.com/), or some other Scala frontend web framework.
2. Indigo is currently locked into WebGL 2.0, and to move to other platforms or rendering technologies means having some way to abstract away from that. 

# Current Goals & Status

Right now, the goal is almost a like for like experience of writing GLSL for WebGL 2.0 (and 1.0 shortly after) in Scala 3, in all it's very specific procedural glory. I'd like to add a few quality of life improvements (like lambdas and function composition - done!) but nothing fancy. I would like to be able to write unit tests. It will be contrived and riddled with corner cases, but even just being able to do that will be super useful (to me).

It is _not_ a goal to be able to write arbirary Scala and have it turned into GLSL. In other ways this isn't a 'full' transpiler (like Scala.js), it's a useful cross-over subset of Scala and GLSL. As many GLSL language features as can sensibly be represented (expect 90%), and as much Scala as GLSL can be coerced into expressing.

Ultimately I'd like to be able to write Shaders in FP friendly Scala that can target more than just GLSL 300 - but that is a lot of work and not necessary for a first useful shippable version of Ultraviolet.

---

## Notes and limitations

Just writing these down during the development process, mostly for me! They may not stay this way...

- GLSL does not support recursion
- GLSL does not support product types, it does allow you to set multiple 'out' values from functions, but for now, we're limited to functions that return a single simple datatype.
- GLSL supports for loops, but we have no way to represent the traditional 'for loop' in Scala, and 'for expressions' are pure syntactic sugar. So no for loops.
- Imports work for free, but only if the things you're importing are inlined, which comes with the usual caveats.
- Pattern matching emulates switch statements, but they are side effecting, not used for setting `val`s.
- Unlike in GLSL, if statements can be used to return values and functions.
- Although Ultraviolet is based on GLSL 300, I've kept `texture2D` and `textureCube` from WebGL 1.0 and it is rewritten to `texture` for WebGL 2.0. This allows us to be more specific on the API.
- Preprocessor directives largely don't exist, but `#define` supported for special cases where you need to define a global value based on a non-constant value.
- Ultraviolet supports the % operator where GLSL does not.
- We do not support unsigned integers (for now), hence `uint` and `uvec` are not supported.
- GLSL does not consistenly support `double`s, and so we don't support `double` or `dvec` either.

Inlining external stuff:
- Here, 'external' means 'not inside the body of your shader'.
- Handy for testing!
- You cannot inline external `val`s.
- You can inline external defs into your code and they turn up more or less as expected.
- A def that is essentially a call but reference val such as `inline def x = 1.0f` will have it's value inlined.
- A def that is a function, laid out like a method e.g. `inline def foo(c: Int): Int = c + 1` will be embedded as a function called `foo`, the argument `c` will be ignored however, and the value passed will be inlined. Bit weird.
- A def that is a lambda however, will be embedded with a new name and will work exactly as you'd expect, recommend you do this! `inline def foo: Int => Int = c => c + 1`
