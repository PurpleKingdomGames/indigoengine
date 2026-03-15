package indigo.render.pipeline.sceneprocessing.utils

import indigo.render.pipeline.sceneprocessing.LightDataBuilder
import indigo.scenegraph.AmbientLight
import indigo.scenegraph.DirectionLight
import indigo.scenegraph.Falloff
import indigo.scenegraph.Light
import indigo.scenegraph.PointLight
import indigo.scenegraph.SpotLight
import indigoengine.shared.collections.Batch

object BuildLightingData:

  val MaxLights: Int = 8

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while"))
  def makeLightsData(lights: Batch[Light]): Batch[Float] =
    val builder = LightDataBuilder()
    val count   = math.min(lights.length, MaxLights)
    builder.setCount(count)

    var i = 0
    while i < count do
      writeLightInto(builder, i, lights(i))
      i += 1

    builder.result()

  private def writeLightInto(
      builder: LightDataBuilder,
      index: Int,
      light: Light
  ): Unit =
    light match
      case l: AmbientLight =>
        builder.setLight(
          index = index,
          flagActive = 1.0f,
          flagType = 0.0f,
          flagUseFarCutoff = 0.0f,
          flagFalloffType = 0.0f,
          colorR = l.color.r.toFloat,
          colorG = l.color.g.toFloat,
          colorB = l.color.b.toFloat,
          colorA = l.color.a.toFloat,
          specularR = 0.0f,
          specularG = 0.0f,
          specularB = 0.0f,
          specularA = 0.0f,
          posX = 0.0f,
          posY = 0.0f,
          rotation = 0.0f,
          pos4 = 0.0f,
          near = 0.0f,
          far = 0.0f,
          angle = 0.0f,
          intensity = 0.0f
        )

      case l: DirectionLight =>
        builder.setLight(
          index = index,
          flagActive = 1.0f,
          flagType = 1.0f,
          flagUseFarCutoff = 0.0f,
          flagFalloffType = 0.0f,
          colorR = l.color.r.toFloat,
          colorG = l.color.g.toFloat,
          colorB = l.color.b.toFloat,
          colorA = l.color.a.toFloat,
          specularR = l.specular.r.toFloat,
          specularG = l.specular.g.toFloat,
          specularB = l.specular.b.toFloat,
          specularA = l.specular.a.toFloat,
          posX = 0.0f,
          posY = 0.0f,
          rotation = l.rotation.toFloat,
          pos4 = 0.0f,
          near = 0.0f,
          far = 0.0f,
          angle = 0.0f,
          intensity = 0.0f
        )

      case l: PointLight =>
        val (useFarCutoff, falloffType, near, far) = extractFalloff(l.falloff)

        builder.setLight(
          index = index,
          flagActive = 1.0f,
          flagType = 2.0f,
          flagUseFarCutoff = useFarCutoff,
          flagFalloffType = falloffType,
          colorR = l.color.r.toFloat,
          colorG = l.color.g.toFloat,
          colorB = l.color.b.toFloat,
          colorA = l.color.a.toFloat,
          specularR = l.specular.r.toFloat,
          specularG = l.specular.g.toFloat,
          specularB = l.specular.b.toFloat,
          specularA = l.specular.a.toFloat,
          posX = l.position.x.toFloat,
          posY = l.position.y.toFloat,
          rotation = 0.0f,
          pos4 = 0.0f,
          near = near,
          far = far,
          angle = 0.0f,
          intensity = l.intensity.toFloat
        )

      case l: SpotLight =>
        val (useFarCutoff, falloffType, near, far) = extractFalloff(l.falloff)

        builder.setLight(
          index = index,
          flagActive = 1.0f,
          flagType = 3.0f,
          flagUseFarCutoff = useFarCutoff,
          flagFalloffType = falloffType,
          colorR = l.color.r.toFloat,
          colorG = l.color.g.toFloat,
          colorB = l.color.b.toFloat,
          colorA = l.color.a.toFloat,
          specularR = l.specular.r.toFloat,
          specularG = l.specular.g.toFloat,
          specularB = l.specular.b.toFloat,
          specularA = l.specular.a.toFloat,
          posX = l.position.x.toFloat,
          posY = l.position.y.toFloat,
          rotation = l.rotation.toFloat,
          pos4 = 0.0f,
          near = near,
          far = far,
          angle = l.angle.toFloat,
          intensity = l.intensity.toFloat
        )

  private def extractFalloff(falloff: Falloff): (Float, Float, Float, Float) =
    falloff match
      case Falloff.None(near, Some(far)) =>
        val useFarCutoff: Float = 1.0f
        val falloffType: Float  = 0.0f

        (useFarCutoff, falloffType, near.toFloat, far.toFloat)

      case Falloff.None(near, None) =>
        val useFarCutoff: Float = 0.0f
        val falloffType: Float  = 0.0f
        val far: Float          = 10000.0f

        (useFarCutoff, falloffType, near.toFloat, far)

      case Falloff.SmoothLinear(near, far) =>
        val useFarCutoff: Float = 1.0f
        val falloffType: Float  = 1.0f

        (useFarCutoff, falloffType, near.toFloat, far.toFloat)

      case Falloff.SmoothQuadratic(near, far) =>
        val useFarCutoff: Float = 1.0f
        val falloffType: Float  = 2.0f

        (useFarCutoff, falloffType, near.toFloat, far.toFloat)

      case Falloff.Linear(near, Some(far)) =>
        val useFarCutoff: Float = 1.0f
        val falloffType: Float  = 3.0f

        (useFarCutoff, falloffType, near.toFloat, far.toFloat)

      case Falloff.Linear(near, None) =>
        val useFarCutoff: Float = 0.0f
        val falloffType: Float  = 3.0f
        val far: Float          = 10000.0f

        (useFarCutoff, falloffType, near.toFloat, far)

      case Falloff.Quadratic(near, Some(far)) =>
        val useFarCutoff: Float = 1.0f
        val falloffType: Float  = 4.0f

        (useFarCutoff, falloffType, near.toFloat, far.toFloat)

      case Falloff.Quadratic(near, None) =>
        val useFarCutoff: Float = 0.0f
        val falloffType: Float  = 4.0f
        val far: Float          = 10000.0f

        (useFarCutoff, falloffType, near.toFloat, far)
