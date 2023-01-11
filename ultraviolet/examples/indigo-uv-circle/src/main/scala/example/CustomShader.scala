package example

import indigo.EntityShader
import indigo.ShaderId
import ultraviolet.syntax.*

object CustomShader:

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  final case class FragEnv(UV: vec2, var COLOR: vec4)

  inline def fragment =
    Shader[FragEnv, Unit] { env =>
      def circleSdf(p: vec2, r: Float): Float =
        length(p) - r

      def calculateColour(uv: vec2, sdf: Float): vec4 =
        val fill       = vec4(uv, 0.0f, 1.0f)
        val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
        vec4(fill.xyz * fillAmount, fillAmount)

      def fragment: Unit =
        val sdf = circleSdf(env.UV - 0.5f, 0.5f)
        env.COLOR = calculateColour(env.UV, sdf)
    }

  val shaderId: ShaderId =
    ShaderId("custom shader")

  val program: EntityShader.Source =
    EntityShader
      .Source(shaderId)
      .withFragmentProgram(fragment.toGLSL[WebGL2].toOutput.code)
