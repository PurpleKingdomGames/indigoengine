package indigo.shaders.library

import ultraviolet.syntax.*

import scala.annotation.nowarn

object Quad:

  trait Env extends Lighting.LightEnv {
    val FILL_TYPE: Float          = 0.0f
    val CORNER_RADII: vec4        = vec4(0.0f)
    val GRADIENT_FROM_TO: vec4    = vec4(0.0f)
    val GRADIENT_FROM_COLOR: vec4 = vec4(0.0f)
    val GRADIENT_TO_COLOR: vec4   = vec4(0.0f)
  }
  object Env:
    val reference: Env = new Env {}

  case class IndigoQuadData(
      FILL_TYPE: Float,
      EMPTY_1: Float,
      EMPTY_2: Float,
      EMPTY_3: Float,
      CORNER_RADII: vec4,
      GRADIENT_FROM_TO: vec4,
      GRADIENT_FROM_COLOR: vec4,
      GRADIENT_TO_COLOR: vec4
  )

  @nowarn("msg=unused")
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  inline def fragment =
    Shader[Env] { env =>
      import ShapeShaderFunctions.*

      // Delegates
      val _calculateLinearGradient: (vec2, vec2, vec2, vec4, vec4) => vec4 =
        calculateLinearGradient
      val _calculateRadialGradient: (vec2, vec2, vec2, vec4, vec4) => vec4 =
        calculateRadialGradient

      ubo[IndigoQuadData]

      def insideBox(tl: vec2, br: vec2, p: vec2): Boolean =
        p.x >= tl.x && p.x <= br.x && p.y >= tl.y && p.y <= br.y

      def fragment(color: vec4): vec4 =
        val fillType = round(env.FILL_TYPE).toInt
        val fill: vec4 =
          fillType match
            case 1 =>
              _calculateLinearGradient(
                env.GRADIENT_FROM_TO.xy,
                env.GRADIENT_FROM_TO.zw,
                env.UV * env.SIZE,
                env.GRADIENT_FROM_COLOR,
                env.GRADIENT_TO_COLOR
              )

            case 2 =>
              _calculateRadialGradient(
                env.GRADIENT_FROM_TO.xy,
                env.GRADIENT_FROM_TO.zw,
                env.UV * env.SIZE,
                env.GRADIENT_FROM_COLOR,
                env.GRADIENT_TO_COLOR
              )

            case _ =>
              env.GRADIENT_FROM_COLOR

        // Dispose rules

        val ctl = env.CORNER_RADII.x
        val ctr = env.CORNER_RADII.y
        val cbr = env.CORNER_RADII.z
        val cbl = env.CORNER_RADII.w

        val coords = env.UV * env.SIZE

        var alpha = fill.a

        // TL
        if ctl > 0.0f && insideBox(vec2(0.0f), vec2(ctl), coords) && distance(coords, vec2(ctl)) > ctl then alpha = 0.0f

        // TR
        if ctr > 0.0f &&
          insideBox(vec2(env.SIZE.x - ctr, 0.0f), vec2(env.SIZE.x, ctr), coords) &&
          distance(coords, vec2(env.SIZE.x - ctr, ctr)) > ctr
        then alpha = 0.0f

        // BR
        if cbr > 0.0f &&
          insideBox(env.SIZE - cbr, env.SIZE, coords) &&
          distance(coords, env.SIZE - vec2(cbr)) > cbr
        then alpha = 0.0f

        // BL
        if cbl > 0.0f &&
          insideBox(vec2(0.0f, env.SIZE.y - cbl), vec2(cbl, env.SIZE.y), coords) &&
          distance(coords, vec2(cbl, env.SIZE.y - cbl)) > cbl
        then alpha = 0.0f

        vec4(fill.rgb * alpha, alpha)
    }
