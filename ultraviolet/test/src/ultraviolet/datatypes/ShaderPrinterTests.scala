package ultraviolet.datatypes

import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class ShaderPrinterTests extends munit.FunSuite {

  test("Printing an AST") {

    val ast =
      ShaderAST.Block(
        List(
          ShaderAST.Val("x", ShaderAST.DataTypes.float(1.0), ShaderAST.DataTypes.ident("float")),
          ShaderAST.Block(
            List(
              ShaderAST.Annotated(
                ShaderAST.DataTypes.ident("const"),
                ShaderAST.Empty(),
                ShaderAST.Val("y", ShaderAST.DataTypes.float(2.0), ShaderAST.DataTypes.ident("float"))
              ),
              ShaderAST.Val("z", ShaderAST.DataTypes.float(3.0), ShaderAST.DataTypes.ident("float"))
            )
          )
        )
      )

    val actual =
      ShaderPrinter.print(ast)

    val expected =
      List(
        "float x=1.0;",
        "const float y=2.0;",
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

        def main: Unit =
          val c: vec4 = texture2D(u_texture2d, v_texcoord);
          env.COLOR = textureCube(u_textureCube, normalize(v_normal)) * c
      }

    // DebugAST.toAST(fragment)

    val webgl1 =
      fragment.toGLSL100.code

    // println(webgl1)

    assertNoDiff(
      webgl1,
      s"""
      |varying vec2 v_texcoord;
      |varying vec3 v_normal;
      |varying vec4 v_color;
      |uniform sampler2D u_texture2d;
      |uniform samplerCube u_textureCube;
      |void main(){
      |  vec4 c=texture2D(u_texture2d,v_texcoord);
      |  COLOR=textureCube(u_textureCube,normalize(v_normal))*c;
      |}
      |""".stripMargin.trim
    )

    val webgl2 =
      fragment.toGLSL300.code

    // println(webgl2)

    assertNoDiff(
      webgl2,
      s"""
      |in vec2 v_texcoord;
      |in vec3 v_normal;
      |out vec4 v_color;
      |uniform sampler2D u_texture2d;
      |uniform samplerCube u_textureCube;
      |void main(){
      |  vec4 c=texture(u_texture2d,v_texcoord);
      |  COLOR=texture(u_textureCube,normalize(v_normal))*c;
      |}
      |""".stripMargin.trim
    )

  }

  test("Print negative symbols") {

    import ShaderAST.*
    import ShaderAST.DataTypes.*

    val ast =
      Neg(Infix("/", ident("x"), ident("y"), ident("x")))

    val actual =
      ShaderPrinter.print(ast)

    // println(actual)

    assertNoDiff(
      actual.mkString("\n"),
      s"""
      |-(x/y)
      |""".stripMargin.trim
    )

  }

  test("Can print if statements at the end of functions") {

    val ast =
      ShaderAST.Block(
        List(
          ShaderAST.Function(
            "move",
            Nil,
            ShaderAST.If(
              ShaderAST.DataTypes.bool(true),
              ShaderAST.Assign(ShaderAST.DataTypes.ident("pos"), ShaderAST.DataTypes.float(10.0f)),
              None
            ),
            ShaderAST.void
          )
        )
      )

    val actual =
      ShaderPrinter.print(ast)

    val expected =
      List(
        "void move(){",
        "  if(true){",
        "    pos=10.0;",
        "  }",
        "}"
      )

    assertEquals(actual, expected)

  }

}
