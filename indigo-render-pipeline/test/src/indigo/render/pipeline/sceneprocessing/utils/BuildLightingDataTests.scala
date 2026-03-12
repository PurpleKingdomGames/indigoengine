package indigo.render.pipeline.sceneprocessing.utils

import indigo.core.datatypes.Point
import indigo.scenegraph.AmbientLight
import indigo.scenegraph.DirectionLight
import indigo.scenegraph.Falloff
import indigo.scenegraph.Light
import indigo.scenegraph.PointLight
import indigo.scenegraph.SpotLight
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

  // Helper to extract a single light's data from the flat array by field group
  private def lightFlags(data: Batch[Float], i: Int): List[Float] =
    (4 + i * 4 until 4 + i * 4 + 4).map(data(_)).toList

  private def lightColor(data: Batch[Float], i: Int): List[Float] =
    (36 + i * 4 until 36 + i * 4 + 4).map(data(_)).toList

  private def lightSpecular(data: Batch[Float], i: Int): List[Float] =
    (68 + i * 4 until 68 + i * 4 + 4).map(data(_)).toList

  private def lightPosRot(data: Batch[Float], i: Int): List[Float] =
    (100 + i * 4 until 100 + i * 4 + 4).map(data(_)).toList

  private def lightNearFar(data: Batch[Float], i: Int): List[Float] =
    (132 + i * 4 until 132 + i * 4 + 4).map(data(_)).toList

  test("convert an ambient light to UBO data") {
    val actual = BuildLightingData.makeLightsData(
      Batch(AmbientLight(RGBA.Red.withAmount(0.5)))
    )

    assertEquals(lightFlags(actual, 0), List(1.0f, 0.0f, 0.0f, 0.0f))
    assertEquals(lightColor(actual, 0), List(1.0f, 0.0f, 0.0f, 0.5f))
    assertEquals(lightSpecular(actual, 0), List(0.0f, 0.0f, 0.0f, 0.0f))
    assertEquals(lightPosRot(actual, 0), List(0.0f, 0.0f, 0.0f, 0.0f))
    assertEquals(lightNearFar(actual, 0), List(0.0f, 0.0f, 0.0f, 0.0f))
  }

  test("convert a direction light to UBO data") {
    val actual = BuildLightingData.makeLightsData(
      Batch(DirectionLight(RGBA.Cyan.withAlpha(0.5), RGBA.White, Radians(0.25)))
    )

    assertEquals(lightFlags(actual, 0), List(1.0f, 1.0f, 0.0f, 0.0f))
    assertEquals(lightColor(actual, 0), List(0.0f, 1.0f, 1.0f, 0.5f))
    assertEquals(lightSpecular(actual, 0), List(1.0f, 1.0f, 1.0f, 1.0f))
    assertEquals(lightPosRot(actual, 0).map(to2dp), List(0.0, 0.0, 0.25, 0.0))
    assertEquals(lightNearFar(actual, 0), List(0.0f, 0.0f, 0.0f, 0.0f))
  }

  test("Combining 3 lights into data") {
    val lights: Batch[Light] =
      Batch(
        AmbientLight(RGBA.Red.withAmount(0.5)),
        DirectionLight(RGBA.Cyan.withAlpha(0.5), RGBA.White, Radians(0.25)),
        AmbientLight(RGBA.Green.withAmount(0.8))
      )

    val actual = BuildLightingData.makeLightsData(lights)

    assertEquals(actual.length, 164)
    assertEquals(actual(0), 3.0f) // count

    // Light 0: ambient red
    assertEquals(lightFlags(actual, 0), List(1.0f, 0.0f, 0.0f, 0.0f))
    assertEquals(lightColor(actual, 0), List(1.0f, 0.0f, 0.0f, 0.5f))

    // Light 1: direction cyan
    assertEquals(lightFlags(actual, 1), List(1.0f, 1.0f, 0.0f, 0.0f))
    assertEquals(lightColor(actual, 1), List(0.0f, 1.0f, 1.0f, 0.5f))
    assertEquals(lightSpecular(actual, 1), List(1.0f, 1.0f, 1.0f, 1.0f))
    assertEquals(lightPosRot(actual, 1).map(to2dp), List(0.0, 0.0, 0.25, 0.0))

    // Light 2: ambient green
    assertEquals(lightFlags(actual, 2), List(1.0f, 0.0f, 0.0f, 0.0f))
    assertEquals(lightColor(actual, 2), List(0.0f, 1.0f, 0.0f, 0.8f))

    // Lights 3-7: empty (inactive)
    (3 until 8).foreach { i =>
      assertEquals(lightFlags(actual, i), List(0.0f, 0.0f, 0.0f, 0.0f))
    }
  }

  test("makeLightsData - 0 lights produces 164 floats with count 0") {
    val actual = BuildLightingData.makeLightsData(Batch.empty)
    assertEquals(actual.length, 164)
    assertEquals(actual(0), 0.0f)
    assertEquals(actual.toList.distinct, List(0.0f))
  }

  test("makeLightsData - 8 lights fills all slots") {
    val lights = Batch.fill(8)(AmbientLight(RGBA.White): Light)
    val actual = BuildLightingData.makeLightsData(lights)

    assertEquals(actual.length, 164)
    assertEquals(actual(0), 8.0f)

    (0 until 8).foreach { i =>
      assertEquals(lightFlags(actual, i)(0), 1.0f) // active
    }
  }

  test("makeLightsData - 9+ lights capped at 8") {
    val lights = Batch.fill(10)(AmbientLight(RGBA.White): Light)
    val actual = BuildLightingData.makeLightsData(lights)

    assertEquals(actual.length, 164)
    assertEquals(actual(0), 8.0f)
  }

  test("makeLightsData - point light with SmoothQuadratic falloff") {
    val light: Light = PointLight(
      position = Point(100, 200),
      color = RGBA.Blue,
      specular = RGBA.Green,
      intensity = 3.0,
      falloff = Falloff.SmoothQuadratic(10, 500)
    )
    val actual = BuildLightingData.makeLightsData(Batch(light))

    assertEquals(actual(0), 1.0f) // count

    // flags: [active=1, type=2 (point), useFarCutoff=1, falloffType=2 (smoothQuadratic)]
    assertEquals(lightFlags(actual, 0), List(1.0f, 2.0f, 1.0f, 2.0f))
    assertEquals(lightColor(actual, 0), List(0.0f, 0.0f, 1.0f, 1.0f))
    assertEquals(lightSpecular(actual, 0), List(0.0f, 1.0f, 0.0f, 1.0f))
    assertEquals(lightPosRot(actual, 0), List(100.0f, 200.0f, 0.0f, 0.0f))
    assertEquals(lightNearFar(actual, 0), List(10.0f, 500.0f, 0.0f, 3.0f))
  }

  test("makeLightsData - spot light with Linear falloff (no far)") {
    val light: Light = SpotLight(
      position = Point(50, 75),
      color = RGBA.Red,
      specular = RGBA.White,
      intensity = 1.5,
      angle = Radians(0.8),
      rotation = Radians(1.2),
      falloff = Falloff.Linear(5, Option.empty)
    )
    val actual = BuildLightingData.makeLightsData(Batch(light))

    // flags: [active=1, type=3 (spot), useFarCutoff=0 (no far), falloffType=3 (linear)]
    assertEquals(lightFlags(actual, 0), List(1.0f, 3.0f, 0.0f, 3.0f))
    assertEquals(lightPosRot(actual, 0).map(to2dp), List(50.0, 75.0, 1.2, 0.0))
    assertEquals(lightNearFar(actual, 0).map(to2dp), List(5.0, 10000.0, 0.8, 1.5))
  }

  test("makeLightsData - point light with None falloff (with far)") {
    val light: Light = PointLight(
      position = Point(10, 20),
      color = RGBA.White,
      specular = RGBA.White,
      intensity = 2.0,
      falloff = Falloff.None(5, Some(200))
    )
    val actual = BuildLightingData.makeLightsData(Batch(light))

    // flags: useFarCutoff=1 (far is defined), falloffType=0 (None)
    assertEquals(lightFlags(actual, 0), List(1.0f, 2.0f, 1.0f, 0.0f))
    assertEquals(lightNearFar(actual, 0), List(5.0f, 200.0f, 0.0f, 2.0f))
  }

  test("makeLightsData - point light with Quadratic falloff (no far)") {
    val light: Light = PointLight(
      position = Point(0, 0),
      color = RGBA.White,
      specular = RGBA.White,
      intensity = 1.0,
      falloff = Falloff.Quadratic(10, Option.empty)
    )
    val actual = BuildLightingData.makeLightsData(Batch(light))

    // flags: useFarCutoff=0, falloffType=4 (Quadratic)
    assertEquals(lightFlags(actual, 0), List(1.0f, 2.0f, 0.0f, 4.0f))
    assertEquals(lightNearFar(actual, 0), List(10.0f, 10000.0f, 0.0f, 1.0f))
  }
