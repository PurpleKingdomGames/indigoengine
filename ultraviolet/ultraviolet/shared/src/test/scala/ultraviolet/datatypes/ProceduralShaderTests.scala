package ultraviolet.datatypes

import ultraviolet.macros.ShaderMacros

class ProceduralShaderTests extends munit.FunSuite {
  import ShaderAST.*
  import ShaderAST.DataTypes.*

  test("exists: whole result == search") {

    val actual =
      ProceduralShader(Nil, Nil, vec4(List(float(1), float(1), float(0), float(1))))
        .exists(vec4(List(float(1), float(1), float(0), float(1))))

    assert(actual)

  }

  test("exists: component in vec4") {

    val actual =
      ProceduralShader(Nil, Nil, vec4(List(float(1), float(1), float(0), float(1))))
        .exists(float(1))

    assert(actual)

  }

  test("exists") {

    val actual =
      ProceduralShaderSamples.sample1.exists(vec4(List(float(1), float(1), float(0), float(1))))

    assert(actual)

  }

  test("find") {
    import ultraviolet.syntax.*

    case class FragEnv(UV: vec2)

    inline def fragment: Shader[FragEnv, Float] =
      Shader { env =>
        def circleSdf(p: vec2, r: Float): Float =
          length(p) - r

        val x = vec2(1.0, 2.0)

        circleSdf(env.UV, 3.0)
      }

    val actual =
      ShaderMacros.toAST(fragment).find {
        case r @ ShaderAST.DataTypes.vec2(_) => true
        case _                               => false
      }

    actual match
      case Some(ShaderAST.DataTypes.vec2(List(ShaderAST.DataTypes.float(x), ShaderAST.DataTypes.float(y)))) =>
        assertEquals(x, 1.0f)
        assertEquals(y, 2.0f)

      case _ =>
        fail("failed")

  }

  test("findAll") {
    import ultraviolet.syntax.*

    case class FragEnv(UV: vec2)

    inline def fragment: Shader[FragEnv, vec2] =
      Shader { env =>
        val x = vec2(100.0, 200.0)

        def circleSdf(p: vec2, r: Float): Float =
          length(p) - r

        val y = vec2(1.0, 2.0)

        circleSdf(env.UV, 3.0)

        vec2(10.0, 20.0)
      }

    val actual =
      ShaderMacros.toAST(fragment).findAll {
        case r @ ShaderAST.DataTypes.vec2(_) => true
        case _                               => false
      }

    assertEquals(actual.length, 3)

    actual match
      case List(
            ShaderAST.DataTypes.vec2(List(ShaderAST.DataTypes.float(x1), ShaderAST.DataTypes.float(y1))),
            ShaderAST.DataTypes.vec2(List(ShaderAST.DataTypes.float(x2), ShaderAST.DataTypes.float(y2))),
            ShaderAST.DataTypes.vec2(List(ShaderAST.DataTypes.float(x3), ShaderAST.DataTypes.float(y3)))
          ) =>
        assertEquals(x1, 100.0f)
        assertEquals(y1, 200.0f)
        assertEquals(x2, 1.0f)
        assertEquals(y2, 2.0f)
        assertEquals(x3, 10.0f)
        assertEquals(y3, 20.0f)

      case _ =>
        fail("failed")

  }

  test("Output & Metadata") {
    import ultraviolet.syntax.*

    case class UBO1(position: vec2, color: vec4)
    case class UBO2(direction: vec3, alpha: highp[Float])

    @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
    inline def fragment =
      Shader {
        @uniform val TIME: Float = 0.0f;
        ubo[UBO1]
        @uniform val UV: vec2 = null;
        @in val v_tex: vec2   = null;
        ubo[UBO2]
        @out val v_coords: vec3 = null;

        vec4(1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2]

    val expectedCode =
      s"""
      |layout (std140) uniform UBO1 {
      |  vec2 position;
      |  vec4 color;
      |};
      |layout (std140) uniform UBO2 {
      |  vec3 direction;
      |  highp float alpha;
      |};
      |uniform float TIME;
      |uniform vec2 UV;
      |in vec2 v_tex;
      |out vec3 v_coords;
      |vec4(1.0);
      |""".stripMargin.trim

    val expectedMetadata =
      ShaderMetadata(
        uniforms = List(
          ShaderField("TIME", "float"),
          ShaderField("UV", "vec2")
        ),
        ubos = List(
          UBODef(
            "UBO1",
            List(
              UBOField(None, "vec2", "position"),
              UBOField(None, "vec4", "color")
            )
          ),
          UBODef(
            "UBO2",
            List(
              UBOField(None, "vec3", "direction"),
              UBOField(Some("highp"), "float", "alpha")
            )
          )
        ),
        varyings = List(
          ShaderField("v_tex", "vec2"),
          ShaderField("v_coords", "vec3")
        )
      )

    assertEquals(actual.code, expectedCode)
    assertEquals(actual.metadata, expectedMetadata)

  }

}

object ProceduralShaderSamples:

  import ShaderAST.*
  import ShaderAST.DataTypes.*

  val sample1 =
    ProceduralShader(
      List(
        Function(
          "fn0",
          List(ShaderAST.DataTypes.ident("") -> "env"),
          Block(List(Block(List(vec4(List(float(1), float(1), float(0), float(1))))))),
          ShaderAST.DataTypes.ident("vec4")
        )
      ),
      Nil,
      Block(
        List(Block(List(Block(List(CallFunction("fn0", Nil, ShaderAST.DataTypes.ident("vec4")), Empty())))))
      )
    )
