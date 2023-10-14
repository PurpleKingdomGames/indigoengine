//> using scala "3.3.1"
//> using platform "js"
//> using jsVersion "1.14.0"

//> using lib "io.indigoengine::ultraviolet::0.1.3"
//> using lib "io.indigoengine::indigo::0.15.0"

import indigo.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object SimpleVoronoi extends IndigoShader:

  val config: GameConfig =
    GameConfig.default
      .withFrameRateLimit(FPS.`60`)
      .withViewport(400, 400)
      .noResize

  val assets: Set[AssetType]      = Set()
  val channel0: Option[AssetPath] = None
  val channel1: Option[AssetPath] = None
  val channel2: Option[AssetPath] = None
  val channel3: Option[AssetPath] = None

  val shader: Shader =
    CustomShader.shader

object CustomShader:

  val shader: Shader =
    UltravioletShader.entityFragment(
      ShaderId("shader"),
      EntityShader.fragment[FragmentEnv](fragment, FragmentEnv.reference)
    )

  import ultraviolet.syntax.*

  // Ported from: https://www.youtube.com/watch?v=l-07BXzNdPw&feature=youtu.be
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  inline def fragment: Shader[FragmentEnv, Unit] =
    Shader[FragmentEnv] { env =>

      def N22(p: vec2): vec2 =
        var a: vec3 = fract(p.xyx * vec3(123.34f, 234.34f, 345.65f))
        a = a + dot(a, a + 34.45f)
        fract(vec2(a.x * a.y, a.y * a.z))

      def fragment(color: vec4): vec4 =
        val uv: vec2 = (2.0f * env.SCREEN_COORDS - env.SIZE) / env.SIZE.y

        val t: Float       = env.TIME
        var minDist: Float = 100.0f

        _for(0.0f, _ < 50.0f, _ + 1.0f) { i =>
          val n: vec2 = N22(vec2(i))
          val p: vec2 = sin(n * t)

          val d = length(uv - p)

          if d < minDist then minDist = d
        }

        vec4(vec3(minDist), 1.0f)

    }
