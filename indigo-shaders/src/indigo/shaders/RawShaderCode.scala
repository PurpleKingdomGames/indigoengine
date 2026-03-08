package indigo.shaders

trait RawShaderCode {
  val id: ShaderId
  val vertex: String
  val fragment: String
}

object RawShaderCode {

  def fromUltravioletShader(customShader: UltravioletShader): RawShaderCode =
    new RawShaderCode {
      val id: ShaderId     = customShader.id
      val vertex: String   = customShader.vertex.code
      val fragment: String = customShader.fragment.code
    }

  def fromEntityShader(customShader: EntityShader.Source): RawShaderCode =
    new RawShaderCode {
      val id: ShaderId =
        customShader.id

      val vertex: String =
        EntityShader.vertexTemplate(customShader.vertex)

      val fragment: String =
        EntityShader.fragmentTemplate(
          s"""
          |${customShader.fragment}
          |
          |${customShader.prepare}
          |
          |${customShader.light}
          |
          |${customShader.composite}
          |""".stripMargin.trim
        )
    }

  def fromBlendShader(customShader: BlendShader.Source): RawShaderCode =
    new RawShaderCode {
      val id: ShaderId =
        customShader.id

      val vertex: String =
        BlendShader.vertexTemplate(customShader.vertex)

      val fragment: String =
        BlendShader.fragmentTemplate(customShader.fragment)
    }

}
