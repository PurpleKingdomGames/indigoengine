package indigo.shaders

import indigo.*
import indigo.syntax.shaders.*
import ultraviolet.syntax.*

import scala.annotation.nowarn

/** This suite isn't connected to any code directly in this module, but it allows us to make a realistic example of
  * deriving UBOs using the full Indigo APIs.
  */
class ShaderUBOAcceptanceTests extends munit.FunSuite {

  test("UniformBlock construction") {

    val actualUniformBlocks =
      ShaderData(CustomShader.shader.id)
        .addUniformData(ArrayData(array[4, Float](0.0f, 1.0f, 1.0f, 1.0f)))
        .addUniformData(CustomData(vec4(1.0f, 0.0f, 1.0f, 1.0f), 10.0f, 20.0f))
        .uniformBlocks
        .toList

    val expectedUniformBlocks =
      Batch(
        UniformBlock(
          UniformBlockName("ArrayData"),
          Uniform("ANOTHER_COLOR") -> ShaderPrimitive.array(4, Array(0.0f, 1.0f, 1.0f, 1.0f))
        ),
        UniformBlock(
          UniformBlockName("CustomData"),
          Uniform("CUSTOM_COLOR") -> ShaderPrimitive.vec4(1.0f, 0.0f, 1.0f, 1.0f),
          Uniform("x")            -> ShaderPrimitive.float(10.0f),
          Uniform("y")            -> ShaderPrimitive.float(20.0f)
        )
      ).toList

    assertEquals(actualUniformBlocks, expectedUniformBlocks)
  }

  test("Shader code") {
    val actual =
      CustomShader.fragment.toGLSL300.code

    val expected =
      """
      |layout (std140) uniform CustomData {
      |  vec4 CUSTOM_COLOR;
      |  float x;
      |  float y;
      |};
      |layout (std140) uniform ArrayData {
      |  float[4] ANOTHER_COLOR;
      |};
      |vec4 fragment(in vec4 color){
      |  vec4 magenta=CUSTOM_COLOR;
      |  vec4 cyan=vec4(ANOTHER_COLOR[0],ANOTHER_COLOR[1],ANOTHER_COLOR[2],ANOTHER_COLOR[3]);
      |  vec4 mByX=vec4(magenta.xyz*UV.x,UV.x);
      |  vec4 cByY=vec4(cyan.xyz*UV.y,UV.y);
      |  return mix(mByX,cByY,mByX.a);
      |}
      |""".stripMargin.trim

    assertNoDiff(actual, expected)
  }

  test("toUniformBlock UV type conversion") {

    val actual =
      UniformBlock(
        UniformBlockName("Foo"),
        Uniform("x") -> 10.0f.toShaderPrimitive,
        Uniform("y") -> 20.0f.toShaderPrimitive,
        Uniform("z") -> array[3, Float](1.0f, 2.0f, 3.0f).toShaderPrimitive
      )

    val expected =
      UniformBlock(
        UniformBlockName("Foo"),
        Uniform("x") -> ShaderPrimitive.float(10),
        Uniform("y") -> ShaderPrimitive.float(20.0f),
        Uniform("z") -> ShaderPrimitive.array(3, Array(1.0f, 2.0f, 3.0f))
      )

    assertEquals(actual.uniformHash, expected.uniformHash)
    assertEquals(
      actual.uniforms.map(_._2.toBatch.map(_.toString).mkString(", ")),
      expected.uniforms.map(_._2.toBatch.map(_.toString).mkString(", "))
    )
  }

}

trait ICustomData:
  val CUSTOM_COLOR: vec4 = vec4(0.0f)
  val x: Float           = 0.0f
  val y: Float           = 0.0f
case class CustomData(
    override val CUSTOM_COLOR: vec4,
    override val x: Float,
    override val y: Float
) extends ICustomData derives ToUniformBlock

trait IArrayData:
  val ANOTHER_COLOR: array[4, Float] = array[4, Float]()
case class ArrayData(override val ANOTHER_COLOR: array[4, Float]) extends IArrayData derives ToUniformBlock

trait FakeFragEnv:
  val UV: vec2 = vec2(0.0f)

type Env = ICustomData & IArrayData & FakeFragEnv
object Env:
  def ref: Env =
    new ICustomData with IArrayData with FakeFragEnv

object CustomShader:

  val shader: ShaderProgram =
    UltravioletShader.entityFragment(
      ShaderId("shader"),
      EntityShader.fragment[Env](fragment, Env.ref)
    )

  @nowarn("msg=unused")
  inline def fragment: Shader[Env, Unit] =
    Shader[Env] { env =>
      ubo[CustomData]
      ubo[ArrayData]

      def fragment(color: vec4): vec4 =
        val magenta = env.CUSTOM_COLOR
        val cyan = vec4(
          env.ANOTHER_COLOR(0),
          env.ANOTHER_COLOR(1),
          env.ANOTHER_COLOR(2),
          env.ANOTHER_COLOR(3)
        )

        val mByX = vec4(magenta.xyz * env.UV.x, env.UV.x)
        val cByY = vec4(cyan.xyz * env.UV.y, env.UV.y)

        mix(mByX, cByY, mByX.a)
    }
