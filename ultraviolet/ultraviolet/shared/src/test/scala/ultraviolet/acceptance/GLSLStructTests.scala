package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLStructTests extends munit.FunSuite {

  test("structs can be declared and used") {

    inline def fragment =
      Shader[Unit, Unit] { _ =>
        class Light(
            val eyePosOrDir: vec3,
            val isDirectional: Boolean,
            val intensity: vec3,
            val attenuation: Float
        )

        def makeLight(): Light =
          Light(vec3(1.0f), true, vec3(2.0f), 2.5f)

        def frag: Unit =
          val x = makeLight()
          val y = x.eyePosOrDir.y
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |struct Light{
      |  vec3 eyePosOrDir;
      |  bool isDirectional;
      |  vec3 intensity;
      |  float attenuation;
      |};
      |Light makeLight(){
      |  return Light(vec3(1.0),true,vec3(2.0),2.5);
      |}
      |void frag(){
      |  Light x=makeLight();
      |  float y=x.eyePosOrDir.y;
      |}
      |""".stripMargin.trim
    )
  }

}
