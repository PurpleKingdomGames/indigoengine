package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLMatrixTests extends munit.FunSuite {

  test("Matrix multiplication") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    case class VertEnv(
        VERTEX: vec4,
        POSITION: vec2,
        ROTATION: Float,
        SIZE: vec2,
        SCALE: vec2,
        FLIP: vec2,
        REF: vec2,
        u_projection: mat4,
        u_baseTransform: mat4,
        var gl_Position: vec4
    )

    inline def fragment =
      Shader[VertEnv, Unit] { env =>
        def translate2d(t: vec2): mat4 =
          mat4(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, t.x, t.y, 0f, 1f)

        def scale2d(s: vec2): mat4 =
          mat4(s.x, 0f, 0f, 0f, 0f, s.y, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)

        def rotate2d(angle: Float): mat4 =
          mat4(cos(angle), -sin(angle), 0f, 0f, sin(angle), cos(angle), 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)

        def main: Unit =
          val transform =
            translate2d(env.POSITION) *
              rotate2d(-1.0f * env.ROTATION) *
              scale2d(env.SIZE * env.SCALE) *
              translate2d(-(env.REF / env.SIZE) + 0.5f) *
              scale2d(vec2(1.0f, -1.0f) * env.FLIP);

          env.gl_Position = env.u_projection * env.u_baseTransform * transform * env.VERTEX;
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |mat4 translate2d(in vec2 t){
      |  return mat4(1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0,t.x,t.y,0.0,1.0);
      |}
      |mat4 scale2d(in vec2 s){
      |  return mat4(s.x,0.0,0.0,0.0,0.0,s.y,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0);
      |}
      |mat4 rotate2d(in float angle){
      |  return mat4(cos(angle),-sin(angle),0.0,0.0,sin(angle),cos(angle),0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0);
      |}
      |void main(){
      |  mat4 transform=(translate2d(POSITION)*(rotate2d((-1.0)*ROTATION)))*(scale2d(SIZE*SCALE))*(translate2d((-(REF/SIZE))+0.5))*(scale2d((vec2(1.0,-1.0))*FLIP));
      |  gl_Position=((u_projection*u_baseTransform)*transform)*VERTEX;
      |}
      |""".stripMargin.trim
    )
  }

}
