package indigo.render.pipeline.datatypes

import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.Vector2

object SpriteSheetFrame:

  def calculateFrameOffset(
      atlasSize: Vector2,
      frameCrop: Rectangle,
      textureOffset: Vector2
  ): SpriteSheetFrameCoordinateOffsets =
    val frameSize       = frameCrop.size.toVector
    val scaleFactor     = frameSize / atlasSize
    val frameCropPos    = frameCrop.position.toVector
    val translateScale  = scaleFactor / frameSize
    val translateOffset = scaleFactor * (frameCropPos / frameSize)
    val translate       = translateScale * textureOffset + translateOffset

    SpriteSheetFrameCoordinateOffsets(scaleFactor, translate, translateScale, translateOffset)

  val defaultOffset: SpriteSheetFrameCoordinateOffsets =
    calculateFrameOffset(Vector2(1.0, 1.0), Rectangle(0, 0, 1, 1), Vector2.zero)

  final class SpriteSheetFrameCoordinateOffsets(
      val scale: Vector2,
      val translate: Vector2,
      val translateScale: Vector2,
      val translateOffset: Vector2
  ) derives CanEqual:
    inline def offsetToCoords(textureOffset: Vector2): Vector2 =
      translateScale * textureOffset + translateOffset
