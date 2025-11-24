package indigo.scenegraph

import indigo.core.assets.AssetName
import indigo.core.datatypes.Point
import indigo.core.datatypes.Size
import indigo.scenegraph.materials.Material
import indigo.core.time.FPS
import indigoengine.shared.datatypes.Seconds

class ClipTests extends munit.FunSuite {

  val clip = Clip(
    Point.zero,
    Size(32),
    ClipSheet(10, FPS(10)),
    Material.Bitmap(AssetName("test"))
  )

  test("Calling reverse changes play direction (forward)") {
    val actual =
      clip.reverse.playMode.direction

    val expected =
      ClipPlayDirection.Backward

    assertEquals(actual, expected)
  }

  test("Calling reverse changes play direction (backwards)") {
    val actual =
      clip
        .withPlayMode(ClipPlayMode.PlayOnce(ClipPlayDirection.Backward, Seconds(1)))
        .reverse
        .playMode
        .direction

    val expected =
      ClipPlayDirection.Forward

    assertEquals(actual, expected)
  }

  test("Calling reverse changes play direction and back again") {
    val actual =
      clip.reverse.reverse.playMode.direction

    val expected =
      ClipPlayDirection.Forward

    assertEquals(actual, expected)
  }

}
