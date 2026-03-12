package indigo.render.pipeline.displayprocessing.utils

import indigo.core.utils.QuickCache
import indigo.shaders.Uniform
import indigoengine.shared.collections.Batch

class PackUBOsTests extends munit.FunSuite {

  given cache: QuickCache[Batch[Float]] = QuickCache.empty

  override def beforeEach(context: BeforeEach): Unit =
    cache.purgeAllNow()

  test("ubo packing") {

    import indigo.shaders.ShaderPrimitive._

    val uniforms =
      Batch(
        Uniform("a") -> float(1),
        Uniform("b") -> float(2),
        Uniform("c") -> vec3(3, 4, 5),
        Uniform("d") -> float(6),
        Uniform("e") -> array(4)(vec2(7, 8), vec2(9, 10), vec2(11, 12)),
        Uniform("f") -> float(13)
      )

    val expected: Array[Float] =
      Array[Array[Float]](
        Array[Float](1, 2, 0, 0),
        Array[Float](3, 4, 5, 0),
        Array[Float](6, 0, 0, 0),
        Array[Float](7, 8, 0, 0, 9, 10, 0, 0, 11, 12, 0, 0, 0, 0, 0, 0),
        Array[Float](13, 0, 0, 0)
      ).flatten

    val actual: Batch[Float] =
      PackUBOs.packUBO(uniforms, "", true)

    assertEquals(actual.toList, expected.toList)

  }

  test("ubo packing - do not straddle byte boundaries") {

    import indigo.shaders.ShaderPrimitive._

    val uniforms =
      Batch(
        Uniform("a") -> float(1),
        Uniform("b") -> vec2(2, 3)
      )

    val expected: Batch[Float] =
      Batch[Batch[Float]](
        Batch[Float](1, 0, 2, 3)
      ).flatten

    val actual: Batch[Float] =
      PackUBOs.packUBO(uniforms, "", true)

    assertEquals(actual.toList, expected.toList)

  }

  test("ubo packing - arrays") {

    import indigo.shaders.ShaderPrimitive._

    val uniforms =
      Batch(
        Uniform("ASPECT_RATIO") -> vec2(1.0),
        Uniform("STROKE_WIDTH") -> float(2.0),
        Uniform("COUNT")        -> float(3.0),
        Uniform("STROKE_COLOR") -> vec4(4.0),
        Uniform("FILL_COLOR")   -> vec4(5.0)
      )

    val expected: Array[Float] =
      Array[Array[Float]](
        Array[Float](1, 1),
        Array[Float](2),
        Array[Float](3),
        Array[Float](4, 4, 4, 4),
        Array[Float](5, 5, 5, 5)
      ).flatten

    assertEquals(
      PackUBOs.packUBO(uniforms, "", true).toList,
      expected.toList
    )

    // Exact 3 array.
    assertEquals(
      PackUBOs
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(3)(vec2(6.0), vec2(7.0), vec2(8.0)), "", true)
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0)
    )

    // 4 array padded.
    assertEquals(
      PackUBOs
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(4)(vec2(6.0), vec2(7.0), vec2(8.0)), "", true)
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0) ++ List[Float](0, 0, 0, 0)
    )

    // 5 array padded.
    assertEquals(
      PackUBOs
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(5)(vec2(6.0), vec2(7.0), vec2(8.0)), "", true)
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0) ++ List[Float](0, 0, 0, 0) ++ List[Float](
        0,
        0,
        0,
        0
      )
    )

    // 6 array padded.
    assertEquals(
      PackUBOs
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(6)(vec2(6.0), vec2(7.0), vec2(8.0)), "", true)
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0) ++ List[Float](0, 0, 0, 0) ++ List[Float](
        0,
        0,
        0,
        0
      ) ++ List[Float](0, 0, 0, 0)
    )

    // 7 array padded.
    assertEquals(
      PackUBOs
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(7)(vec2(6.0), vec2(7.0), vec2(8.0)), "", true)
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0) ++ List[Float](0, 0, 0, 0) ++ List[Float](
        0,
        0,
        0,
        0
      ) ++ List[Float](0, 0, 0, 0) ++ List[Float](0, 0, 0, 0)
    )

    // 8 array padded.
    assertEquals(
      PackUBOs
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(8)(vec2(6.0), vec2(7.0), vec2(8.0)), "", true)
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0) ++ List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++ List[Float](0, 0, 0, 0) ++ List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0)
    )

    // 16 array padded.
    assertEquals(
      PackUBOs
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(16)(vec2(6.0), vec2(7.0), vec2(8.0)), "", true)
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0)
    )

  }

  test("ubo packing - raw array of floats") {

    import indigo.shaders.ShaderPrimitive._

    val uniforms =
      Batch(
        Uniform("TEST") -> rawArray(Array(0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f))
      )

    val expected: Array[Float] =
      Array(0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f)

    assertEquals(
      PackUBOs.packUBO(uniforms, "", true).toList,
      expected.toList
    )

  }

}
