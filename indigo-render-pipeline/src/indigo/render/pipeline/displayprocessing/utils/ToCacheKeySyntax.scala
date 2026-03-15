package indigo.render.pipeline.displayprocessing.utils

import indigo.shaders.ShaderData

object ToCacheKeySyntax:

  def toCacheKey(sd: ShaderData): String =
    val sb = new StringBuilder(sd.shaderId.toString)
    sd.channel0.foreach(v => sb.append(v.toString))
    sd.channel1.foreach(v => sb.append(v.toString))
    sd.channel2.foreach(v => sb.append(v.toString))
    sd.channel3.foreach(v => sb.append(v.toString))
    sd.uniformBlocks.foreach(ub => sb.append(ub.uniformHash))
    sb.toString
