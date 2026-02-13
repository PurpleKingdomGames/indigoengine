package indigo.platform.api.display

import indigo.core.datatypes.Vector2
import indigo.core.datatypes.mutable.CheapMatrix4
import indigo.platform.api.AtlasId
import indigo.scenegraph.CloneBatchData
import indigo.scenegraph.CloneId
import indigo.scenegraph.CloneTileData
import indigo.shaders.ShaderId
import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.Radians

sealed trait DisplayEntity

final case class DisplayGroup(
    transform: CheapMatrix4,
    entities: Batch[DisplayEntity]
) extends DisplayEntity derives CanEqual
object DisplayGroup:
  val empty: DisplayGroup =
    DisplayGroup(CheapMatrix4.identity, Batch())

final case class DisplayTextLetters(letters: Batch[DisplayEntity]) extends DisplayEntity derives CanEqual
object DisplayTextLetters:
  val empty: DisplayTextLetters =
    DisplayTextLetters(Batch())

final case class DisplayCloneBatch(
    id: CloneId,
    cloneData: Batch[CloneBatchData]
) extends DisplayEntity derives CanEqual

final case class DisplayCloneTiles(
    id: CloneId,
    cloneData: Batch[CloneTileData]
) extends DisplayEntity derives CanEqual

final case class DisplayMutants(
    id: CloneId,
    cloneData: Batch[Batch[DisplayObjectUniformData]]
) extends DisplayEntity derives CanEqual

final case class DisplayObject(
    x: Float,
    y: Float,
    scaleX: Float,
    scaleY: Float,
    refX: Float,
    refY: Float,
    flipX: Float,
    flipY: Float,
    rotation: Radians,
    width: Float,
    height: Float,
    atlasName: Option[AtlasId],
    frameScaleX: Float,
    frameScaleY: Float,
    channelOffset0X: Float,
    channelOffset0Y: Float,
    channelOffset1X: Float,
    channelOffset1Y: Float,
    channelOffset2X: Float,
    channelOffset2Y: Float,
    channelOffset3X: Float,
    channelOffset3Y: Float,
    textureX: Float,
    textureY: Float,
    textureWidth: Float,
    textureHeight: Float,
    atlasWidth: Float,
    atlasHeight: Float,
    shaderId: ShaderId,
    shaderUniformData: Batch[DisplayObjectUniformData]
) extends DisplayEntity derives CanEqual
object DisplayObject:

  given CanEqual[Option[DisplayObject], Option[DisplayObject]] = CanEqual.derived

  def apply(
      x: Float,
      y: Float,
      scaleX: Float,
      scaleY: Float,
      refX: Float,
      refY: Float,
      flipX: Float,
      flipY: Float,
      rotation: Radians,
      width: Int,
      height: Int,
      atlasName: Option[AtlasId],
      frame: SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets,
      channelOffset1: Vector2,
      channelOffset2: Vector2,
      channelOffset3: Vector2,
      texturePosition: Vector2,
      textureSize: Vector2,
      atlasSize: Vector2,
      shaderId: ShaderId,
      shaderUniformData: Batch[DisplayObjectUniformData]
  ): DisplayObject =
    DisplayObject(
      x,
      y,
      scaleX,
      scaleY,
      refX,
      refY,
      flipX,
      flipY,
      rotation,
      width.toFloat,
      height.toFloat,
      atlasName,
      frame.scale.x.toFloat,
      frame.scale.y.toFloat,
      frame.translate.x.toFloat,
      frame.translate.y.toFloat,
      channelOffset1.x.toFloat,
      channelOffset1.y.toFloat,
      channelOffset2.x.toFloat,
      channelOffset2.y.toFloat,
      channelOffset3.x.toFloat,
      channelOffset3.y.toFloat,
      texturePosition.x.toFloat,
      texturePosition.y.toFloat,
      textureSize.x.toFloat,
      textureSize.y.toFloat,
      atlasSize.x.toFloat,
      atlasSize.y.toFloat,
      shaderId,
      shaderUniformData
    )

final case class DisplayObjectUniformData(uniformHash: String, blockName: String, data: Batch[Float]) derives CanEqual
