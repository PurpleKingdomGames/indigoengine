package indigo.render.pipeline.displayprocessing

import indigo.core.assets.AssetName
import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.Vector2
import indigo.core.events.GlobalEvent
import indigo.core.time.GameTime
import indigo.render.pipeline.assets.AssetMapping
import indigo.render.pipeline.assets.AtlasId
import indigo.render.pipeline.assets.TextureRefAndOffset
import indigo.render.pipeline.datatypes.DisplayCloneBatch
import indigo.render.pipeline.datatypes.DisplayCloneTiles
import indigo.render.pipeline.datatypes.DisplayGroup
import indigo.render.pipeline.datatypes.DisplayMutants
import indigo.render.pipeline.datatypes.DisplayObject
import indigo.render.pipeline.datatypes.DisplayTextLetters
import indigo.scenegraph.Graphic
import indigo.scenegraph.SceneNode
import indigo.scenegraph.materials.Material
import indigo.scenegraph.registers.AnimationsRegister
import indigo.scenegraph.registers.BoundaryLocator
import indigo.scenegraph.registers.FontRegister
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.KVP
import indigoengine.shared.collections.mutable
import indigoengine.shared.datatypes.Seconds

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class DisplayObjectConversionsTests extends munit.FunSuite {

  val graphic: Graphic[?] =
    Graphic(Rectangle(10, 20, 200, 100), Material.Bitmap(AssetName("texture")))

  val animationRegister = new AnimationsRegister
  val fontRegister      = new FontRegister
  val boundaryLocator   = new BoundaryLocator(animationRegister, fontRegister)
  val texture = new TextureRefAndOffset(AtlasId("texture"), Vector2(100, 100), Vector2.zero, Vector2(200, 100))
  val assetMapping: AssetMapping = new AssetMapping(KVP.empty.add("texture" -> texture))

  val cloneBlankMapping: mutable.KVP[DisplayObject] = mutable.KVP.empty[DisplayObject]

  val doc = new DisplayObjectConversions(
    boundaryLocator,
    animationRegister,
    fontRegister
  )

  def convert(node: SceneNode): DisplayObject = {
    doc.purgeCaches()

    doc
      .processSceneNodes(
        Batch(node),
        GameTime.is(Seconds(1)),
        assetMapping,
        cloneBlankMapping,
        256,
        Batch[GlobalEvent](),
        (_: GlobalEvent) => ()
      )
      ._1
      .head match {
      case _: DisplayCloneBatch =>
        throw new Exception("failed (DisplayCloneBatch)")

      case _: DisplayCloneTiles =>
        throw new Exception("failed (DisplayCloneTiles)")

      case _: DisplayMutants =>
        throw new Exception("failed (DisplayMutants)")

      case _: DisplayTextLetters =>
        throw new Exception("failed (DisplayTextLetters)")

      case _: DisplayGroup =>
        throw new Exception("failed (DisplayGroup)")

      case d: DisplayObject =>
        d
    }
  }

  test("convert a graphic to a display object") {
    val actual: DisplayObject =
      convert(graphic)

    assertEquals(actual.x, 10.0f)
    assertEquals(actual.y, 20.0f)
    assertEquals(actual.width, 200.0f)
    assertEquals(actual.height, 100.0f)
  }

}
