package indigo.render.pipeline.sceneprocessing.utils

import indigo.render.pipeline.sceneprocessing.LightData
import indigo.scenegraph.AmbientLight
import indigo.scenegraph.DirectionLight
import indigo.scenegraph.Falloff
import indigo.scenegraph.Light
import indigo.scenegraph.PointLight
import indigo.scenegraph.SpotLight
import indigoengine.shared.collections.Batch

object BuildLightingData:

  val MaxLights: Int = 8

  private val bareLightData: LightData =
    LightData(
      Batch[Float](),
      Batch[Float](),
      Batch[Float](),
      Batch[Float](),
      Batch[Float]()
    )

  private val missingLightData: Batch[Batch[LightData]] =
    Batch.fromIndexedSeq(
      (0 to 8).map { i =>
        Batch.fill(i)(LightData.empty)
      }
    )

  def makeLightsData(lights: Batch[Light]): Batch[Float] = {
    val limitedLights = lights.take(MaxLights)
    val count         = limitedLights.length
    val fullLights    = limitedLights.map(makeLightData) ++ missingLightData(MaxLights - count)

    Batch[Float](count.toFloat, 0.0f, 0.0f, 0.0f) ++ fullLights.foldLeft(bareLightData)(_ + _).toArray
  }

  def makeLightData(light: Light): LightData =
    light match {
      case l: AmbientLight =>
        LightData(
          lightFlags = Batch[Float](1.0f, 0.0f, 0.0f, 0.0f),
          lightColor = Batch[Float](l.color.r.toFloat, l.color.g.toFloat, l.color.b.toFloat, l.color.a.toFloat),
          lightSpecular = Batch[Float](0.0f, 0.0f, 0.0f, 0.0f),
          lightPositionRotation = Batch[Float](0.0f, 0.0f, 0.0f, 0.0f),
          lightNearFarAngleIntensity = Batch[Float](0.0f, 0.0f, 0.0f, 0.0f)
        )

      case l: DirectionLight =>
        LightData(
          lightFlags = Batch[Float](1.0f, 1.0f, 0.0f, 0.0f),
          lightColor = Batch[Float](l.color.r.toFloat, l.color.g.toFloat, l.color.b.toFloat, l.color.a.toFloat),
          lightSpecular =
            Batch[Float](l.specular.r.toFloat, l.specular.g.toFloat, l.specular.b.toFloat, l.specular.a.toFloat),
          lightPositionRotation = Batch[Float](0.0f, 0.0f, l.rotation.toFloat, 0.0f),
          lightNearFarAngleIntensity = Batch[Float](0.0f, 0.0f, 0.0f, 0.0f)
        )

      case l: PointLight =>
        val useFarCuttOff: Float =
          l.falloff match {
            case Falloff.None(_, far)      => if (far.isDefined) 1.0f else 0.0f
            case Falloff.Linear(_, far)    => if (far.isDefined) 1.0f else 0.0f
            case Falloff.Quadratic(_, far) => if (far.isDefined) 1.0f else 0.0f
            case _                         => 1.0f
          }

        val falloffType: Float =
          l.falloff match {
            case _: Falloff.None            => 0.0f
            case _: Falloff.SmoothLinear    => 1.0f
            case _: Falloff.SmoothQuadratic => 2.0f
            case _: Falloff.Linear          => 3.0f
            case _: Falloff.Quadratic       => 4.0f
          }

        val near: Float =
          l.falloff match {
            case Falloff.None(near, _)            => near.toFloat
            case Falloff.SmoothLinear(near, _)    => near.toFloat
            case Falloff.SmoothQuadratic(near, _) => near.toFloat
            case Falloff.Linear(near, _)          => near.toFloat
            case Falloff.Quadratic(near, _)       => near.toFloat
          }

        val far: Float =
          l.falloff match {
            case Falloff.None(_, far)            => far.map(_.toFloat).getOrElse(10000.0f)
            case Falloff.SmoothLinear(_, far)    => far.toFloat
            case Falloff.SmoothQuadratic(_, far) => far.toFloat
            case Falloff.Linear(_, far)          => far.map(_.toFloat).getOrElse(10000.0f)
            case Falloff.Quadratic(_, far)       => far.map(_.toFloat).getOrElse(10000.0f)
          }

        LightData(
          lightFlags = Batch[Float](1.0f, 2.0f, useFarCuttOff, falloffType),
          lightColor = Batch[Float](l.color.r.toFloat, l.color.g.toFloat, l.color.b.toFloat, l.color.a.toFloat),
          lightSpecular =
            Batch[Float](l.specular.r.toFloat, l.specular.g.toFloat, l.specular.b.toFloat, l.specular.a.toFloat),
          lightPositionRotation = Batch[Float](l.position.x.toFloat, l.position.y.toFloat, 0.0f, 0.0f),
          lightNearFarAngleIntensity = Batch[Float](near, far, 0.0f, l.intensity.toFloat)
        )

      case l: SpotLight =>
        val useFarCuttOff: Float =
          l.falloff match {
            case Falloff.None(_, far)      => if (far.isDefined) 1.0f else 0.0f
            case Falloff.Linear(_, far)    => if (far.isDefined) 1.0f else 0.0f
            case Falloff.Quadratic(_, far) => if (far.isDefined) 1.0f else 0.0f
            case _                         => 1.0f
          }

        val falloffType: Float =
          l.falloff match {
            case _: Falloff.None            => 0.0f
            case _: Falloff.SmoothLinear    => 1.0f
            case _: Falloff.SmoothQuadratic => 2.0f
            case _: Falloff.Linear          => 3.0f
            case _: Falloff.Quadratic       => 4.0f
          }

        val near: Float =
          l.falloff match {
            case Falloff.None(near, _)            => near.toFloat
            case Falloff.SmoothLinear(near, _)    => near.toFloat
            case Falloff.SmoothQuadratic(near, _) => near.toFloat
            case Falloff.Linear(near, _)          => near.toFloat
            case Falloff.Quadratic(near, _)       => near.toFloat
          }

        val far: Float =
          l.falloff match {
            case Falloff.None(_, far)            => far.map(_.toFloat).getOrElse(10000.0f)
            case Falloff.SmoothLinear(_, far)    => far.toFloat
            case Falloff.SmoothQuadratic(_, far) => far.toFloat
            case Falloff.Linear(_, far)          => far.map(_.toFloat).getOrElse(10000.0f)
            case Falloff.Quadratic(_, far)       => far.map(_.toFloat).getOrElse(10000.0f)
          }

        LightData(
          lightFlags = Batch[Float](1.0f, 3.0f, useFarCuttOff, falloffType),
          lightColor = Batch[Float](l.color.r.toFloat, l.color.g.toFloat, l.color.b.toFloat, l.color.a.toFloat),
          lightSpecular =
            Batch[Float](l.specular.r.toFloat, l.specular.g.toFloat, l.specular.b.toFloat, l.specular.a.toFloat),
          lightPositionRotation = Batch[Float](l.position.x.toFloat, l.position.y.toFloat, l.rotation.toFloat, 0.0f),
          lightNearFarAngleIntensity = Batch[Float](near, far, l.angle.toFloat, l.intensity.toFloat)
        )
    }
