# ShaderToy Examples

> Warning: Ultraviolet has not yet been released, so to run these examples you will first need to run `sbt localPublish` (that isn't a typo!) in the main project directory.

There are three [ShaderToy](https://www.shadertoy.com/) examples that have been ported to Ultraviolet in this directory:

1. Default - this is the default shader you are given when you make a new shader toy project.
2. Plasma by [Jolle](https://www.shadertoy.com/user/jolle) - https://www.shadertoy.com/view/XsVSDz
3. Seascape by [TDM](https://www.shadertoy.com/user/TDM) - https://www.shadertoy.com/view/Ms2SD1

# Running the examples

You can see the output of the scripts in the `glsl` directory, but if you'd like to build them yourself then you will need [Scala-CLI](https://scala-cli.virtuslab.org/).

Once installed, simple run any of the following in your terminal:

```sh
scala-cli run Default.scala
scala-cli run Plasma.scala
scala-cli run Seascape.scala
```

They will re-generate the files within the `glsl` directory.

To run the examples, head over the [ShaderToy](https://www.shadertoy.com/) and create a new project.

`Default` and `Seascape` can be pasted straight into the editor and run.

`Plasma` is in two parts, first add a tab of type 'Buffer A' and paste the contents of `plasma-buffer-a.frag` into it. Then paste the contents of `plasma-image.frag` into the default 'image' tab. You then need to set Buffer A as a source for Image, then you can run the shader program.
