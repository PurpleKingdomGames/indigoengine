package ultraviolet.datatypes

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class ShaderTests extends munit.FunSuite {

  test("Shader's can be run (and so, tested)") {

    case class UBO1(UV: vec2)

    def shader =
      Shader[UBO1, vec4] { env =>
        vec4(env.UV, 2.0f, 1.0f)
      }

    val actual =
      shader.run(UBO1(vec2(4.0f, 3.0f)))

    val expected =
      vec4(4.0f, 3.0f, 2.0f, 1.0f)

    assertEquals(actual, expected)

  }

  test("Shader's can be run with an intersection type") {

    trait UBO1:
      def UV: vec2
    trait UBO2:
      def TIME: Float

    case class UBO(UV: vec2, TIME: Float) extends UBO1, UBO2

    def shader =
      Shader[UBO1 & UBO2, vec4] { env =>
        vec4(env.UV, 2.0f, 1.0f)
      }

    val actual =
      shader.run(UBO(vec2(4.0f, 3.0f), 2.0f))

    val expected =
      vec4(4.0f, 3.0f, 2.0f, 1.0f)

    assertEquals(actual, expected)

  }

  test("(map) Shader's can be mapped in a simple case") {

    case class UBO1(UV: vec2)

    inline def shader: Shader[UBO1, vec2] =
      Shader[UBO1, vec4] { env =>
        ubo[UBO1]
        vec4(env.UV, 2.0f, 1.0f)
      }.map(_.xy)

    val actual =
      shader.run(UBO1(vec2(4.0f, 3.0f)))

    val expected =
      vec2(4.0f, 3.0f)

    assertEquals(actual, expected)

    val actualCode =
      shader.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(shader)
    // println(actualCode)

    assertEquals(
      actualCode,
      s"""
      |layout (std140) uniform UBO1 {
      |  vec2 UV;
      |};
      |vec2 def0(in vec4 val0){
      |  return val0.xy;
      |}
      |def0(vec4(UV,2.0,1.0));
      |""".stripMargin.trim
    )
  }

  test("(map) even simpler") {

    case class UBO1(UV: vec2)

    inline def shader: Shader[UBO1, vec2] =
      Shader[UBO1, vec4] { env =>
        vec4(env.UV, 2.0f, 1.0f)
      }.map(_.xy)

    val actual =
      shader.run(UBO1(vec2(4.0f, 3.0f)))

    val expected =
      vec2(4.0f, 3.0f)

    assertEquals(actual, expected)

    val actualCode =
      shader.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(shader)
    // println(actualCode)

    assertEquals(
      actualCode,
      s"""
      |vec2 def0(in vec4 val0){
      |  return val0.xy;
      |}
      |def0(vec4(UV,2.0,1.0));
      |""".stripMargin.trim
    )
  }

  test("(map) can be chained") {

    case class UBO1(UV: vec2)

    inline def shader: Shader[UBO1, Float] =
      Shader[UBO1, vec4] { env =>
        vec4(env.UV, 2.0f, 1.0f)
      }.map(_.xy).map(v2 => v2.y).map(f => f * 10.0f)

    val actual =
      shader.run(UBO1(vec2(4.0f, 3.0f)))

    val expected =
      30.0f

    assertEquals(actual, expected)

    val actualCode =
      shader.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(shader)
    // println(actualCode)

    assertEquals(
      actualCode,
      s"""
      |vec2 def0(in vec4 val0){
      |  return val0.xy;
      |}
      |float def1(in vec2 v2){
      |  return v2.y;
      |}
      |float def2(in float f){
      |  return f*10.0;
      |}
      |def2(def1(def0(vec4(UV,2.0,1.0))));
      |""".stripMargin.trim
    )
  }

  test("(map) use external functions") {

    case class UBO1(UV: vec2)

    inline def f: vec4 => Float   = _.x
    inline def g(a: Float): Float = a * 20.0f

    inline def shader: Shader[UBO1, Float] =
      Shader[UBO1, vec4] { env =>
        vec4(env.UV, 2.0f, 1.0f)
      }.map(f).map(g(_))

    val actual =
      shader.run(UBO1(vec2(4.0f, 3.0f)))

    val expected =
      80.0f

    assertEquals(actual, expected)

    val actualCode =
      shader.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(shader)
    // println(actualCode)

    assertEquals(
      actualCode,
      s"""
      |float def0(in vec4 val0){
      |  return val0.x;
      |}
      |float def1(in float val1){
      |  return val1*20.0;
      |}
      |def1(def0(vec4(UV,2.0,1.0)));
      |""".stripMargin.trim
    )
  }

  test("(map) complex body") {

    case class UBO1(UV: vec2)

    inline def f: vec4 => Float  = _.x
    inline def g: Float => Float = _ * 20

    inline def shader: Shader[UBO1, Float] =
      Shader[UBO1, vec4] { env =>
        vec4(env.UV, 2.0f, 1.0f)
      }.map { v =>
        val h = (g compose f)(v)

        h + 10.0f
      }

    val actual =
      shader.run(UBO1(vec2(4.0f, 3.0f)))

    val expected =
      90.0f

    assertEquals(actual, expected)

    val actualCode =
      shader.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(shader)
    // println(actualCode)

    assertEquals(
      actualCode,
      s"""
      |float def1(in float val0){
      |  return val0*20;
      |}
      |float def2(in vec4 val1){
      |  return val1.x;
      |}
      |float def3(in vec4 val2){
      |  return def1(def2(val2));
      |}
      |float def0(in vec4 v){
      |  float h=def3(v);
      |  return h+10.0;
      |}
      |def0(vec4(UV,2.0,1.0));
      |""".stripMargin.trim
    )
  }

  test("(flatMap) Shader's can be flat mapped (flatMap)") {

    case class UBO1(UV: vec2)

    inline def shader: Shader[UBO1, vec2] =
      Shader[UBO1, vec4] { env =>
        ubo[UBO1]
        vec4(env.UV, 2.0f, 1.0f)
      }.flatMap { v4 =>
        Shader[UBO1, vec2] { _ =>
          v4.xy
        }
      }

    val actual =
      shader.run(UBO1(vec2(4.0f, 3.0f)))

    val expected =
      vec2(4.0f, 3.0f)

    assertEquals(actual, expected)

    val actualCode =
      shader.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(shader)
    // println(actualCode)

    assertEquals(
      actualCode,
      s"""
      |layout (std140) uniform UBO1 {
      |  vec2 UV;
      |};
      |vec2 def0(in vec4 v4){
      |  return v4.xy;
      |}
      |def0(vec4(UV,2.0,1.0));
      |""".stripMargin.trim
    )
  }

  test("(flatMap) Shader's can be flat mapped (for comp)") {

    case class UBO1(UV: vec2)

    inline def base: Float => Shader[UBO1, vec4] =
      (z: Float) =>
        Shader[UBO1, vec4] { env =>
          vec4(env.UV, z, 1.0f)
        }

    inline def toVec2(v4: vec4): Shader[UBO1, vec2] =
      Shader[UBO1, vec2] { env =>
        v4.xy
      }

    inline def calc: Shader[UBO1, vec2] =
      for
        a <- base(20.0f)
        b <- toVec2(a)
      yield b + 1.0f

    inline def shader: Shader[UBO1, vec2] =
      Shader[UBO1, vec2] { env =>
        ubo[UBO1]
        calc.run(env)
      }

    val actual =
      shader.run(UBO1(vec2(4.0f, 3.0f)))

    val expected =
      vec2(5.0f, 4.0f)

    assertEquals(actual, expected)

    val actualCode =
      shader.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(shader)
    // println(actualCode)

    assertEquals(
      actualCode,
      s"""
      |layout (std140) uniform UBO1 {
      |  vec2 UV;
      |};
      |vec4 def0(in float z){
      |  return vec4(UV,z,1.0);
      |}
      |vec2 def2(in vec2 b){
      |  return b+1.0;
      |}
      |vec2 def1(in vec4 a){
      |  return def2(a.xy);
      |}
      |def1(def0(20.0));
      |""".stripMargin.trim
    )
  }

  test("(flatMap) Flat mapped shaders can declare a ubo anywhere") {

    case class UBO1(UV: vec2)
    case class UBO2(TIME: highp[Float])

    inline def base: Float => Shader[UBO1, vec4] =
      (z: Float) =>
        Shader[UBO1, vec4] { env =>
          ubo[UBO1]
          vec4(env.UV, z, 1.0f)
        }

    inline def toVec2(v4: vec4): Shader[UBO1, vec2] =
      Shader[UBO1, vec2] { env =>
        @uniform val ALPHA: Float = 0.0f;
        val res                   = v4.xy
        ubo[UBO2]
        res
      }

    inline def shader: Shader[UBO1, vec2] =
      for
        a <- base(20.0f)
        b <- toVec2(a)
      yield b + 1.0f

    val actual =
      shader.run(UBO1(vec2(4.0f, 3.0f)))

    val expected =
      vec2(5.0f, 4.0f)

    assertEquals(actual, expected)

    val actualCode =
      shader.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(shader)
    // println(actualCode)

    assertEquals(
      actualCode,
      s"""
      |layout (std140) uniform UBO1 {
      |  vec2 UV;
      |};
      |layout (std140) uniform UBO2 {
      |  highp float TIME;
      |};
      |uniform float ALPHA;
      |vec4 def0(in float z){
      |  return vec4(UV,z,1.0);
      |}
      |vec2 def2(in vec2 b){
      |  return b+1.0;
      |}
      |vec2 def1(in vec4 a){
      |  vec2 res=a.xy;
      |  return def2(res);
      |}
      |def1(def0(20.0));
      |""".stripMargin.trim
    )
  }

}
