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
import indigo.scenegraph.BlankEntity
import indigo.scenegraph.Clip
import indigo.scenegraph.CloneBatch
import indigo.scenegraph.CloneTileData
import indigo.scenegraph.CloneTiles
import indigo.scenegraph.DependentNode
import indigo.scenegraph.EntityNode
import indigo.scenegraph.Graphic
import indigo.scenegraph.Group
import indigo.scenegraph.Mutants
import indigo.scenegraph.RenderNode
import indigo.scenegraph.SceneNodeVisitor
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

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
final class DisplayObjectConversionVisitor(
    boundaryLocator: BoundaryLocator,
    animationsRegister: AnimationsRegister,
    fontRegister: FontRegister,
    gameTime: GameTime,
    assetMapping: AssetMapping,
    maxBatchSize: Int,
    inputEvents: => Batch[GlobalEvent],
    sendEvent: GlobalEvent => Unit
)(using
    QuickCache[TextureRefAndOffset],
    QuickCache[Vector2],
    QuickCache[SpriteSheetFrameCoordinateOffsets],
    QuickCache[DisplayCloneBatch],
    QuickCache[DisplayCloneTiles],
    QuickCache[Batch[Float]],
    QuickCache[Batch[CloneTileData]],
    QuickCache[DisplayObject],
    QuickCache[Batch[DisplayObject]],
    QuickCache[Option[AnimationRef]]
) extends SceneNodeVisitor[DisplayConversionResult]:

  private var cloneBlankDisplayObjects: mutable.KVP[DisplayObject] =
    mutable.KVP.empty

  def setCloneBlanks(blanks: mutable.KVP[DisplayObject]): Unit =
    cloneBlankDisplayObjects = blanks

  val noClones = Batch.empty[DisplayConversionResultClone]

  def visitBlankEntity(node: BlankEntity): DisplayConversionResult =
    DisplayConversionResult(EntityNodeConversion.sceneEntityToDisplayObject(node, assetMapping), noClones)

  def visitClip(node: Clip[?]): DisplayConversionResult =
    DisplayConversionResult(EntityNodeConversion.sceneEntityToDisplayObject(node, assetMapping), noClones)

  def visitCloneBatch(node: CloneBatch): DisplayConversionResult =
    DisplayConversionResult(
      cloneBlankDisplayObjects.get(node.id.toString) match {
        case None =>
          DisplayGroup.empty

        case Some(_) =>
          CloneBatchConversion.cloneBatchDataToDisplayEntities(node)
      },
      noClones
    )

  def visitCloneTiles(node: CloneTiles): DisplayConversionResult =
    DisplayConversionResult(
      cloneBlankDisplayObjects.get(node.id.toString) match {
        case None =>
          DisplayGroup.empty

        case Some(_) =>
          CloneTilesConversion.cloneTilesDataToDisplayEntities(node)
      },
      noClones
    )

  def visitEntityNode(node: EntityNode[?]): DisplayConversionResult =
    DisplayConversionResult(EntityNodeConversion.sceneEntityToDisplayObject(node, assetMapping), noClones)

  def visitGraphic(node: Graphic[?]): DisplayConversionResult =
    DisplayConversionResult(GraphicConversion.graphicToDisplayObject(node, assetMapping), noClones)

  def visitGroup(node: Group): DisplayConversionResult =
    val entities = mutable.Batch.empty[DisplayEntity]
    val clones   = mutable.Batch.empty[DisplayConversionResultClone]

    node.children.foreach: child =>
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

      val res = child.accept(this)
      entities.append(res.displayEntity)
      res.clones.foreach(clones.append)

    DisplayConversionResult(
      DisplayGroup(
        GroupConversion.groupToMatrix(node),
        entities.toBatch
      ),
      clones.toBatch
    )

  def visitMutants(node: Mutants): DisplayConversionResult =
    DisplayConversionResult(
      cloneBlankDisplayObjects.get(node.id.toString) match {
        case None =>
          DisplayGroup.empty

        case Some(_) =>
          MutantConversion.mutantsToDisplayEntities(node)
      },
      noClones
    )

  def visitShapeBox(node: Shape.Box): DisplayConversionResult =
    DisplayConversionResult(ShapeConversion.shapeToDisplayObject(node), noClones)

  def visitShapeCircle(node: Shape.Circle): DisplayConversionResult =
    DisplayConversionResult(ShapeConversion.shapeToDisplayObject(node), noClones)

  def visitShapeLine(node: Shape.Line): DisplayConversionResult =
    DisplayConversionResult(ShapeConversion.shapeToDisplayObject(node), noClones)

  def visitShapePolygon(node: Shape.Polygon): DisplayConversionResult =
    DisplayConversionResult(ShapeConversion.shapeToDisplayObject(node), noClones)

  def visitSprite(node: Sprite[?]): DisplayConversionResult =
    // TODO: Use StringBuilder here.
    val animation = QuickCache("anim-" + node.bindingKey + node.animationKey + node.animationActions.hashCode.toString) {
      animationsRegister.fetchAnimationForSprite(gameTime, node.bindingKey, node.animationKey, node.animationActions)
    }

    DisplayConversionResult(
      animation match {
        case None =>
          IndigoLogger.errorOnce(s"Cannot render Sprite, missing Animations with key: ${node.animationKey.toString()}")
          DisplayGroup.empty

        case Some(anim) =>
          SpriteConversion.spriteToDisplayObject(boundaryLocator, node, assetMapping, anim)
      },
      noClones
    )

  def visitText(node: Text[?]): DisplayConversionResult =
    if !(node.rotation ~== Radians.zero) then
      val alignmentOffsetX: Rectangle => Int = lineBounds =>
        node.alignment match {
          case TextAlignment.Left => 0

          case TextAlignment.Center => -(lineBounds.size.width / 2)

          case TextAlignment.Right => -lineBounds.size.width
        }

      val converterFunc: (TextLine, Int, Int) => Batch[DisplayEntity] =
        fontRegister
          .findByFontKey(node.fontKey)
          .map { fontInfo =>
            TextConversion.textLineToDisplayObjects(node, assetMapping, fontInfo)
          }
          .getOrElse { (_, _, _) =>
            IndigoLogger.errorOnce(s"Cannot render Text, missing Font with key: ${node.fontKey.toString()}")
            Batch()
          }

      val letters: Batch[DisplayEntity] =
        boundaryLocator
          .textAsLinesWithBounds(node.text, node.fontKey, node.letterSpacing, node.lineHeight)
          .foldLeft(0 -> Batch[DisplayEntity]()) { (acc, textLine) =>
            (
              acc._1 + textLine.lineBounds.height,
              acc._2 ++ converterFunc(textLine, alignmentOffsetX(textLine.lineBounds), acc._1)
            )
          }
          ._2

      DisplayConversionResult(DisplayTextLetters(letters), noClones)
    else
      val alignmentOffsetX: Rectangle => Int = lineBounds =>
        node.alignment match {
          case TextAlignment.Left => 0

          case TextAlignment.Center => -(lineBounds.size.width / 2)

          case TextAlignment.Right => -lineBounds.size.width
        }

      val converterFunc: (TextLine, Int, Int) => Batch[CloneTileData] =
        fontRegister
          .findByFontKey(node.fontKey)
          .map { fontInfo => (txtLn: TextLine, xPos: Int, yPos: Int) =>
            TextConversion.textLineToDisplayCloneTileData(node, fontInfo)(txtLn, xPos, yPos)
          }
          .getOrElse { (_, _, _) =>
            IndigoLogger.errorOnce(s"Cannot render Text, missing Font with key: ${node.fontKey.toString()}")
            Batch[CloneTileData]()
          }

      val (cloneId, clone) = TextConversion.makeTextCloneDisplayObject(node, assetMapping)

      val letters: Batch[CloneTileData] =
        boundaryLocator
          .textAsLinesWithBounds(node.text, node.fontKey, node.letterSpacing, node.lineHeight)
          .foldLeft(
            0 -> Batch[CloneTileData]()
          ) { (acc, textLine) =>
            (
              acc._1 + textLine.lineBounds.height,
              acc._2 ++ converterFunc(textLine, alignmentOffsetX(textLine.lineBounds), acc._1)
            )
          }
          ._2

      DisplayConversionResult(
        DisplayTextLetters(
          letters.grouped(maxBatchSize).map { d =>
            new DisplayCloneTiles(
              id = cloneId,
              cloneData = d
            )
          }
        ),
        Batch(DisplayConversionResultClone(cloneId.toString, clone))
      )
