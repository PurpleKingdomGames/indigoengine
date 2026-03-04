package indigo.shaders

/** The point of these tests is purely to exercise the process of compiling and validating the standard shaders. If we
  * get no exceptions we're at least in basically good shape, i.e. They may not do as intended but there's nothing
  * structurally wrong with them, like a stray forward reference for example.
  */
class StandardShadersTests extends munit.FunSuite {

  test("Bitmap is valid") {
    assert(StandardShaders.Bitmap.vertex.code.nonEmpty)
    assert(StandardShaders.Bitmap.fragment.code.nonEmpty)
  }

  test("LitBitmap is valid") {
    assert(StandardShaders.LitBitmap.vertex.code.nonEmpty)
    assert(StandardShaders.LitBitmap.fragment.code.nonEmpty)
  }

  test("ImageEffects is valid") {
    assert(StandardShaders.ImageEffects.vertex.code.nonEmpty)
    assert(StandardShaders.ImageEffects.fragment.code.nonEmpty)
  }

  test("LitImageEffects is valid") {
    assert(StandardShaders.LitImageEffects.vertex.code.nonEmpty)
    assert(StandardShaders.LitImageEffects.fragment.code.nonEmpty)
  }

  test("BitmapClip is valid") {
    assert(StandardShaders.BitmapClip.vertex.code.nonEmpty)
    assert(StandardShaders.BitmapClip.fragment.code.nonEmpty)
  }

  test("LitBitmapClip is valid") {
    assert(StandardShaders.LitBitmapClip.vertex.code.nonEmpty)
    assert(StandardShaders.LitBitmapClip.fragment.code.nonEmpty)
  }

  test("ImageEffectsClip is valid") {
    assert(StandardShaders.ImageEffectsClip.vertex.code.nonEmpty)
    assert(StandardShaders.ImageEffectsClip.fragment.code.nonEmpty)
  }

  test("LitImageEffectsClip is valid") {
    assert(StandardShaders.LitImageEffectsClip.vertex.code.nonEmpty)
    assert(StandardShaders.LitImageEffectsClip.fragment.code.nonEmpty)
  }

  test("ShapeBox is valid") {
    assert(StandardShaders.ShapeBox.vertex.code.nonEmpty)
    assert(StandardShaders.ShapeBox.fragment.code.nonEmpty)
  }

  test("LitShapeBox is valid") {
    assert(StandardShaders.LitShapeBox.vertex.code.nonEmpty)
    assert(StandardShaders.LitShapeBox.fragment.code.nonEmpty)
  }

  test("ShapeCircle is valid") {
    assert(StandardShaders.ShapeCircle.vertex.code.nonEmpty)
    assert(StandardShaders.ShapeCircle.fragment.code.nonEmpty)
  }

  test("LitShapeCircle is valid") {
    assert(StandardShaders.LitShapeCircle.vertex.code.nonEmpty)
    assert(StandardShaders.LitShapeCircle.fragment.code.nonEmpty)
  }

  test("ShapeLine is valid") {
    assert(StandardShaders.ShapeLine.vertex.code.nonEmpty)
    assert(StandardShaders.ShapeLine.fragment.code.nonEmpty)
  }

  test("LitShapeLine is valid") {
    assert(StandardShaders.LitShapeLine.vertex.code.nonEmpty)
    assert(StandardShaders.LitShapeLine.fragment.code.nonEmpty)
  }

  test("ShapePolygon is valid") {
    assert(StandardShaders.ShapePolygon.vertex.code.nonEmpty)
    assert(StandardShaders.ShapePolygon.fragment.code.nonEmpty)
  }

  test("LitShapePolygon is valid") {
    assert(StandardShaders.LitShapePolygon.vertex.code.nonEmpty)
    assert(StandardShaders.LitShapePolygon.fragment.code.nonEmpty)
  }

  test("NormalBlend is valid") {
    assert(StandardShaders.NormalBlend.vertex.code.nonEmpty)
    assert(StandardShaders.NormalBlend.fragment.code.nonEmpty)
  }

  test("LightingBlend is valid") {
    assert(StandardShaders.LightingBlend.vertex.code.nonEmpty)
    assert(StandardShaders.LightingBlend.fragment.code.nonEmpty)
  }

  test("BlendEffects is valid") {
    assert(StandardShaders.BlendEffects.vertex.code.nonEmpty)
    assert(StandardShaders.BlendEffects.fragment.code.nonEmpty)
  }
}
