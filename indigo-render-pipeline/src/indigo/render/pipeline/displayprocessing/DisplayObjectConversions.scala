package indigo.render.pipeline.displayprocessing

import indigo.core.animation.AnimationRef
import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.TextAlignment
import indigo.core.datatypes.Vector2
import indigo.core.events.GlobalEvent
import indigo.core.time.GameTime
import indigo.core.utils.IndigoLogger
import indigo.core.utils.QuickCache
import indigo.render.pipeline.assets.AssetMapping
import indigo.render.pipeline.assets.TextureRefAndOffset
import indigo.render.pipeline.datatypes.DisplayCloneBatch
import indigo.render.pipeline.datatypes.DisplayCloneTiles
import indigo.render.pipeline.datatypes.DisplayEntity
import indigo.render.pipeline.datatypes.DisplayGroup
import indigo.render.pipeline.datatypes.DisplayObject
import indigo.render.pipeline.datatypes.DisplayTextLetters
import indigo.render.pipeline.datatypes.SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
import indigo.render.pipeline.displayprocessing.utils.*
import indigo.scenegraph.CloneBatch
import indigo.scenegraph.CloneBlank
import indigo.scenegraph.CloneTileData
import indigo.scenegraph.CloneTiles
import indigo.scenegraph.DependentNode
import indigo.scenegraph.EntityNode
import indigo.scenegraph.Graphic
import indigo.scenegraph.Group
import indigo.scenegraph.Mutants
import indigo.scenegraph.RenderNode
import indigo.scenegraph.SceneNode
import indigo.scenegraph.Shape
import indigo.scenegraph.Sprite
import indigo.scenegraph.Text
import indigo.scenegraph.TextLine
import indigo.scenegraph.registers.AnimationsRegister
import indigo.scenegraph.registers.BoundaryLocator
import indigo.scenegraph.registers.FontRegister
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.mutable
import indigoengine.shared.datatypes.Radians

final class DisplayObjectConversions(
    boundaryLocator: BoundaryLocator,
    animationsRegister: AnimationsRegister,
    fontRegister: FontRegister
):

  // TODO: Use static explicit QuickCache instances to remove implicit search.
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
      gameTime: GameTime,
      assetMapping: AssetMapping,
      cloneBlankDisplayObjects: => mutable.KVP[DisplayObject],
      maxBatchSize: Int,
      inputEvents: => Batch[GlobalEvent],
      sendEvent: GlobalEvent => Unit
  ): (Batch[DisplayEntity], Batch[(String, DisplayObject)]) =
    val f =
      sceneNodeToDisplayObject(
        gameTime,
        assetMapping,
        cloneBlankDisplayObjects,
        maxBatchSize,
        inputEvents,
        sendEvent
      )

    val l = sceneNodes.map { node =>
      node match
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

      f(node)
    }

    (l.map(_._1), l.foldLeft(Batch[(String, DisplayObject)]())(_ ++ _._2))

  def sceneNodeToDisplayObject(
      gameTime: GameTime,
      assetMapping: AssetMapping,
      cloneBlankDisplayObjects: => mutable.KVP[DisplayObject],
      maxBatchSize: Int,
      inputEvents: => Batch[GlobalEvent],
      sendEvent: GlobalEvent => Unit
  )(sceneNode: SceneNode): (DisplayEntity, Batch[(String, DisplayObject)]) =
    val noClones = Batch[(String, DisplayObject)]()
    sceneNode match {
      case x: Graphic[_] =>
        (GraphicConversion.graphicToDisplayObject(x, assetMapping), noClones)

      case s: Shape[_] =>
        (ShapeConversion.shapeToDisplayObject(s), noClones)

      case s: EntityNode[_] =>
        (EntityNodeConversion.sceneEntityToDisplayObject(s, assetMapping), noClones)

      case c: CloneBatch =>
        (
          cloneBlankDisplayObjects.get(c.id.toString) match {
            case None =>
              DisplayGroup.empty

            case Some(_) =>
              CloneBatchConversion.cloneBatchDataToDisplayEntities(c)
          },
          noClones
        )

      case c: CloneTiles =>
        (
          cloneBlankDisplayObjects.get(c.id.toString) match {
            case None =>
              DisplayGroup.empty

            case Some(_) =>
              CloneTilesConversion.cloneTilesDataToDisplayEntities(c)
          },
          noClones
        )

      case c: Mutants =>
        (
          cloneBlankDisplayObjects.get(c.id.toString) match {
            case None =>
              DisplayGroup.empty

            case Some(_) =>
              MutantConversion.mutantsToDisplayEntities(c)
          },
          noClones
        )

      case g: Group =>
        val children =
          processSceneNodes(
            g.children,
            gameTime,
            assetMapping,
            cloneBlankDisplayObjects,
            maxBatchSize,
            inputEvents,
            sendEvent
          )
        (
          DisplayGroup(
            GroupConversion.groupToMatrix(g),
            children._1
          ),
          children._2
        )

      case x: Sprite[_] =>
        val animation = QuickCache("anim-" + x.bindingKey + x.animationKey + x.animationActions.hashCode.toString) {
          animationsRegister.fetchAnimationForSprite(gameTime, x.bindingKey, x.animationKey, x.animationActions)
        }

        (
          animation match {
            case None =>
              IndigoLogger.errorOnce(s"Cannot render Sprite, missing Animations with key: ${x.animationKey.toString()}")
              DisplayGroup.empty

            case Some(anim) =>
              SpriteConversion.spriteToDisplayObject(boundaryLocator, x, assetMapping, anim)
          },
          noClones
        )

      case x: Text[_] if !(x.rotation ~== Radians.zero) =>
        val alignmentOffsetX: Rectangle => Int = lineBounds =>
          x.alignment match {
            case TextAlignment.Left => 0

            case TextAlignment.Center => -(lineBounds.size.width / 2)

            case TextAlignment.Right => -lineBounds.size.width
          }

        val converterFunc: (TextLine, Int, Int) => Batch[DisplayEntity] =
          fontRegister
            .findByFontKey(x.fontKey)
            .map { fontInfo =>
              TextConversion.textLineToDisplayObjects(x, assetMapping, fontInfo)
            }
            .getOrElse { (_, _, _) =>
              IndigoLogger.errorOnce(s"Cannot render Text, missing Font with key: ${x.fontKey.toString()}")
              Batch()
            }

        val letters: Batch[DisplayEntity] =
          boundaryLocator
            .textAsLinesWithBounds(x.text, x.fontKey, x.letterSpacing, x.lineHeight)
            .foldLeft(0 -> Batch[DisplayEntity]()) { (acc, textLine) =>
              (
                acc._1 + textLine.lineBounds.height,
                acc._2 ++ converterFunc(textLine, alignmentOffsetX(textLine.lineBounds), acc._1)
              )
            }
            ._2

        (DisplayTextLetters(letters), noClones)

      case x: Text[_] =>
        val alignmentOffsetX: Rectangle => Int = lineBounds =>
          x.alignment match {
            case TextAlignment.Left => 0

            case TextAlignment.Center => -(lineBounds.size.width / 2)

            case TextAlignment.Right => -lineBounds.size.width
          }

        val converterFunc: (TextLine, Int, Int) => Batch[CloneTileData] =
          fontRegister
            .findByFontKey(x.fontKey)
            .map { fontInfo => (txtLn: TextLine, xPos: Int, yPos: Int) =>
              TextConversion.textLineToDisplayCloneTileData(x, fontInfo)(txtLn, xPos, yPos)
            }
            .getOrElse { (_, _, _) =>
              IndigoLogger.errorOnce(s"Cannot render Text, missing Font with key: ${x.fontKey.toString()}")
              Batch[CloneTileData]()
            }

        val (cloneId, clone) = TextConversion.makeTextCloneDisplayObject(x, assetMapping)

        val letters: Batch[CloneTileData] =
          boundaryLocator
            .textAsLinesWithBounds(x.text, x.fontKey, x.letterSpacing, x.lineHeight)
            .foldLeft(
              0 -> Batch[CloneTileData]()
            ) { (acc, textLine) =>
              (
                acc._1 + textLine.lineBounds.height,
                acc._2 ++ converterFunc(textLine, alignmentOffsetX(textLine.lineBounds), acc._1)
              )
            }
            ._2

        (
          DisplayTextLetters(
            letters.grouped(maxBatchSize).map { d =>
              new DisplayCloneTiles(
                id = cloneId,
                cloneData = d
              )
            }
          ),
          Batch((cloneId.toString, clone))
        )

      case _: RenderNode[_] =>
        (DisplayGroup.empty, noClones)

      case _: DependentNode[_] =>
        (DisplayGroup.empty, noClones)
    }

  def cloneBlankToDisplayObject(
      blank: CloneBlank,
      gameTime: GameTime,
      assetMapping: AssetMapping
  ): Option[DisplayObject] =
    blank.cloneable() match
      case s: Shape[_] =>
        Some(ShapeConversion.shapeToDisplayObject(s))

      case g: Graphic[_] =>
        Some(GraphicConversion.graphicToDisplayObject(g, assetMapping))

      case s: Sprite[_] =>
        animationsRegister
          .fetchAnimationForSprite(
            gameTime,
            s.bindingKey,
            s.animationKey,
            s.animationActions
          )
          .map { anim =>
            SpriteConversion.spriteToDisplayObject(
              boundaryLocator,
              s,
              assetMapping,
              anim
            )
          }

      // TODO: Should we just use this for everything? Clip isn't caught above, for instance.
      // Or are we better off calling the specialised functions?
      case e: EntityNode[_] =>
        Some(EntityNodeConversion.sceneEntityToDisplayObject(e, assetMapping))

      case _ =>
        None
