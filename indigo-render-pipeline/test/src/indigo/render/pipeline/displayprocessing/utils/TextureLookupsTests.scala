package indigo.render.pipeline.displayprocessing.utils

import indigo.core.datatypes.Vector2
import indigo.render.pipeline.assets.AtlasId
import indigo.render.pipeline.assets.TextureRefAndOffset

class TextureLookupsTests extends munit.FunSuite {

  def ref(atlas: String): TextureRefAndOffset =
    TextureRefAndOffset(AtlasId(atlas), Vector2(64, 64), Vector2.zero, Vector2(16, 16))

  test("validateChannelAtlases - all channels on the same atlas passes") {
    val result =
      TextureLookups.validateChannelAtlases(
        Some(ref("atlas_0")),
        Some(ref("atlas_0")),
        Some(ref("atlas_0")),
        Some(ref("atlas_0"))
      )

    assertEquals(result, None)
  }

  test("validateChannelAtlases - only channel 0 present passes") {
    val result =
      TextureLookups.validateChannelAtlases(Some(ref("atlas_0")), None, None, None)

    assertEquals(result, None)
  }

  test("validateChannelAtlases - no channels at all passes") {
    val result =
      TextureLookups.validateChannelAtlases(None, None, None, None)

    assertEquals(result, None)
  }

  test("validateChannelAtlases - channel 1 on a different atlas fails with guidance") {
    val result =
      TextureLookups.validateChannelAtlases(
        Some(ref("atlas_0")),
        Some(ref("atlas_1")),
        None,
        None
      )

    assert(result.isDefined)

    val msg = result.get

    assert(msg.contains("atlas_0"), msg)
    assert(msg.contains("atlas_1"), msg)
    assert(msg.contains("AssetType.Tagged"), msg)
  }

  test("validateChannelAtlases - reports every mismatched channel") {
    val result =
      TextureLookups.validateChannelAtlases(
        Some(ref("atlas_0")),
        Some(ref("atlas_0")),
        Some(ref("atlas_1")),
        Some(ref("atlas_2"))
      )

    assert(result.isDefined)

    val msg = result.get

    assert(msg.contains("atlas_0"), msg)
    assert(msg.contains("atlas_1"), msg)
    assert(msg.contains("atlas_2"), msg)
  }

  test("validateChannelAtlases - a lone channel 2 passes") {
    val result =
      TextureLookups.validateChannelAtlases(None, None, Some(ref("atlas_1")), None)

    assert(result.isEmpty)
  }

}
