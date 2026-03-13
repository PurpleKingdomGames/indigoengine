package indigo.render.pipeline.displayprocessing.utils

import indigo.core.assets.AssetName
import indigo.shaders.*
import indigoengine.shared.collections.Batch

class ToCacheKeySyntaxTests extends munit.FunSuite {
  import ToCacheKeySyntax.*

  test("Can make a cache key") {
    val sd = ShaderData(
      shaderId = ShaderId("test"),
      uniformBlocks = Batch(
        UniformBlock(
          UniformBlockName("ub1"),
          Batch(
            Uniform("COLOR") -> ShaderPrimitive.vec4(1.0f)
          )
        ),
        UniformBlock(
          UniformBlockName("ub2"),
          Batch(
            Uniform("POSITION") -> ShaderPrimitive.vec2(1.0f, 0.0f),
            Uniform("ANGLE")    -> ShaderPrimitive.float(1.2f)
          )
        )
      ),
      channel0 = Some(AssetName("Asset 1")),
      channel1 = Some(AssetName("Asset 2")),
      channel2 = Some(AssetName("Asset 3")),
      channel3 = Some(AssetName("Asset 4"))
    )

    val actual =
      toCacheKey(sd)

    val expected =
      "testAsset 1Asset 2Asset 3Asset 4ub11111ub2101.2000000476837158"

    assertEquals(actual, expected)
  }

}
