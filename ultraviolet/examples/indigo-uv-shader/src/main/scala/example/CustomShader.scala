import indigo.EntityShader
import indigo.FragmentEnv
import indigo.ShaderId
import indigo.UltravioletShader
import indigo.VertexEnv
import ultraviolet.syntax.*

object CustomShader:

  val shaderId: ShaderId =
    ShaderId("custom shader")

  val shader: UltravioletShader =
    // Ported from: https://www.youtube.com/watch?v=l-07BXzNdPw&feature=youtu.be
    UltravioletShader.entityFragment(
      shaderId,
      EntityShader.fragment(modifyColor)
    )

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  inline def N22 = (p: vec2) =>
    var a: vec3 = fract(p.xyx * vec3(123.34f, 234.34f, 345.65f))
    a = a + dot(a, a + 34.45f)
    fract(vec2(a.x * a.y, a.y * a.z))

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  inline def modifyColor: vec4 => Shader[FragmentEnv, vec4] =
    inputColor =>
      Shader[FragmentEnv, vec4] { env =>
        val uv: vec2 = (2.0f * env.SCREEN_COORDS - env.SIZE) / env.SIZE.y

        var m: Float       = 0.0f
        val t: Float       = env.TIME
        var minDist: Float = 100.0f

        _for(0.0f, _ < 50.0f, _ + 1.0f) { i =>
          val n: vec2 = N22(vec2(i))
          val p: vec2 = sin(n * t)

          val d = length(uv - p)
          m = m + smoothstep(0.02f, 0.01f, d)

          if d < minDist then minDist = d
        }

        // val col: vec3 = vec3(m) // circles
        // val col: vec3 = vec3(minDist) // simple voronoi
        val col: vec3 = vec3(minDist) + vec3(m) // simple voronoi + circle

        vec4(col, 1.0f)
      }
