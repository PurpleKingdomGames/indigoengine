[![MIT License](https://img.shields.io/github/license/PurpleKingdomGames/ultraviolet?color=indigo)](https://github.com/tterb/atomic-design-ui/blob/master/LICENSEs)
[![Latest Tagged Release](https://img.shields.io/badge/dynamic/json?color=purple&label=latest%20release&query=%24%5B0%5D.name&url=https%3A%2F%2Fapi.github.com%2Frepos%2FPurpleKingdomGames%2Fultraviolet%2Ftags)](https://github.com/PurpleKingdomGames/ultraviolet/releases)
[![Discord Chat](https://img.shields.io/discord/716435281208672356?color=blue&label=discord)](https://discord.gg/b5CD47g)
[![CI](https://github.com/PurpleKingdomGames/ultraviolet/actions/workflows/ci.yml/badge.svg)](https://github.com/PurpleKingdomGames/ultraviolet/actions/workflows/ci.yml)

# Ultraviolet

Ultraviolet is a Scala 3 to GLSL transpiler library built on top of Scala 3 inline macros.

## Uses & Examples

Examples can be found in the examples directory of this repo. You can use Ultraviolet to generate GLSL shader code for Indigo, and also for ShaderToy.

## Status: "It works on my machine"

Ultraviolet is in early stage development. It appears to be working well but there will be many, many corner cases that haven't been found yet. Please report bugs and issues!

## Motivation

This project is motivated from two needs:

1. The most pressing need is that GLSL tooling is patchy, and I'd like to have a much better shadering writing experience both for myself and any other Scala devs whether they're writing shaders for [Indigo](https://indigoengine.io/), [ShaderToy](https://www.shadertoy.com/), or some other Scala frontend web framework.
2. Indigo is currently locked into WebGL 2.0, and to move to other platforms or rendering technologies means having some way to abstract away from that. 

## Current Goals

Right now, the goal is an almost a like for like experience of writing GLSL for WebGL in Scala 3, in all it's very specific procedural glory. It will include a few quality of life improvements such as lambdas and function composition, but nothing fancy for now. You will also be able to write unit tests after a fashion.

It is _not_ a goal to be able to write arbirary Scala and have it turned into GLSL. In other words this isn't a 'full' transpiler (like Scala.js), it's a useful cross-over subset of Scala and GLSL. As many GLSL language features as can sensibly be represented, and as much Scala as GLSL can be coerced into expressing.

Ultimately I'd like to be able to write Shaders in FP friendly Scala that can target more than just GLSL 300 - but that is a lot of work and not necessary for a first useful shippable version of Ultraviolet.


# Language feature comparison

## Unofficial reserved words

When writing shaders in Scala, Scala reserved words will be checked and errors shown by the compiler.

You shouldn't have too much trouble with GLSL reserved words because many of them have the same status in Scala, but it's worth noting the GLSL is like C and that there will be words to avoid.

One thing to avoid: Do not call a function something like `def xy(v: vec4): ???` because this will likely interfere with the Swizzle mechanisms. Not at the point of definition but at the point of use.

## Comparison table

Only including the differences or note worthy features. If they're the same they are omitted.

| Feature                           | Scala | GLSL | Ultraviolet | Notes                                                                                            |
| --------------------------------- | ----- | ---- | ----------- | ------------------------------------------------------------------------------------------------ |
| Recursion                         | ✅     | ❌    | ❌           |
| A stack!                          | ✅     | ❌    | ❌           |
| `String` and `Char`               | ✅     | ❌    | ❌           |
| `uint` / `uvec`                   | ❌     | ✅    | ❌           |
| `Double` / `dvec`                 | ✅     | ❌    | ❌           |
| `struct`                          | ❌     | ✅    | 💡           | You can define structs by declaring classes.                                                     |
| for loops                         | ❌     | ✅    | 💡           | In Scala, use the `cfor` method provided.                                                        |
| Imports                           | ✅     | ❌    | ✅           | Imported methods and values must be inlined.                                                     |
| Switch statements                 | ❌     | ✅    | 💡           | Scala does not have switch statements, but they can be expressed using pattern matching instead. |
| If statements can return values   | ✅     | ❌    | ✅           |
| Pattern matches can return values | ✅     | ❌    | ✅           |
| `#define`                         | ❌     | ✅    | ✅           | Use the `@define` annotation. (see note below)                                                   |
| `const`                           | ❌     | ✅    | ✅           | `@const`                                                                                         |
| `uniform`                         | ❌     | ✅    | ✅           | `@uniform`                                                                                       |
| `varying`, `in`, `out`            | ❌     | ✅    | ✅           | `@in`, `@out`                                                                                    |
| `%` (modulus op)                  | ✅     | ❌    | ✅           |
| Lambda/Anonymous functions        | ✅     | ❌    | ✅           |
| `compose`                         | ✅     | ❌    | ✅           |
| `andThen`                         | ✅     | ❌    | ✅           |


Other comments:

- Although Ultraviolet is based on GLSL 300, I've kept `texture2D` and `textureCube` from WebGL 1.0 for clarity, and these are rewritten to `texture` for WebGL 2.0. 
- Preprocessor directives largely don't exist, but `#define` supported for special cases where you need to define a global value based on a non-constant value.


## Things to know about inlining

Ultraviolet allows you to share / reuse code, as long as it is inlined following Scala 3's standard inlining rules. However there are things to know about how this will affect your GLSL!

Here, 'external' means 'not inside the body of your shader'.

- You cannot inline external `val`s.
- You can inline external `def`s into your code, but:
  - A def that is essentially a call by reference val such as `inline def x = 1.0f` will have it's value inlined.
  - A def that is a function, laid out like a method e.g. `inline def foo(c: Int): Int = c + 1` will be embedded as a function called `foo` as expected, but the argument `c` will be ignored, and the value passed will be inlined. Bit weird!
  - A def that is a lambda, however, will be embedded with a new name and will work exactly as you'd expect, recommend you do this! `inline def foo: Int => Int = c => c + 1`
