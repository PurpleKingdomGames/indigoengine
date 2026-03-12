package indigo.render.pipeline.sceneprocessing.utils

import indigo.render.pipeline.sceneprocessing.LightData
import indigo.scenegraph.AmbientLight
import indigo.scenegraph.DirectionLight
import indigo.scenegraph.Light
import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.RGBA
import indigoengine.shared.datatypes.Radians

class BuildLightingDataTests extends munit.FunSuite:

  def to2dp(d: Float): Double =
    Math.round(d.toDouble * 100).toDouble / 100.0

  /*
  For reference, this is what the shader is expecting.
  layout (std140) uniform IndigoDynamicLightingData {
    float numOfLights;
    vec4 lightFlags[8]; // vec4(active, type, use far, falloff type)
    vec4 lightColor[8];
    vec4 lightSpecular[8];
    vec4 lightPositionRotation[8];
    vec4 lightNearFarAngleIntensity[8];
  };
   */

  test("convert an ambient light to UBO data Array[Float]") {

    val light: Light =
      AmbientLight(RGBA.Red.withAmount(0.5))

    val actual: LightData =
      BuildLightingData.makeLightData(light)

    val expected: LightData =
      LightData(
        Batch[Float](1.0f, 0.0f, 0.0f, 0.0f), // lightFlags
        Batch[Float](1.0f, 0.0f, 0.0f, 0.5f), // lightColor
        Batch[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightSpecular
        Batch[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightPositionRotation
        Batch[Float](0.0f, 0.0f, 0.0f, 0.0f)  // lightNearFarAngleIntensity
      )

    assertEquals(actual.toArray.toList, expected.toArray.toList)

  }

  test("convert a direction light to UBO data Array[Float]") {

    val light: Light =
      DirectionLight(RGBA.Cyan.withAlpha(0.5), RGBA.White, Radians(0.25))

    val actual: LightData =
      BuildLightingData.makeLightData(light)

    val expected: LightData =
      LightData(
        Batch[Float](1.0f, 1.0f, 0.0f, 0.0f),  // lightFlags
        Batch[Float](0.0f, 1.0f, 1.0f, 0.5f),  // lightColor
        Batch[Float](1.0f, 1.0f, 1.0f, 1.0f),  // lightSpecular
        Batch[Float](0.0f, 0.0f, 0.25f, 0.0f), // lightPositionRotation
        Batch[Float](0.0f, 0.0f, 0.0f, 0.0f)   // lightNearFarAngleAttenuation
      )

    assertEquals(actual.toArray.toList, expected.toArray.toList)

  }

  test("Combining lights into data") {
    val lights: Batch[Light] =
      Batch(
        AmbientLight(RGBA.Red.withAmount(0.5)),
        DirectionLight(RGBA.Cyan.withAlpha(0.5), RGBA.White, Radians(0.25)),
        AmbientLight(RGBA.Green.withAmount(0.8))
      )

    val actual: Batch[Float] =
      BuildLightingData.makeLightsData(lights)

    val expected: Batch[Float] =
      Batch[Float](3, 0, 0, 0) ++ // first value, even though single float, requires space of vec4.
        (
          LightData(
            Batch[Float](1.0f, 0.0f, 0.0f, 0.0f), // lightFlags
            Batch[Float](1.0f, 0.0f, 0.0f, 0.5f), // lightColor
            Batch[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightSpecular
            Batch[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightPositionRotation
            Batch[Float](0.0f, 0.0f, 0.0f, 0.0f)  // lightNearFarAngleAttenuation
          ) +
            LightData(
              Batch[Float](1.0f, 1.0f, 0.0f, 0.0f),  // lightFlags
              Batch[Float](0.0f, 1.0f, 1.0f, 0.5f),  // lightColor
              Batch[Float](1.0f, 1.0f, 1.0f, 1.0f),  // lightSpecular
              Batch[Float](0.0f, 0.0f, 0.25f, 0.0f), // lightPositionRotation
              Batch[Float](0.0f, 0.0f, 0.0f, 0.0f)   // lightNearFarAngleAttenuation
            ) +
            LightData(
              Batch[Float](1.0f, 0.0f, 0.0f, 0.0f), // lightFlags
              Batch[Float](0.0f, 1.0f, 0.0f, 0.8f), // lightColor
              Batch[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightSpecular
              Batch[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightPositionRotation
              Batch[Float](0.0f, 0.0f, 0.0f, 0.0f)  // lightNearFarAngleAttenuation
            ) + LightData.empty +                   // There are always 8 lights.
            LightData.empty +
            LightData.empty +
            LightData.empty +
            LightData.empty
        ).toArray

    assertEquals(actual.toList.map(to2dp), expected.toList.map(to2dp))
  }
