package indigo.render.pipeline.sceneprocessing

import indigoengine.shared.collections.Batch

// TODO: Make mutable and allocate less.
final case class LightData(
    lightFlags: Batch[Float],
    lightColor: Batch[Float],
    lightSpecular: Batch[Float],
    lightPositionRotation: Batch[Float],
    lightNearFarAngleIntensity: Batch[Float]
) derives CanEqual {
  def +(other: LightData): LightData =
    this.copy(
      lightFlags = lightFlags ++ other.lightFlags,
      lightColor = lightColor ++ other.lightColor,
      lightSpecular = lightSpecular ++ other.lightSpecular,
      lightPositionRotation = lightPositionRotation ++ other.lightPositionRotation,
      lightNearFarAngleIntensity = lightNearFarAngleIntensity ++ other.lightNearFarAngleIntensity
    )

  // TODO: Remove all the ++'s
  def toArray: Batch[Float] =
    lightFlags ++
      lightColor ++
      lightSpecular ++
      lightPositionRotation ++
      lightNearFarAngleIntensity
}

object LightData {
  val empty: LightData =
    LightData(
      Batch[Float](0.0f, 0.0f, 0.0f, 0.0f),
      Batch[Float](0.0f, 0.0f, 0.0f, 0.0f),
      Batch[Float](0.0f, 0.0f, 0.0f, 0.0f),
      Batch[Float](0.0f, 0.0f, 0.0f, 0.0f),
      Batch[Float](0.0f, 0.0f, 0.0f, 0.0f)
    )

  val emptyData: Batch[Float] =
    empty.toArray
}
