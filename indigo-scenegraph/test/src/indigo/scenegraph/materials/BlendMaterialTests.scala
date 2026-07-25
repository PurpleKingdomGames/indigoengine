package indigo.scenegraph.materials

import indigoengine.shared.datatypes.RGBA

class BlendMaterialTests extends munit.FunSuite {

  test("Normal is a Fast blend material") {
    assert(BlendMaterial.Normal.isInstanceOf[BlendMaterial.SrcOnly])
  }

  test("Lighting is a Standard blend material") {
    assert(BlendMaterial.Lighting(RGBA.White).isInstanceOf[BlendMaterial.SrcAndDst])
  }

  test("BlendEffects is a Standard blend material regardless of affectsBackground") {
    assert(BlendMaterial.BlendEffects.None.isInstanceOf[BlendMaterial.SrcAndDst])
    assert(BlendMaterial.BlendEffects.None.applyToBackground.isInstanceOf[BlendMaterial.SrcAndDst])
  }

}
