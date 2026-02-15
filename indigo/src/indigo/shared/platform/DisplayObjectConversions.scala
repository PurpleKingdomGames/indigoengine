package indigo.shared.platform

import indigo.core.animation.AnimationRef
import indigo.core.assets.AssetName
import indigo.core.config.RenderingTechnology
import indigo.core.datatypes.FontChar
import indigo.core.datatypes.FontInfo
import indigo.core.datatypes.Point
import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.TextAlignment
import indigo.core.datatypes.Vector2
import indigo.core.datatypes.mutable.CheapMatrix4
import indigo.core.events.GlobalEvent
import indigo.core.time.GameTime
import indigo.core.utils.IndigoLogger
import indigo.core.utils.QuickCache
import indigo.platform.api.AssetMapping
import indigo.platform.api.TextureRefAndOffset
import indigo.platform.api.display.DisplayCloneBatch
import indigo.platform.api.display.DisplayCloneTiles
import indigo.platform.api.display.DisplayEntity
import indigo.platform.api.display.DisplayGroup
import indigo.platform.api.display.DisplayMutants
import indigo.platform.api.display.DisplayObject
import indigo.platform.api.display.DisplayObjectUniformData
import indigo.platform.api.display.DisplayTextLetters
import indigo.platform.api.display.SpriteSheetFrame
import indigo.platform.api.display.SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
import indigo.scenegraph.CloneBatch
import indigo.scenegraph.CloneId
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
import indigo.shaders.ShaderData
import indigo.shaders.ShaderPrimitive
import indigo.shaders.Uniform
import indigo.shaders.UniformBlock
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.KVP
import indigoengine.shared.datatypes.Radians

import scala.annotation.nowarn
import scala.annotation.tailrec

final class DisplayObjectConversions(
    boundaryLocator: BoundaryLocator,
    animationsRegister: AnimationsRegister,
    fontRegister: FontRegister
) {

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

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def lookupTexture(assetMapping: AssetMapping, name: AssetName): TextureRefAndOffset =
    QuickCache("tex-" + name.toString) {
      assetMapping.mappings
        .get(name.toString)
        .getOrElse {
          throw new Exception("Failed to find texture ref + offset for: " + name)
        }
    }

  private def cloneBatchDataToDisplayEntities(batch: CloneBatch): DisplayCloneBatch =
    if batch.staticBatchKey.isDefined then
      QuickCache(batch.staticBatchKey.get.toString) {
        new DisplayCloneBatch(
          id = batch.id,
          cloneData = batch.cloneData
        )
      }
    else
      new DisplayCloneBatch(
        id = batch.id,
        cloneData = batch.cloneData
      )

  private def cloneTilesDataToDisplayEntities(batch: CloneTiles): DisplayCloneTiles =
    if batch.staticBatchKey.isDefined then
      QuickCache(batch.staticBatchKey.get.toString) {
        new DisplayCloneTiles(
          id = batch.id,
          cloneData = batch.cloneData
        )
      }
    else
      new DisplayCloneTiles(
        id = batch.id,
        cloneData = batch.cloneData
      )

  private def mutantsToDisplayEntities(mutants: Mutants): DisplayMutants =
    val uniformDataConvert: Batch[UniformBlock] => Batch[DisplayObjectUniformData] = uniformBlocks =>
      uniformBlocks.map { ub =>
        DisplayObjectUniformData(
          uniformHash = ub.uniformHash,
          blockName = ub.blockName.toString,
          data = DisplayObjectConversions.packUBO(ub.uniforms, ub.uniformHash, false)
        )
      }

    new DisplayMutants(
      id = mutants.id,
      cloneData = mutants.uniformBlocks.map(uniformDataConvert)
    )

  def processSceneNodes(
      sceneNodes: Batch[SceneNode],
      gameTime: GameTime,
      assetMapping: AssetMapping,
      cloneBlankDisplayObjects: => KVP[DisplayObject],
      renderingTechnology: RenderingTechnology,
      maxBatchSize: Int,
      inputEvents: => Batch[GlobalEvent],
      sendEvent: GlobalEvent => Unit
  ): (Batch[DisplayEntity], Batch[(String, DisplayObject)]) =
    val f =
      sceneNodeToDisplayObject(
        gameTime,
        assetMapping,
        cloneBlankDisplayObjects,
        renderingTechnology,
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

  private def groupToMatrix(group: Group): CheapMatrix4 =
    CheapMatrix4.identity
      .scale(
        if (group.flip.horizontal) -1.0 else 1.0,
        if (group.flip.vertical) -1.0 else 1.0,
        1.0f
      )
      .translate(
        -group.ref.x.toFloat,
        -group.ref.y.toFloat,
        0.0f
      )
      .scale(group.scale.x.toFloat, group.scale.y.toFloat, 1.0f)
      .rotate(group.rotation.toFloat)
      .translate(
        group.position.x.toFloat,
        group.position.y.toFloat,
        0.0f
      )

  def sceneNodeToDisplayObject(
      gameTime: GameTime,
      assetMapping: AssetMapping,
      cloneBlankDisplayObjects: => KVP[DisplayObject],
      renderingTechnology: RenderingTechnology,
      maxBatchSize: Int,
      inputEvents: => Batch[GlobalEvent],
      sendEvent: GlobalEvent => Unit
  )(sceneNode: SceneNode): (DisplayEntity, Batch[(String, DisplayObject)]) =
    val noClones = Batch[(String, DisplayObject)]()
    sceneNode match {
      case x: Graphic[_] =>
        (graphicToDisplayObject(x, assetMapping), noClones)

      case s: Shape[_] =>
        (shapeToDisplayObject(s), noClones)

      case s: EntityNode[_] =>
        (sceneEntityToDisplayObject(s, assetMapping), noClones)

      case c: CloneBatch =>
        (
          cloneBlankDisplayObjects.get(c.id.toString) match {
            case None =>
              DisplayGroup.empty

            case Some(_) =>
              cloneBatchDataToDisplayEntities(c)
          },
          noClones
        )

      case c: CloneTiles =>
        (
          cloneBlankDisplayObjects.get(c.id.toString) match {
            case None =>
              DisplayGroup.empty

            case Some(_) =>
              cloneTilesDataToDisplayEntities(c)
          },
          noClones
        )

      case c: Mutants =>
        (
          cloneBlankDisplayObjects.get(c.id.toString) match {
            case None =>
              DisplayGroup.empty

            case Some(_) =>
              mutantsToDisplayEntities(c)
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
            renderingTechnology,
            maxBatchSize,
            inputEvents,
            sendEvent
          )
        (
          DisplayGroup(
            groupToMatrix(g),
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
              spriteToDisplayObject(boundaryLocator, x, assetMapping, anim)
          },
          noClones
        )

      case x: Text[_] if renderingTechnology.isWebGL1 || !(x.rotation ~== Radians.zero) =>
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
              textLineToDisplayObjects(x, assetMapping, fontInfo)
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

      case x: Text[_] if renderingTechnology.isWebGL2 =>
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
              textLineToDisplayCloneTileData(x, fontInfo)(txtLn, xPos, yPos)
            }
            .getOrElse { (_, _, _) =>
              IndigoLogger.errorOnce(s"Cannot render Text, missing Font with key: ${x.fontKey.toString()}")
              Batch[CloneTileData]()
            }

        val (cloneId, clone) = makeTextCloneDisplayObject(x, assetMapping)

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

  def optionalAssetToOffset(assetMapping: AssetMapping, maybeAssetName: Option[AssetName]): Vector2 =
    maybeAssetName match {
      case None =>
        Vector2.zero

      case Some(assetName) =>
        lookupTexture(assetMapping, assetName).offset
    }

  def shapeToDisplayObject(leaf: Shape[?]): DisplayObject = {

    val offset = leaf match
      case s: Shape.Box =>
        val size = s.dimensions.size

        if size.width == size.height then Point.zero
        else if size.width < size.height then
          Point(-Math.round((size.height.toDouble - size.width.toDouble) / 2).toInt, 0)
        else Point(0, -Math.round((size.width.toDouble - size.height.toDouble) / 2).toInt)

      case _ =>
        Point.zero

    val boundsActual = BoundaryLocator.untransformedShapeBounds(leaf)

    val shader: ShaderData = Shape.toShaderData(leaf, boundsActual)
    val bounds             = boundsActual.toSquare

    val vec2Zero = Vector2.zero
    val uniformData: Batch[DisplayObjectUniformData] =
      shader.uniformBlocks.map { ub =>
        DisplayObjectUniformData(
          uniformHash = ub.uniformHash,
          blockName = ub.blockName.toString,
          data = DisplayObjectConversions.packUBO(ub.uniforms, ub.uniformHash, false)
        )
      }

    val offsetRef = leaf.ref - offset

    DisplayObject(
      x = leaf.position.x.toFloat,
      y = leaf.position.y.toFloat,
      scaleX = leaf.scale.x.toFloat,
      scaleY = leaf.scale.y.toFloat,
      refX = offsetRef.x.toFloat,
      refY = offsetRef.y.toFloat,
      flipX = if leaf.flip.horizontal then -1.0 else 1.0,
      flipY = if leaf.flip.vertical then -1.0 else 1.0,
      rotation = leaf.rotation,
      width = bounds.size.width,
      height = bounds.size.height,
      atlasName = None,
      frame = SpriteSheetFrame.defaultOffset,
      channelOffset1 = vec2Zero,
      channelOffset2 = vec2Zero,
      channelOffset3 = vec2Zero,
      texturePosition = vec2Zero,
      textureSize = vec2Zero,
      atlasSize = vec2Zero,
      shaderId = shader.shaderId,
      shaderUniformData = uniformData
    )
  }

  def sceneEntityToDisplayObject(leaf: EntityNode[?], assetMapping: AssetMapping): DisplayObject = {
    val shader: ShaderData = leaf.toShaderData

    val channelOffset1 = optionalAssetToOffset(assetMapping, shader.channel1)
    val channelOffset2 = optionalAssetToOffset(assetMapping, shader.channel2)
    val channelOffset3 = optionalAssetToOffset(assetMapping, shader.channel3)

    val bounds = Rectangle(Point.zero, leaf.size)

    val texture =
      shader.channel0.map(assetName => lookupTexture(assetMapping, assetName))

    val frameInfo: SpriteSheetFrameCoordinateOffsets =
      texture match {
        case None =>
          SpriteSheetFrame.defaultOffset

        case Some(texture) =>
          QuickCache(s"${bounds.hashCode().toString}_${shader.hashCode().toString}") {
            SpriteSheetFrame.calculateFrameOffset(
              atlasSize = texture.atlasSize,
              frameCrop = bounds,
              textureOffset = texture.offset
            )
          }
      }

    val shaderId = shader.shaderId

    val uniformData: Batch[DisplayObjectUniformData] =
      shader.uniformBlocks.map { ub =>
        DisplayObjectUniformData(
          uniformHash = ub.uniformHash,
          blockName = ub.blockName.toString,
          data = DisplayObjectConversions.packUBO(ub.uniforms, ub.uniformHash, false)
        )
      }

    DisplayObject(
      x = leaf.position.x.toFloat,
      y = leaf.position.y.toFloat,
      scaleX = leaf.scale.x.toFloat,
      scaleY = leaf.scale.y.toFloat,
      refX = leaf.ref.x.toFloat,
      refY = leaf.ref.y.toFloat,
      flipX = if leaf.flip.horizontal then -1.0 else 1.0,
      flipY = if leaf.flip.vertical then -1.0 else 1.0,
      rotation = leaf.rotation,
      width = bounds.size.width,
      height = bounds.size.height,
      atlasName = texture.map(_.atlasName),
      frame = frameInfo,
      channelOffset1 = frameInfo.offsetToCoords(channelOffset1),
      channelOffset2 = frameInfo.offsetToCoords(channelOffset2),
      channelOffset3 = frameInfo.offsetToCoords(channelOffset3),
      texturePosition = texture.map(_.offset).getOrElse(Vector2.zero),
      textureSize = texture.map(_.size).getOrElse(Vector2.zero),
      atlasSize = texture.map(_.atlasSize).getOrElse(Vector2.zero),
      shaderId = shaderId,
      shaderUniformData = uniformData
    )
  }

  def graphicToDisplayObject(leaf: Graphic[?], assetMapping: AssetMapping): DisplayObject = {
    val shaderData     = leaf.material.toShaderData
    val shaderDataHash = shaderData.toCacheKey
    val materialName   = shaderData.channel0.get

    val emissiveOffset = findAssetOffsetValues(assetMapping, shaderData.channel1, shaderDataHash, "_e")
    val normalOffset   = findAssetOffsetValues(assetMapping, shaderData.channel2, shaderDataHash, "_n")
    val specularOffset = findAssetOffsetValues(assetMapping, shaderData.channel3, shaderDataHash, "_s")

    val texture = lookupTexture(assetMapping, materialName)

    val frameInfo =
      QuickCache(s"${leaf.crop.hashCode().toString}_$shaderDataHash") {
        SpriteSheetFrame.calculateFrameOffset(
          atlasSize = texture.atlasSize,
          frameCrop = leaf.crop,
          textureOffset = texture.offset
        )
      }

    val shaderId = shaderData.shaderId

    val uniformData: Batch[DisplayObjectUniformData] =
      shaderData.uniformBlocks.map { ub =>
        DisplayObjectUniformData(
          uniformHash = ub.uniformHash,
          blockName = ub.blockName.toString,
          data = DisplayObjectConversions.packUBO(ub.uniforms, ub.uniformHash, false)
        )
      }

    DisplayObject(
      x = leaf.position.x.toFloat,
      y = leaf.position.y.toFloat,
      scaleX = leaf.scale.x.toFloat,
      scaleY = leaf.scale.y.toFloat,
      refX = leaf.ref.x.toFloat,
      refY = leaf.ref.y.toFloat,
      flipX = if leaf.flip.horizontal then -1.0 else 1.0,
      flipY = if leaf.flip.vertical then -1.0 else 1.0,
      rotation = leaf.rotation,
      width = leaf.crop.size.width,
      height = leaf.crop.size.height,
      atlasName = Some(texture.atlasName),
      frame = frameInfo,
      channelOffset1 = frameInfo.offsetToCoords(emissiveOffset),
      channelOffset2 = frameInfo.offsetToCoords(normalOffset),
      channelOffset3 = frameInfo.offsetToCoords(specularOffset),
      texturePosition = texture.offset,
      textureSize = texture.size,
      atlasSize = texture.atlasSize,
      shaderId = shaderId,
      shaderUniformData = uniformData
    )
  }

  def spriteToDisplayObject(
      boundaryLocator: BoundaryLocator,
      leaf: Sprite[?],
      assetMapping: AssetMapping,
      anim: AnimationRef
  ): DisplayObject = {
    val material       = leaf.material
    val shaderData     = material.toShaderData
    val shaderDataHash = shaderData.toCacheKey
    val materialName   = shaderData.channel0.get

    val emissiveOffset = findAssetOffsetValues(assetMapping, shaderData.channel1, shaderDataHash, "_e")
    val normalOffset   = findAssetOffsetValues(assetMapping, shaderData.channel2, shaderDataHash, "_n")
    val specularOffset = findAssetOffsetValues(assetMapping, shaderData.channel3, shaderDataHash, "_s")

    val texture = lookupTexture(assetMapping, materialName)

    val frameInfo =
      QuickCache(anim.frameHash + shaderDataHash) {
        SpriteSheetFrame.calculateFrameOffset(
          atlasSize = texture.atlasSize,
          frameCrop = anim.currentFrame.crop,
          textureOffset = texture.offset
        )
      }

    val bounds = boundaryLocator.spriteFrameBounds(leaf).getOrElse(Rectangle.zero)

    val shaderId = shaderData.shaderId

    val uniformData: Batch[DisplayObjectUniformData] =
      shaderData.uniformBlocks.map { ub =>
        DisplayObjectUniformData(
          uniformHash = ub.uniformHash,
          blockName = ub.blockName.toString,
          data = DisplayObjectConversions.packUBO(ub.uniforms, ub.uniformHash, false)
        )
      }

    DisplayObject(
      x = leaf.position.x.toFloat,
      y = leaf.position.y.toFloat,
      scaleX = leaf.scale.x.toFloat,
      scaleY = leaf.scale.y.toFloat,
      refX = leaf.ref.x.toFloat,
      refY = leaf.ref.y.toFloat,
      flipX = if leaf.flip.horizontal then -1.0 else 1.0,
      flipY = if leaf.flip.vertical then -1.0 else 1.0,
      rotation = leaf.rotation,
      width = bounds.width,
      height = bounds.height,
      atlasName = Some(texture.atlasName),
      frame = frameInfo,
      channelOffset1 = frameInfo.offsetToCoords(emissiveOffset),
      channelOffset2 = frameInfo.offsetToCoords(normalOffset),
      channelOffset3 = frameInfo.offsetToCoords(specularOffset),
      texturePosition = texture.offset,
      textureSize = texture.size,
      atlasSize = texture.atlasSize,
      shaderId = shaderId,
      shaderUniformData = uniformData
    )
  }

  def textLineToDisplayObjects(
      leaf: Text[?],
      assetMapping: AssetMapping,
      fontInfo: FontInfo
  ): (TextLine, Int, Int) => Batch[DisplayEntity] =
    (line, alignmentOffsetX, yOffset) => {

      val material       = leaf.material
      val shaderData     = material.toShaderData
      val shaderDataHash = shaderData.toCacheKey
      val materialName   = shaderData.channel0.get

      val lineHash: String =
        "[indigo_txt]" +
          leaf.material.hashCode.toString +
          leaf.position.hashCode.toString +
          leaf.scale.hashCode.toString +
          leaf.rotation.hashCode.toString +
          leaf.ref.hashCode.toString +
          leaf.flip.horizontal.toString +
          leaf.flip.vertical.toString +
          leaf.fontKey.toString +
          line.hashCode.toString

      val emissiveOffset = findAssetOffsetValues(assetMapping, shaderData.channel1, shaderDataHash, "_e")
      val normalOffset   = findAssetOffsetValues(assetMapping, shaderData.channel2, shaderDataHash, "_n")
      val specularOffset = findAssetOffsetValues(assetMapping, shaderData.channel3, shaderDataHash, "_s")

      val texture = lookupTexture(assetMapping, materialName)

      val shaderId = shaderData.shaderId

      val uniformData: Batch[DisplayObjectUniformData] =
        shaderData.uniformBlocks.map { ub =>
          DisplayObjectUniformData(
            uniformHash = ub.uniformHash,
            blockName = ub.blockName.toString,
            data = DisplayObjectConversions.packUBO(ub.uniforms, ub.uniformHash, false)
          )
        }

      QuickCache(lineHash) {
        zipWithCharDetails(Batch.fromArray(line.text.toCharArray), fontInfo, leaf.letterSpacing).map {
          case (fontChar, xPosition) =>
            val frameInfo =
              QuickCache(fontChar.bounds.hashCode().toString + "_" + shaderDataHash) {
                SpriteSheetFrame.calculateFrameOffset(
                  atlasSize = texture.atlasSize,
                  frameCrop = fontChar.bounds,
                  textureOffset = texture.offset
                )
              }

            DisplayObject(
              x = leaf.position.x.toFloat,
              y = leaf.position.y.toFloat,
              scaleX = leaf.scale.x.toFloat,
              scaleY = leaf.scale.y.toFloat,
              refX = (leaf.ref.x + -(xPosition + alignmentOffsetX)).toFloat,
              refY = (leaf.ref.y - yOffset).toFloat,
              flipX = if leaf.flip.horizontal then -1.0 else 1.0,
              flipY = if leaf.flip.vertical then -1.0 else 1.0,
              rotation = leaf.rotation,
              width = fontChar.bounds.width,
              height = fontChar.bounds.height,
              atlasName = Some(texture.atlasName),
              frame = frameInfo,
              channelOffset1 = frameInfo.offsetToCoords(emissiveOffset),
              channelOffset2 = frameInfo.offsetToCoords(normalOffset),
              channelOffset3 = frameInfo.offsetToCoords(specularOffset),
              texturePosition = texture.offset,
              textureSize = texture.size,
              atlasSize = texture.atlasSize,
              shaderId = shaderId,
              shaderUniformData = uniformData
            )
        }
      }
    }

  def makeTextCloneDisplayObject(
      leaf: Text[?],
      assetMapping: AssetMapping
  ): (CloneId, DisplayObject) = {

    val cloneId: CloneId =
      CloneId(
        "[indigo_txt_clone]" +
          leaf.material.hashCode.toString +
          leaf.position.hashCode.toString +
          leaf.scale.hashCode.toString +
          leaf.rotation.hashCode.toString +
          leaf.ref.hashCode.toString +
          leaf.flip.horizontal.toString +
          leaf.flip.vertical.toString +
          leaf.fontKey.toString
      )

    val clone =
      QuickCache(s"[indigo_text_clone_ref][${cloneId.toString}]") {
        val material       = leaf.material
        val shaderData     = material.toShaderData
        val shaderDataHash = shaderData.toCacheKey
        val materialName   = shaderData.channel0.get
        val emissiveOffset = findAssetOffsetValues(assetMapping, shaderData.channel1, shaderDataHash, "_e")
        val normalOffset   = findAssetOffsetValues(assetMapping, shaderData.channel2, shaderDataHash, "_n")
        val specularOffset = findAssetOffsetValues(assetMapping, shaderData.channel3, shaderDataHash, "_s")
        val texture        = lookupTexture(assetMapping, materialName)
        val shaderId       = shaderData.shaderId

        val uniformData: Batch[DisplayObjectUniformData] =
          shaderData.uniformBlocks.map { ub =>
            DisplayObjectUniformData(
              uniformHash = ub.uniformHash,
              blockName = ub.blockName.toString,
              data = DisplayObjectConversions.packUBO(ub.uniforms, ub.uniformHash, false)
            )
          }

        val frameInfo =
          SpriteSheetFrame.calculateFrameOffset(
            atlasSize = texture.atlasSize,
            frameCrop = Rectangle.one,
            textureOffset = texture.offset
          )

        DisplayObject(
          x = leaf.position.x.toFloat,
          y = leaf.position.y.toFloat,
          scaleX = leaf.scale.x.toFloat,
          scaleY = leaf.scale.y.toFloat,
          refX = leaf.ref.x.toFloat,
          refY = leaf.ref.y.toFloat,
          flipX = if leaf.flip.horizontal then -1.0 else 1.0,
          flipY = if leaf.flip.vertical then -1.0 else 1.0,
          rotation = leaf.rotation,
          width = 1,
          height = 1,
          atlasName = Some(texture.atlasName),
          frame = frameInfo,
          channelOffset1 = frameInfo.offsetToCoords(emissiveOffset),
          channelOffset2 = frameInfo.offsetToCoords(normalOffset),
          channelOffset3 = frameInfo.offsetToCoords(specularOffset),
          texturePosition = texture.offset,
          textureSize = texture.size,
          atlasSize = texture.atlasSize,
          shaderId = shaderId,
          shaderUniformData = uniformData
        )
      }

    (
      cloneId,
      clone
    )
  }

  def textLineToDisplayCloneTileData(
      leaf: Text[?],
      fontInfo: FontInfo
  ): (TextLine, Int, Int) => Batch[CloneTileData] =
    (line, alignmentOffsetX, yOffset) => {
      val lineHash: String =
        "[indigo_tln]" +
          leaf.position.hashCode.toString +
          leaf.ref.hashCode.toString +
          leaf.scale.hashCode.toString +
          line.hashCode.toString +
          leaf.fontKey.toString

      QuickCache(lineHash) {
        zipWithCharDetails(Batch.fromArray(line.text.toArray), fontInfo, leaf.letterSpacing).map {
          case (fontChar, xPosition) =>
            CloneTileData(
              x = leaf.position.x + leaf.ref.x + xPosition + alignmentOffsetX,
              y = leaf.position.y + leaf.ref.y + yOffset,
              rotation = Radians.zero,
              scaleX = leaf.scale.x.toFloat,
              scaleY = leaf.scale.y.toFloat,
              cropX = fontChar.bounds.x,
              cropY = fontChar.bounds.y,
              cropWidth = fontChar.bounds.width,
              cropHeight = fontChar.bounds.height
            )
        }
      }
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var accCharDetails: Batch[(FontChar, Int)] = Batch()

  @nowarn("msg=unused")
  private def zipWithCharDetails(
      charList: Batch[Char],
      fontInfo: FontInfo,
      letterSpacing: Int
  ): Batch[(FontChar, Int)] = {
    @tailrec
    def rec(remaining: Batch[(Char, FontChar)], nextX: Int): Batch[(FontChar, Int)] =
      if remaining.isEmpty then accCharDetails
      else
        val x  = remaining.head
        val xs = remaining.tail
        (x._2, nextX) +: accCharDetails

        val ls = if xs.isEmpty then 0 else letterSpacing
        rec(xs, nextX + x._2.bounds.width + ls)

    accCharDetails = Batch()
    rec(charList.map(c => (c, fontInfo.findByCharacter(c))), 0)
  }

  def findAssetOffsetValues(
      assetMapping: AssetMapping,
      maybeAssetName: Option[AssetName],
      cacheKey: String,
      cacheSuffix: String
  ): Vector2 =
    QuickCache[Vector2](cacheKey + cacheSuffix) {
      maybeAssetName
        .map { t =>
          lookupTexture(assetMapping, t).offset
        }
        .getOrElse(Vector2.zero)
    }

  extension (sd: ShaderData)
    def toCacheKey: String =
      sd.shaderId.toString +
        sd.channel0.map(_.toString).getOrElse("") +
        sd.channel1.map(_.toString).getOrElse("") +
        sd.channel2.map(_.toString).getOrElse("") +
        sd.channel3.map(_.toString).getOrElse("") +
        sd.uniformBlocks.map(_.uniformHash).mkString
}

object DisplayObjectConversions {

  private val empty0: Batch[Float] = Batch[Float]()
  private val empty1: Batch[Float] = Batch[Float](0.0f)
  private val empty2: Batch[Float] = Batch[Float](0.0f, 0.0f)
  private val empty3: Batch[Float] = Batch[Float](0.0f, 0.0f, 0.0f)

  def expandTo4(arr: Batch[Float]): Batch[Float] =
    arr.length match {
      case 0 => arr
      case 1 => arr ++ empty3
      case 2 => arr ++ empty2
      case 3 => arr ++ empty1
      case 4 => arr
      case _ => arr
    }

  def packUBO(
      uniforms: Batch[(Uniform, ShaderPrimitive)],
      cacheKey: String,
      disableCache: Boolean
  )(using QuickCache[Batch[Float]]): Batch[Float] = {
    @tailrec
    def rec(
        remaining: Batch[ShaderPrimitive],
        current: Batch[Float],
        acc: Batch[Float]
    ): Batch[Float] =
      remaining match
        case us if us.isEmpty =>
          // println(s"done, expanded: ${current.toList} to ${expandTo4(current).toList}")
          // println(s"result: ${(acc ++ expandTo4(current)).toList}")
          acc ++ expandTo4(current)

        case us if current.length == 4 =>
          // println(s"current full, sub-result: ${(acc ++ current).toList}")
          rec(us, empty0, acc ++ current)

        case us if current.isEmpty && us.head.isArray =>
          // println(s"Found an array, current is empty, set current to: ${u.toArray.toList}")
          rec(us.tail, us.head.toBatch, acc)

        case us if current.length == 1 && us.head.length == 2 =>
          // println("Current value is float, must not straddle byte boundary when adding vec2")
          rec(us.tail, current ++ Batch(0.0f) ++ us.head.toBatch, acc)

        case us if current.length + us.head.length > 4 =>
          // println(s"doesn't fit, expanded: ${current.toList} to ${expandTo4(current).toList},  sub-result: ${(acc ++ expandTo4(current)).toList}")
          rec(us, empty0, acc ++ expandTo4(current))

        case us if us.head.isArray =>
          // println(s"fits but next value is array, expanded: ${current.toList} to ${expandTo4(current).toList},  sub-result: ${(acc ++ expandTo4(current)).toList}")
          rec(us, empty0, acc ++ current)

        case us =>
          // println(s"fits, current is now: ${(current ++ u.toArray).toList}")
          rec(us.tail, current ++ us.head.toBatch, acc)

    QuickCache(cacheKey, disableCache) {
      rec(uniforms.map(_._2), empty0, empty0)
    }
  }

}
