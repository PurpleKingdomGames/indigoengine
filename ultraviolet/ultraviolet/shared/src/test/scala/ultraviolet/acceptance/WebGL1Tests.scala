package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class WebGL1Tests extends munit.FunSuite {

  test("Can generate a simple valid WebGL 1.0 fragment shader") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
    inline def fragment =
      Shader[WebGL1Env, Unit] { env =>
        PrecisionMediumPFloat
        @uniform val u_texture: sampler2D.type = sampler2D
        @in val v_texcoord: vec2               = null

        def main: Unit =
          env.gl_FragColor = texture2D(u_texture, v_texcoord)
      }

    val actual =
      fragment.toGLSL[WebGL1].code

    // DebugAST.toAST(fragment)
    // println(actual)

    val expected: String =
      """
      |precision mediump float;
      |uniform sampler2D u_texture;
      |varying vec2 v_texcoord;
      |void main(){
      |  gl_FragColor=texture2D(u_texture,v_texcoord);
      |}
      |""".stripMargin.trim

    assertEquals(actual, expected)
  }

  test("Can generate a simple valid WebGL 1.0 vertex shader") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
    inline def fragment =
      Shader[WebGL1Env, Unit] { env =>
        @attribute val a_verticesAndCoords: vec4 = null

        @uniform val u_projection: mat4     = null
        @uniform val u_translateScale: vec4 = null
        @uniform val u_refRotation: vec4    = null
        @uniform val u_frameTransform: vec4 = null
        @uniform val u_sizeFlip: vec4       = null
        @uniform val u_baseTransform: mat4  = null

        @in var v_texcoord: vec2 = null

        def translate2d(t: vec2): mat4 =
          mat4(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, t.x, t.y, 0f, 1f)

        def scale2d(s: vec2): mat4 =
          mat4(s.x, 0f, 0f, 0f, 0f, s.y, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)

        def rotate2d(angle: Float): mat4 =
          mat4(cos(angle), -sin(angle), 0f, 0f, sin(angle), cos(angle), 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)

        def scaleTexCoordsWithOffset(texcoord: vec2, offset: vec2): vec2 =
          val transform: mat4 = translate2d(offset) * scale2d(u_frameTransform.zw)
          (transform * vec4(texcoord, 1.0f, 1.0f)).xy

        def scaleTexCoords(texcoord: vec2): vec2 =
          scaleTexCoordsWithOffset(texcoord, u_frameTransform.xy)

        def main(): Unit =
          val vertices: vec4  = vec4(a_verticesAndCoords.xy, 1.0f, 1.0f);
          val texcoords: vec2 = a_verticesAndCoords.zw;

          val ref: vec2                  = u_refRotation.xy;
          val size: vec2                 = u_sizeFlip.xy;
          val flip: vec2                 = u_sizeFlip.zw;
          val translation: vec2          = u_translateScale.xy;
          val scale: vec2                = u_translateScale.zw;
          val rotation: Float            = u_refRotation.w;
          val moveToReferencePoint: vec2 = -(ref / size) + 0.5f;

          val transform: mat4 =
            translate2d(translation) *
              rotate2d(-1.0f * rotation) *
              scale2d(size * scale) *
              translate2d(moveToReferencePoint) *
              scale2d(vec2(1.0f, -1.0f) * flip);

          env.gl_Position = u_projection * u_baseTransform * transform * vertices;

          v_texcoord = scaleTexCoords(texcoords);
      }

    val actual =
      fragment.toGLSL[WebGL1].code

    // DebugAST.toAST(fragment)
    // println(actual)

    val expected: String =
      """
      |attribute vec4 a_verticesAndCoords;
      |uniform mat4 u_projection;
      |uniform vec4 u_translateScale;
      |uniform vec4 u_refRotation;
      |uniform vec4 u_frameTransform;
      |uniform vec4 u_sizeFlip;
      |uniform mat4 u_baseTransform;
      |varying vec2 v_texcoord;
      |mat4 translate2d(in vec2 t){
      |  return mat4(1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0,t.x,t.y,0.0,1.0);
      |}
      |mat4 scale2d(in vec2 s){
      |  return mat4(s.x,0.0,0.0,0.0,0.0,s.y,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0);
      |}
      |mat4 rotate2d(in float angle){
      |  return mat4(cos(angle),-sin(angle),0.0,0.0,sin(angle),cos(angle),0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0);
      |}
      |vec2 scaleTexCoordsWithOffset(in vec2 texcoord,in vec2 offset){
      |  mat4 transform=translate2d(offset)*scale2d(u_frameTransform.zw);
      |  return (transform*vec4(texcoord,1.0,1.0)).xy;
      |}
      |vec2 scaleTexCoords(in vec2 texcoord){
      |  return scaleTexCoordsWithOffset(texcoord,u_frameTransform.xy);
      |}
      |void main(){
      |  vec4 vertices=vec4(a_verticesAndCoords.xy,1.0,1.0);
      |  vec2 texcoords=a_verticesAndCoords.zw;
      |  vec2 ref=u_refRotation.xy;
      |  vec2 size=u_sizeFlip.xy;
      |  vec2 flip=u_sizeFlip.zw;
      |  vec2 translation=u_translateScale.xy;
      |  vec2 scale=u_translateScale.zw;
      |  float rotation=u_refRotation.w;
      |  vec2 moveToReferencePoint=(-(ref/size))+0.5;
      |  mat4 transform=(translate2d(translation)*(rotate2d((-1.0)*rotation)))*(scale2d(size*scale))*translate2d(moveToReferencePoint)*(scale2d((vec2(1.0,-1.0))*flip));
      |  gl_Position=((u_projection*u_baseTransform)*transform)*vertices;
      |  v_texcoord=scaleTexCoords(texcoords);
      |}
      |""".stripMargin.trim

    assertEquals(actual, expected)
  }

}
