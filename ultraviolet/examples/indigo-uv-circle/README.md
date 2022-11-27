# Indigo + Ultraviolet Example

> Warning: Ultraviolet has not yet been released, so to run these examples you will first need to run `sbt localPublish` (that isn't a typo!) in the main project directory.

This is a simple example of using an Ultraviolet generated shader inside Indigo.

## Running the example

Having checked out the repo and arrived in this example's directory in your terminal, simply run: `sbt runGame`

## Information

Note that this is the same version of Indigo that is currently released, and requires no special mechanisms to work. What is happening is that Ultraviolet is generating the requires GLSL - as if you'd written it by hand - and then is it being incorporated as an `EntityShader.Source` shader.

`EntityShader.Source` shaders are normally quite horrible to work with because they are just GLSL but written into a standard Scala multiline string. No IDE or GLSL linter help available. By using Ultraviolet we get the full support of the Scala compiler to make sure we're getting things right.

The problems this approach (which is the only one right now!) does not solve are that ou still need to know how to write shaders! You also need to know what environment variables and so on are available (see the docs), in order to construct a usable `In` value, as we've done here with the `FragEnv` case class:

```scala
// This is only two of the values that are available.
final case class FragEnv(UV: vec2, var COLOR: vec4)
```
