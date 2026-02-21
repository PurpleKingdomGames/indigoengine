package indigo.scenegraph

import indigo.core.datatypes.Size
import indigo.shaders.ShaderData
import indigo.shaders.ShaderId

class BlankEntityTests extends munit.FunSuite:

  val shaderData: ShaderData =
    ShaderData(ShaderId("test-shader"))

  test("resizeTo(Size) sets the size") {
    val actual =
      BlankEntity(shaderData).resizeTo(Size(64, 64)).size

    assertEquals(actual, Size(64, 64))
  }

  test("resizeTo(Int, Int) sets the size") {
    val actual =
      BlankEntity(shaderData).resizeTo(64, 64).size

    assertEquals(actual, Size(64, 64))
  }

  test("withSize sets the size") {
    val actual =
      BlankEntity(shaderData).withSize(Size(64, 64)).size

    assertEquals(actual, Size(64, 64))
  }

  test("resizeTo(Size) and BlankEntity(Size, shaderData) produce equivalent sizes") {
    val viaConstructor =
      BlankEntity(Size(64, 64), shaderData).size

    val viaResizeTo =
      BlankEntity(shaderData).resizeTo(Size(64, 64)).size

    assertEquals(viaResizeTo, viaConstructor)
  }

  test("resizeTo(Int, Int) and BlankEntity(Size, shaderData) produce equivalent sizes") {
    val viaConstructor =
      BlankEntity(Size(64, 64), shaderData).size

    val viaResizeTo =
      BlankEntity(shaderData).resizeTo(64, 64).size

    assertEquals(viaResizeTo, viaConstructor)
  }
