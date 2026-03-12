package indigo.render.pipeline.sceneprocessing

import indigoengine.shared.collections.Batch

// Layout matches the shader UBO (std140):
//   [0..3]     header (count, 0, 0, 0)
//   [4..35]    lightFlags[8]
//   [36..67]   lightColor[8]
//   [68..99]   lightSpecular[8]
//   [100..131] lightPositionRotation[8]
//   [132..163] lightNearFarAngleIntensity[8]
final class LightDataBuilder private (private val data: Array[Float]):

  def setCount(count: Int): Unit =
    data(0) = count.toFloat

  def setLight(
      index: Int,
      flagActive: Float,
      flagType: Float,
      flagUseFarCutoff: Float,
      flagFalloffType: Float,
      colorR: Float,
      colorG: Float,
      colorB: Float,
      colorA: Float,
      specularR: Float,
      specularG: Float,
      specularB: Float,
      specularA: Float,
      posX: Float,
      posY: Float,
      rotation: Float,
      pos4: Float,
      near: Float,
      far: Float,
      angle: Float,
      intensity: Float
  ): Unit =
    val fi = 4 + index * 4
    data(fi) = flagActive
    data(fi + 1) = flagType
    data(fi + 2) = flagUseFarCutoff
    data(fi + 3) = flagFalloffType

    val ci = 36 + index * 4
    data(ci) = colorR
    data(ci + 1) = colorG
    data(ci + 2) = colorB
    data(ci + 3) = colorA

    val si = 68 + index * 4
    data(si) = specularR
    data(si + 1) = specularG
    data(si + 2) = specularB
    data(si + 3) = specularA

    val pi = 100 + index * 4
    data(pi) = posX
    data(pi + 1) = posY
    data(pi + 2) = rotation
    data(pi + 3) = pos4

    val ni = 132 + index * 4
    data(ni) = near
    data(ni + 1) = far
    data(ni + 2) = angle
    data(ni + 3) = intensity

  def result(): Batch[Float] =
    Batch.fromArray(data.clone())

object LightDataBuilder:
  val DataSize: Int = 164

  def apply(): LightDataBuilder =
    new LightDataBuilder(new Array[Float](DataSize))
