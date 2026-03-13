package indigo.render.pipeline.datatypes

import indigo.core.datatypes.Vector2
import indigo.core.datatypes.mutable.CheapMatrix4
import indigo.render.pipeline.assets.AtlasId
import indigo.scenegraph.CloneBatchData
import indigo.scenegraph.CloneId
import indigo.scenegraph.CloneTileData
import indigo.shaders.ShaderId
import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.Radians

sealed trait DisplayEntity

final class DisplayGroup(
    val transform: CheapMatrix4,
    val entities: Batch[DisplayEntity]
) extends DisplayEntity derives CanEqual
object DisplayGroup:
  val empty: DisplayGroup =
    DisplayGroup(CheapMatrix4.identity, Batch())

final class DisplayTextLetters(
    val letters: Batch[DisplayEntity]
) extends DisplayEntity derives CanEqual
object DisplayTextLetters:
  val empty: DisplayTextLetters =
    DisplayTextLetters(Batch())

final class DisplayCloneBatch(
    val id: CloneId,
    val cloneData: Batch[CloneBatchData]
) extends DisplayEntity derives CanEqual

final class DisplayCloneTiles(
    val id: CloneId,
    val cloneData: Batch[CloneTileData]
) extends DisplayEntity derives CanEqual

final class DisplayMutants(
    val id: CloneId,
    val cloneData: Batch[Batch[DisplayObjectUniformData]]
) extends DisplayEntity derives CanEqual

final class DisplayObject(
    val x: Float,
    val y: Float,
    val scaleX: Float,
    val scaleY: Float,
    val refX: Float,
    val refY: Float,
    val flipX: Float,
    val flipY: Float,
    val rotation: Radians,
    val width: Float,
    val height: Float,
    val atlasName: Option[AtlasId],
    val frameScaleX: Float,
    val frameScaleY: Float,
    val channelOffset0X: Float,
    val channelOffset0Y: Float,
    val channelOffset1X: Float,
    val channelOffset1Y: Float,
    val channelOffset2X: Float,
    val channelOffset2Y: Float,
    val channelOffset3X: Float,
    val channelOffset3Y: Float,
    val textureX: Float,
    val textureY: Float,
    val textureWidth: Float,
    val textureHeight: Float,
    val atlasWidth: Float,
    val atlasHeight: Float,
    val shaderId: ShaderId,
    val shaderUniformData: Batch[DisplayObjectUniformData]
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
    new DisplayObject(
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

final class DisplayObjectUniformData(
    val uniformHash: String,
    val blockName: String,
    val data: Batch[Float]
) derives CanEqual
