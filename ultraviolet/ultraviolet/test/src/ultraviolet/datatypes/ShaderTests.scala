package ultraviolet.datatypes

import ultraviolet.DebugAST
import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
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
      val _base: Float => Shader[UBO1, vec4] = base
      for
        a <- _base(20.0f)
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
      val _base: Float => Shader[UBO1, vec4] = base
      for
        a <- _base(20.0f)
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

  test("Shader's can run functions that embed other shaders") {

    inline def modifyVertex: vec4 => Shader[Unit, vec4] =
      (vtx: vec4) =>
        Shader[Unit, vec4] { _ =>
          vtx + vec4(1.0f)
        }

    @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
    inline def shader(inline f: vec4 => Shader[Unit, vec4]): Shader[Unit, Unit] =
      Shader {
        val _f: vec4 => Shader[Unit, vec4] = f
        var VERTEX: vec4                   = null
        def vertex: Unit =
          VERTEX = _f(VERTEX).run(())
      }

    val actualCode =
      shader(modifyVertex).toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(shader(modifyVertex))
    // println(actualCode)

    assertEquals(
      actualCode,
      s"""
      |vec4 def0(in vec4 vtx){
      |  return vtx+vec4(1.0);
      |}
      |vec4 VERTEX;
      |void vertex(){
      |  VERTEX=def0(VERTEX);
      |}
      |""".stripMargin.trim
    )
  }

  test("Shader's can run functions that embed other shaders, declared inline") { // using external functions?

    // Note that in this case `f` does not have an `inline` qualifier.
    @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
    inline def shader(f: vec4 => Shader[Unit, vec4]): Shader[Unit, Unit] =
      Shader {
        var VERTEX: vec4 = null
        def vertex: Unit =
          VERTEX = f(VERTEX).run(())
      }

    val actualCode =
      shader { (input: vec4) =>
        Shader[Unit, vec4] { _ =>
          input + vec4(1.0f)
        }
      }.toGLSL[WebGL2](false).toOutput.code

    assertEquals(
      actualCode,
      s"""
      |vec4 def0(in vec4 input){
      |  return input+vec4(1.0);
      |}
      |vec4 VERTEX;
      |void vertex(){
      |  VERTEX=def0(VERTEX);
      |}
      |""".stripMargin.trim
    )
  }

  test("Shaders can be create from within a companion (inlined)") {

    inline def modifyVertex: vec4 => Shader[Foo.Env, vec4] =
      (vtx: vec4) =>
        Shader[Foo.Env, vec4] { _ =>
          vtx + vec4(1.0f)
        }

    inline def actualCode =
      Foo.shader(modifyVertex).toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(actualCode)
    // println(actualCode)

    assertEquals(
      actualCode,
      s"""
      |vec4 def0(in vec4 vtx){
      |  return vtx+vec4(1.0);
      |}
      |vec4 VERTEX;
      |void vertex(){
      |  VERTEX=def0(VERTEX);
      |}
      |""".stripMargin.trim
    )
  }

  test("Shaders can be create from within a companion (inlined + converted)") {

    inline def modifyVertex: vec4 => Shader[Foo.Env, vec4] =
      (vtx: vec4) =>
        Shader[Foo.Env, vec4] { _ =>
          vtx + vec4(1.0f)
        }

    val actualCode =
      Foo.shaderResult(modifyVertex).toOutput.code

    // DebugAST.toAST(Foo.shader(modifyVertex))
    // println(actualCode)

    assertEquals(
      actualCode,
      s"""
      |vec4 def0(in vec4 vtx){
      |  return vtx+vec4(1.0);
      |}
      |vec4 VERTEX;
      |void vertex(){
      |  VERTEX=def0(VERTEX);
      |}
      |""".stripMargin.trim
    )
  }

  test("Shaders can be create from within a companion (inlined + converted) (more complicated)") {

    inline def circleSdf = (p: vec2, r: Float) => length(p) - r

    inline def calculateColour = (uv: vec2, sdf: Float) =>
      val fill       = vec4(uv, 0.0f, 1.0f)
      val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
      vec4(fill.xyz * fillAmount, fillAmount)

    inline def modifyColor: vec4 => ultraviolet.syntax.Shader[Foo.Env, vec4] =
      _ =>
        Shader[Foo.Env, vec4] { env =>
          // Proxies to external fns
          val _circleSdf: (vec2, Float) => Float      = circleSdf
          val _calculateColour: (vec2, Float) => vec4 = calculateColour

          val sdf = _circleSdf(env.UV - 0.5f, 0.5f)
          _calculateColour(env.UV, sdf)
        }

    val actualCode =
      Foo.shaderResult(modifyColor).toOutput.code

    // DebugAST.toAST(Foo.shader(modifyColor))
    // println(actualCode)

    assertEquals(
      actualCode,
      s"""
      |float def1(in vec2 p,in float r){
      |  return length(p)-r;
      |}
      |vec4 def2(in vec2 uv,in float sdf){
      |  vec4 fill=vec4(uv,0.0,1.0);
      |  float fillAmount=(1.0-step(0.0,sdf))*fill.w;
      |  return vec4(fill.xyz*fillAmount,fillAmount);
      |}
      |vec4 def0(in vec4 val0){
      |  float sdf=def1(UV-0.5,0.5);
      |  return def2(UV,sdf);
      |}
      |vec4 VERTEX;
      |void vertex(){
      |  VERTEX=def0(VERTEX);
      |}
      |""".stripMargin.trim
    )
  }

  test("Shader validation can be disabled to render illegal programs") {

    inline def shader: Shader[Unit, vec4] =
      Shader[Unit, vec4] { env =>

        def foo(): Float =
          def bar(): Float = 1.0f
          bar()

        vec4(vec2(1.0f), 2.0f, 1.0f)
      }

    // interceptMessage doesn't work because it isn't a runtime exception, the error is at compile time.
    // But this is the error we'd normally see.
    // interceptMessage[ShaderError.Validation](
    //   "[ultraviolet] It is not permitted to nest named functions, however, you can declare nested anonymous functions."
    // ) {
    //   shader.toGLSL[WebGL2]
    // }

    val actualCode =
      shader.toGLSL[WebGL2](false).toOutput.code

    // DebugAST.toAST(shader)
    // println(actualCode)

    assertEquals(
      actualCode,
      s"""
      |float foo(){
      |  float bar(){
      |    return 1.0;
      |  }
      |  return bar();
      |}
      |vec4(vec2(1.0),2.0,1.0);
      |""".stripMargin.trim
    )
  }

}

object Foo {
  case class Env(id: Int, UV: vec2)
  case class Env2(id2: Int)

  @nowarn("msg=unused")
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  inline def shader(inline f: vec4 => Shader[Env, vec4]): Shader[Env2, Unit] =
    Shader[Env2] { env =>
      val _f: vec4 => Shader[Env, vec4] = f
      var VERTEX: vec4                  = null
      def vertex: Unit =
        VERTEX = _f(VERTEX).run(Env(0, vec2(0.0f)))
    }

  inline def shaderResult(inline f: vec4 => Shader[Env, vec4]): ShaderResult =
    shader(f).toGLSL[WebGL2]
}
