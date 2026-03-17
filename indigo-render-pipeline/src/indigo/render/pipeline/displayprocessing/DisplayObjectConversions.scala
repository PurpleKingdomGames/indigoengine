package indigo.render.pipeline.displayprocessing

import indigo.core.animation.AnimationRef
import indigo.core.datatypes.Vector2
import indigo.core.events.GlobalEvent
import indigo.core.time.GameTime
import indigo.core.utils.QuickCache
import indigo.render.pipeline.assets.AssetMapping
import indigo.render.pipeline.assets.TextureRefAndOffset
import indigo.render.pipeline.datatypes.DisplayCloneBatch
import indigo.render.pipeline.datatypes.DisplayCloneTiles
import indigo.render.pipeline.datatypes.DisplayEntity
import indigo.render.pipeline.datatypes.DisplayObject
import indigo.render.pipeline.datatypes.SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
import indigo.scenegraph.CloneBlank
import indigo.scenegraph.CloneTileData
import indigo.scenegraph.DependentNode
import indigo.scenegraph.RenderNode
import indigo.scenegraph.SceneNode
import indigo.scenegraph.registers.AnimationsRegister
import indigo.scenegraph.registers.BoundaryLocator
import indigo.scenegraph.registers.FontRegister
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.mutable

final class DisplayObjectConversions(
    boundaryLocator: BoundaryLocator,
    animationsRegister: AnimationsRegister,
    fontRegister: FontRegister
):

  // Per asset load
  implicit private val textureRefAndOffsetCache: QuickCache[TextureRefAndOffset] = QuickCache.empty
  implicit private val vector2Cache: QuickCache[Vector2]                         = QuickCache.empty
  implicit private val frameCache: QuickCache[SpriteSheetFrameCoordinateOffsets] = QuickCache.empty
  implicit private val listDoCache: QuickCache[Batch[DisplayEntity]]             = QuickCache.empty
  implicit private val cloneBatchCache: QuickCache[DisplayCloneBatch]            = QuickCache.empty
  implicit private val cloneTilesCache: QuickCache[DisplayCloneTiles]            = QuickCache.empty
  implicit private val uniformsCache: QuickCache[Batch[Float]]                   = QuickCache.empty
  implicit private val textCloneTileDataCache: QuickCache[Batch[CloneTileData]]  = QuickCache.empty
  implicit private val displayObjectCache: QuickCache[DisplayObject]             = QuickCache.empty
  implicit private val displayObjectBatchCache: QuickCache[Batch[DisplayObject]] = QuickCache.empty

  // Per frame
  implicit private val perFrameAnimCache: QuickCache[Option[AnimationRef]] = QuickCache.empty

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var nodeConvertor: DisplayObjectConversionVisitor =
    null

  def prepareToProcessFrame(
      gameTime: GameTime,
      assetMapping: AssetMapping,
      maxBatchSize: Int,
      inputEvents: => Batch[GlobalEvent],
      sendEvent: GlobalEvent => Unit
  ): Unit =
    nodeConvertor = new DisplayObjectConversionVisitor(
      boundaryLocator,
      animationsRegister,
      fontRegister,
      gameTime,
      assetMapping,
      maxBatchSize,
      inputEvents,
      sendEvent
    )

  def updateConvertorCloneBlanks(blanks: mutable.KVP[DisplayObject]): Unit =
    nodeConvertor.setCloneBlanks(blanks)

  // Called on asset load/reload to account for atlas rebuilding etc.
  def purgeCaches(): Unit = {
    textureRefAndOffsetCache.purgeAllNow()
    vector2Cache.purgeAllNow()
    frameCache.purgeAllNow()
    listDoCache.purgeAllNow()
    cloneBatchCache.purgeAllNow()
    cloneTilesCache.purgeAllNow()
    uniformsCache.purgeAllNow()
    textCloneTileDataCache.purgeAllNow()
    displayObjectCache.purgeAllNow()
    perFrameAnimCache.purgeAllNow()
  }

  def purgeEachFrame(): Unit =
    perFrameAnimCache.purgeAllNow()

  def processSceneNodes(
      sceneNodes: Batch[SceneNode],
      inputEvents: => Batch[GlobalEvent],
      sendEvent: GlobalEvent => Unit
  ): DisplayConversionResults =
    val entities = mutable.Batch.empty[DisplayEntity]
    val clones   = mutable.Batch.empty[DisplayConversionResultClone]

    sceneNodes.foreach: child =>
      child match
        case n: RenderNode[_] =>
          val nn = n.asInstanceOf[n.Out]
          if n.eventHandlerEnabled then
            inputEvents.foreach { e =>
              n.eventHandler((nn, e)).foreach { ee =>
                sendEvent(ee)
              }
            }

        case n: DependentNode[_] =>
          val nn = n.asInstanceOf[n.Out]
          if n.eventHandlerEnabled then
            inputEvents.foreach { e =>
              n.eventHandler((nn, e)).foreach { ee =>
                sendEvent(ee)
              }
            }

      val res = child.accept(nodeConvertor)
      entities.append(res.displayEntity)
      res.clones.foreach(clones.append)

    DisplayConversionResults(
      entities.toBatch,
      clones.toBatch
    )

  def cloneBlankToDisplayObject(
      blank: CloneBlank
  ): Option[DisplayObject] =
    val node = blank.cloneable()

    if node.isInstanceOf[SceneNode] then
      node.asInstanceOf[SceneNode].accept(nodeConvertor).displayEntity match
        case d: DisplayObject => Some(d)
        case _                => None
    else None

final class DisplayConversionResults(
    val displayEntities: Batch[DisplayEntity],
    val clones: Batch[DisplayConversionResultClone]
)
final class DisplayConversionResult(val displayEntity: DisplayEntity, val clones: Batch[DisplayConversionResultClone])
final class DisplayConversionResultClone(val id: String, val displayObject: DisplayObject)
