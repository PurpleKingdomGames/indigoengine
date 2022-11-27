package ultraviolet.datatypes

import ultraviolet.syntax.*

class ShaderPrinterTests extends munit.FunSuite {

  test("The default printer can print an AST") {

    val ast =
      ShaderAST.Block(
        List(
          ShaderAST.Val("x", ShaderAST.DataTypes.float(1.0), Option("float")),
          ShaderAST.Block(
            List(
              ShaderAST.Annotated(
                ShaderAST.DataTypes.ident("const"),
                ShaderAST.Empty(),
                ShaderAST.Val("y", ShaderAST.DataTypes.float(2.0), Option("float"))
              ),
              ShaderAST.Val("z", ShaderAST.DataTypes.float(3.0), Option("float"))
            )
          )
        )
      )

    val actual =
      ShaderPrinter.print[ShaderPrinter.WebGL2](ast)

    val expected =
      List(
        "float x=1.0;",
        "const float y=2.0;",
        "float z=3.0;"
      )

    assertEquals(actual, expected)

  }

  test("A custom printer can partially match to modify the output of the print") {

    given ShaderPrinter[Any] = new ShaderPrinter[Any] {
      def isValid(
          inType: Option[String],
          outType: Option[String],
          headers: List[ShaderAST],
          functions: List[ShaderAST],
          body: ShaderAST
      ): ShaderValid = ShaderValid.Valid

      def transformer: PartialFunction[ShaderAST, ShaderAST] = {
        case ShaderAST.Val("x", ShaderAST.DataTypes.float(1.0), Some("float")) =>
          ShaderAST.Val("xx", ShaderAST.DataTypes.float(100.0), Some("float"))
      }

      def printer: PartialFunction[ShaderAST, List[String]] = {
        case ShaderAST.Val("y", ShaderAST.DataTypes.float(2.0), Some("float")) =>
          List("float foo")
      }
    }

    val ast =
      ShaderAST.Block(
        List(
          ShaderAST.Val("x", ShaderAST.DataTypes.float(1.0), Option("float")),
          ShaderAST.Block(
            List(
              ShaderAST.Annotated(
                ShaderAST.DataTypes.ident("const"),
                ShaderAST.Empty(),
                ShaderAST.Val("y", ShaderAST.DataTypes.float(2.0), Option("float"))
              ),
              ShaderAST.Val("z", ShaderAST.DataTypes.float(3.0), Option("float"))
            )
          )
        )
      )

    val actual =
      ShaderPrinter.print(ast)

    val expected =
      List(
        "float xx=100.0;",
        "const float foo;",
        "float z=3.0;"
      )

    assertEquals(actual, expected)

  }

  test("Can output WebGL 1.0 and 2.0") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    case class Env(var COLOR: vec4)

    @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
    inline def fragment =
      Shader[Env, Unit] { env =>
        @in val v_texcoord: vec2   = null
        @in val v_normal: vec3     = null
        @out val v_color: vec4     = null
        @uniform val u_texture2d   = sampler2D
        @uniform val u_textureCube = samplerCube

        val c: vec4 = texture2D(u_texture2d, v_texcoord);
        env.COLOR = textureCube(u_textureCube, normalize(v_normal)) * c
      }

    // DebugAST.toAST(fragment)

    val webgl1 =
      fragment.toGLSL[ShaderPrinter.WebGL1].code

    // println(webgl1)

    assertEquals(
      webgl1,
      s"""
      |varying vec2 v_texcoord;
      |varying vec3 v_normal;
      |varying vec4 v_color;
      |uniform sampler2D u_texture2d;
      |uniform samplerCube u_textureCube;
      |vec4 c=texture2D(u_texture2d,v_texcoord);
      |COLOR=textureCube(u_textureCube,normalize(v_normal))*c;
      |""".stripMargin.trim
    )

    val webgl2 =
      fragment.toGLSL[ShaderPrinter.WebGL2].code

    // println(webgl2)

    assertEquals(
      webgl2,
      s"""
      |in vec2 v_texcoord;
      |in vec3 v_normal;
      |out vec4 v_color;
      |uniform sampler2D u_texture2d;
      |uniform samplerCube u_textureCube;
      |vec4 c=texture(u_texture2d,v_texcoord);
      |COLOR=texture(u_textureCube,normalize(v_normal))*c;
      |""".stripMargin.trim
    )

  }

  test("Print negative symbols") {

    import ShaderAST.*
    import ShaderAST.DataTypes.*

    val ast =
      Neg(Infix("/", ident("x"), ident("y"), Some(ident("x"))))

    val actual =
      ShaderPrinter.print[WebGL2](ast)

    // println(actual)

    assertEquals(
      actual.mkString("\n"),
      s"""
      |-(x/y)
      |""".stripMargin.trim
    )

  }

}
