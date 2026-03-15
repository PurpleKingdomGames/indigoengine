package indigo.render.pipeline.sceneprocessing

import indigo.core.assets.AssetName
import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.Vector2
import indigo.core.events.GlobalEvent
import indigo.core.time.GameTime
import indigo.core.utils.QuickCache
import indigo.render.pipeline.assets.AssetMapping
import indigo.render.pipeline.assets.AtlasId
import indigo.render.pipeline.assets.TextureRefAndOffset
import indigo.render.pipeline.datatypes.DisplayObject
import indigo.render.pipeline.displayprocessing.DisplayObjectConversions
import indigo.scenegraph.Graphic
import indigo.scenegraph.Layer
import indigo.scenegraph.SceneUpdateFragment
import indigo.scenegraph.materials.Material
import indigo.scenegraph.registers.AnimationsRegister
import indigo.scenegraph.registers.BoundaryLocator
import indigo.scenegraph.registers.FontRegister
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.KVP
import indigoengine.shared.collections.mutable
import indigoengine.shared.datatypes.RGBA
import indigoengine.shared.datatypes.Seconds

class SceneProcessorTests extends munit.FunSuite {

  val animationRegister = new AnimationsRegister
  val fontRegister      = new FontRegister
  val boundaryLocator   = new BoundaryLocator(animationRegister, fontRegister)
  val texture =
    new TextureRefAndOffset(AtlasId("texture"), Vector2(100, 100), Vector2.zero, Vector2(200, 100))
  val assetMapping: AssetMapping = new AssetMapping(KVP.empty.add("texture" -> texture))

  val doc = new DisplayObjectConversions(boundaryLocator, animationRegister, fontRegister)

  given QuickCache[Batch[Float]] = QuickCache.empty

  test("makeDisplayLayers - single layer with one graphic") {
    val graphic = Graphic(Rectangle(10, 20, 200, 100), Material.Bitmap(AssetName("texture")))
    val scene   = SceneUpdateFragment(graphic)

    val result = SceneProcessor.makeDisplayLayers(
      scene,
      GameTime.is(Seconds(1)),
      assetMapping,
      256,
      Batch.empty[GlobalEvent],
      (_: GlobalEvent) => (),
      mutable.KVP.empty[DisplayObject],
      doc
    )

    val (layers, cloneBlanks) = result
    assertEquals(layers.length, 1)

    val layer = layers(0)
    assertEquals(layer.entities.length, 1)
    assertEquals(cloneBlanks.size, 0)
    assertEquals(layer.bgColor, RGBA.Zero)
  }

  test("makeDisplayLayers - two separate layers") {
    val graphic1 = Graphic(Rectangle(0, 0, 50, 50), Material.Bitmap(AssetName("texture")))
    val graphic2 = Graphic(Rectangle(100, 100, 50, 50), Material.Bitmap(AssetName("texture")))
    val scene = SceneUpdateFragment(
      Layer(graphic1),
      Layer(graphic2)
    )

    val result = SceneProcessor.makeDisplayLayers(
      scene,
      GameTime.is(Seconds(1)),
      assetMapping,
      256,
      Batch.empty[GlobalEvent],
      (_: GlobalEvent) => (),
      mutable.KVP.empty[DisplayObject],
      doc
    )

    val (layers, _) = result
    assertEquals(layers.length, 2)
    assertEquals(layers(0).entities.length, 1)
    assertEquals(layers(1).entities.length, 1)
  }

  test("makeDisplayLayers - empty scene") {
    val scene = SceneUpdateFragment.empty

    val result = SceneProcessor.makeDisplayLayers(
      scene,
      GameTime.is(Seconds(1)),
      assetMapping,
      256,
      Batch.empty[GlobalEvent],
      (_: GlobalEvent) => (),
      mutable.KVP.empty[DisplayObject],
      doc
    )

    val (layers, _) = result
    assertEquals(layers.length, 0)
  }
}
