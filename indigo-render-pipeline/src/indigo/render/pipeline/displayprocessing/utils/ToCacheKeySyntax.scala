package indigo.render.pipeline.displayprocessing.utils

import indigo.shaders.ShaderData

object ToCacheKeySyntax:

  extension (sd: ShaderData)
    def toCacheKey: String =
      sd.shaderId.toString +
        sd.channel0.map(_.toString).getOrElse("") +
        sd.channel1.map(_.toString).getOrElse("") +
        sd.channel2.map(_.toString).getOrElse("") +
        sd.channel3.map(_.toString).getOrElse("") +
        sd.uniformBlocks.map(_.uniformHash).mkString
