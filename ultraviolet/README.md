[![MIT License](https://img.shields.io/github/license/PurpleKingdomGames/ultraviolet?color=indigo)](https://github.com/tterb/atomic-design-ui/blob/master/LICENSEs)
[![Latest Tagged Release](https://img.shields.io/badge/dynamic/json?color=purple&label=latest%20release&query=%24%5B0%5D.name&url=https%3A%2F%2Fapi.github.com%2Frepos%2FPurpleKingdomGames%2Fultraviolet%2Ftags)](https://github.com/PurpleKingdomGames/ultraviolet/releases)
[![Discord Chat](https://img.shields.io/discord/716435281208672356?color=blue&label=discord)](https://discord.gg/b5CD47g)
[![CI](https://github.com/PurpleKingdomGames/ultraviolet/actions/workflows/ci.yml/badge.svg)](https://github.com/PurpleKingdomGames/ultraviolet/actions/workflows/ci.yml)

# Ultraviolet

Ultraviolet is a Scala 3 to GLSL transpiler library built on top of Scala 3 inline macros.

## Uses & Examples

Examples can be found in the examples directory of this repo. You can use Ultraviolet to generate GLSL shader code for Indigo, and also for ShaderToy.

## TL;DR: What is a Shader / Ultraviolet / basic programmer intuition

If you've stumbled across this repo and have no idea what all this shader stuff is about:

1. A shader program is used to render graphics on a GPU.
2. ***Think of a tiny C program that runs for every pixel on the screen***. It is not as simple as that, but that's a good start think about what's going on here..
3. Ultraviolet allows you to write those programs in Scala 3.

## Status: "It works on my machine"

Ultraviolet is in early stage development. It appears to be working well but there will be many, many corner cases that haven't been found yet. Please report bugs and issues!

## Motivation

This project is motivated from two needs:

1. The most pressing need is that GLSL tooling is patchy, and I'd like to have a much better shadering writing experience both for myself and any other Scala devs whether they're writing shaders for [Indigo](https://indigoengine.io/), [ShaderToy](https://www.shadertoy.com/), or some other Scala frontend web framework.
2. Indigo is currently locked into WebGL 2.0, and to move to other platforms or rendering technologies means having some way to abstract away from that. 

## Current Goals

Right now, the goal is an almost like-for-like experience of writing GLSL (for WebGL) in Scala 3, in all it's very specific procedural glory. It includes a few quality of life improvements such as anonymous functions and function composition, but nothing fancy for now. You can even write unit tests!

It is _not_ a goal to be able to write arbirary Scala and have it turned into GLSL. In other words this isn't a 'full' transpiler (like Scala.js), it's a useful cross-over subset of Scala and GLSL. As many GLSL language features as can sensibly be represented, and as much Scala as GLSL can be coerced into expressing.

Ultimately I'd like to be able to write Shaders in FP friendly Scala that can target more than just GLSL 300, but that is not necessary for Ultraviolet to be useful and fun.

# Language feature comparison

***The goldren rule is: Keep It Simple!***

GLSL is not a general purpose language like Scala is, and while it's possible to represent most of GLSL in Scala, the opposite is not true.

GLSL is for doing maths on simple numeric data types, and as someone else described it, is "a very limited programming model."

Go forth and do maths and make pretty pictures!

## Gotcha's, foot guns, and weird stuff

### Strings? Where we're going, we don't need Strings.

GLSL is a C-like language for doing maths. There are no `Char` or `String` types.

### No functions as return types

Functions are not first class citizens in GLSL, and so it is not possible (currently) to have a function as a return type of a function. Simple function composition does work, and the `Shader` type forms a monad you can `map` and `flatMap` over,

### Limited support for product types

You cannot make or use arbitrary Product types. For example, it is tempting to just make a little tuple in order to return two values from a function... but you can't.

The closest thing you can do is make use of 'structs', which in Ultraviolet are represented by classes declared in the shader body - but this is quite limited / limiting.

```scala
inline def fragment =
  Shader[Unit, Unit] { _ =>
    class Light(
        val eyePosOrDir: vec3,
        val isDirectional: Boolean,
        val intensity: vec3,
        val attenuation: Float
    )

    def makeLight(): Light =
      Light(vec3(1.0f), true, vec3(2.0f), 2.5f)

    def frag: Unit =
      val x = makeLight()
      val y = x.eyePosOrDir.y
  }
```

### No sum types

There is no way to represent anything like an enum, the closest you can get is using an `int` as a flag to switch on in a pattern match.

### No forward referencing

In Scala, you can call functions at the bottom of a program from code living at the top. This type of thing is called a forward reference, and is not allowed in GLSL.

There are compile time validation checks for this.

### No, your fancy library won't work here

Almost every language feature you have available via UltraViolet has required work to allow it to be converted to GLSL. Bringing in your favourite library that adds arbitrary functionality will not work.

### Nested functions and function purity

Because functions in Scala are first-class citizens, you can do all sorts of fancy things with them that we take for granted as Scala developers. One such thing is being able to arbitrarily nest functions.

In GLSL, functions are special, and can only exist at the top level of the program.

In general, this is manageable problem, but there are two rules to follow:

1. **'Named' functions e.g. `def foo(x: Float): vec2 = ???` _cannot_ be nested inside one another.** This is because Ultraviolet will preserve the order of your code including named functions, in order to avoid problems with forward references.
2. **Anonymous functions _can_ be nested, but _must be pure_.**. Ultraviolet will re-organise anonymous functions, this is what allows us to simulate things like function composition. The price is that anonymous functions must be pure, i.e. they can only produce a value based on their arguments, and cannot make reference to other outside entities that Scala would normally consider to be 'in scope'.

These rules should be enforced by compile time program validation.

### Just write a glsl as a String?

This is completely valid but only if it's the only contents of the block:

```scala
Shader {
  "int foo = 10;"
}
```

This is fine anywhere:


```scala
Shader {
  RawGLSL("int foo = 10;")
  // or
  raw("int bar = 11;")
}
```

But this will not work:

```scala
Shader {
  raw("int foo = 10;").trim
}
```

Because we can't do string-y things in GLSL, and trim is a string operation.

### Pattern matching weirdness

A pattern match is converted to a switch statement, and in GLSL you can only switch on an Int. So far that's limiting, but ok.

What is totally unintuitive is that on some graphics hardware, in some implmentations, switch statements will process ***all*** branches irrespective of whether they're going to be used or not.

The problem with that, is that if you declare the same variable name in two branches, the GLSL compiler will fail and tell you that you've redeclared it. Bonkers, but the takeaway is: Don't repeat variable names in pattern match branches...

### Unofficial reserved words

When writing shaders in Scala, Scala reserved words will be checked and errors shown by the compiler.

You shouldn't have too much trouble with GLSL reserved words because many of them have the same status in Scala, and Ultraviolets validation should catch all the others at compile time.

Naming conventions to avoid:
- Do not call a function something like `def xy(v: vec4): ???` because this will likely interfere with the Swizzle mechanisms. Not at the point of definition but at the point of use.
- Do not name anything `val0...N` or `def0...N`, as this is the naming scheme UltraViolet uses internally when it needs to create identifiers, and you'll end up in a mess.

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
- Preprocessor directives largely don't exist, but `#define` is supported for special cases where you need to define a global value based on a non-constant value.


## Things to know about inlining

Ultraviolet allows you to share / reuse code, as long as it is inlined following Scala 3's standard inlining rules. However there are things to know about how this will affect your GLSL!

Here, 'external' means 'not inside the body of your shader'.

- You cannot inline external `val`s.
- You can inline external `def`s into your code, but:
  - A def that is essentially a call by reference val such as `inline def x = 1.0f` will have it's value inlined.
  - A def that is a function, laid out like a method e.g. `inline def foo(c: Int): Int = c + 1` will be inlined.
  - A def that is an anonymous function will be embedded with a new name and will work exactly as you'd expect, i.e. `inline def foo: Int => Int = c => c + 1`
