package indigo.shaders.library

import indigo.shaders.StandardShaders
import ultraviolet.syntax.*

class ClipTests extends munit.FunSuite {

  // Mirrors the cpu-side mapping in indigo.scenegraph.Clip.toGraphic
  def expectedCell(frame: Int, wrapAt: Int, arrangement: Int): vec2 =
    if arrangement == 1 then
      vec2(
        (frame / wrapAt).toFloat,
        (frame % wrapAt).toFloat
      )
    else
      vec2(
        (frame % wrapAt).toFloat,
        (frame / wrapAt).toFloat
      )

  test("frameToSheetCell agrees with the cpu-side mapping for all sheet widths") {
    for
      wrapAt      <- 2 to 16
      arrangement <- 0 to 1
      frame       <- 0 to 64
    do
      assertEquals(
        Clip.frameToSheetCell(frame, wrapAt, arrangement),
        expectedCell(frame, wrapAt, arrangement),
        clue = s"frame: $frame, wrapAt: $wrapAt, arrangement: $arrangement"
      )
  }

  test("frameToSheetCell wraps a 7 column sheet on the multiples of 7") {
    val actual =
      (0 to 21).toList.map(f => Clip.frameToSheetCell(f, 7, 0))

    assertEquals(actual(0), vec2(0.0f, 0.0f))
    assertEquals(actual(6), vec2(6.0f, 0.0f))
    assertEquals(actual(7), vec2(0.0f, 1.0f))
    assertEquals(actual(14), vec2(0.0f, 2.0f))
    assertEquals(actual(21), vec2(0.0f, 3.0f))
  }

  test("frameToSheetCell survives a zero wrap width") {
    assertEquals(Clip.frameToSheetCell(3, 0, 0), vec2(0.0f, 3.0f))
  }

  test("frameToSheetCell clamps negative frames to the first cell") {
    assertEquals(Clip.frameToSheetCell(-1, 7, 0), vec2(0.0f, 0.0f))
    assertEquals(Clip.frameToSheetCell(-1, 7, 1), vec2(0.0f, 0.0f))
  }

  test("the clip vertex shaders do not use mod for the cell lookup") {
    val shaders =
      List(
        "BitmapClip"          -> StandardShaders.BitmapClip,
        "LitBitmapClip"       -> StandardShaders.LitBitmapClip,
        "ImageEffectsClip"    -> StandardShaders.ImageEffectsClip,
        "LitImageEffectsClip" -> StandardShaders.LitImageEffectsClip
      )

    shaders.foreach { case (name, shader) =>
      assert(!shader.vertex.code.contains("mod("), clue = s"$name vertex shader contains mod(")
    }
  }

  test("the clip vertex shader picks its sheet cell with integer arithmetic") {
    val actual =
      StandardShaders.BitmapClip.vertex.code.replace("\r\n", "\n")

    assert(clue(actual).contains("int row=f/w;"))
    assert(clue(actual).contains("int col=f-(row*w);"))
    assert(clue(actual).contains("return tick-((tick/totalFrames)*totalFrames);"))
  }

}
