package ultraviolet.macros

class ShaderMacroUtilsTests extends munit.FunSuite with ShaderMacroUtils {

  test("isMixedNumericOperands - int and float families cannot be mixed") {
    assert(isMixedNumericOperands("int", "float"))
    assert(isMixedNumericOperands("float", "int"))
    assert(isMixedNumericOperands("ivec2", "float"))
    assert(isMixedNumericOperands("vec3", "int"))
    assert(isMixedNumericOperands("ivec2", "mat2"))
    assert(isMixedNumericOperands("mat4", "ivec4"))
  }

  test("isMixedNumericOperands - matching families are fine") {
    assert(!isMixedNumericOperands("int", "int"))
    assert(!isMixedNumericOperands("ivec2", "int"))
    assert(!isMixedNumericOperands("float", "float"))
    assert(!isMixedNumericOperands("vec3", "float"))
    assert(!isMixedNumericOperands("mat4", "vec4"))
  }

  test("isMixedNumericOperands - types we do not model are left alone") {
    assert(!isMixedNumericOperands("void", "float"))
    assert(!isMixedNumericOperands("int", "void"))
    assert(!isMixedNumericOperands("MyStruct", "int"))
    assert(!isMixedNumericOperands("bool", "float"))
    assert(!isMixedNumericOperands("sampler2D", "int"))
  }

  test("mixedNumericOperandsMsg names the operator and both types") {
    val actual = mixedNumericOperandsMsg("%", "int", "float")

    assert(actual.contains("'int % float'"), actual)
    assert(actual.contains("toFloat"), actual)
  }

}
