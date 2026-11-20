package ultraviolet.macros

import ultraviolet.datatypes.ShaderAST

trait ShaderMacroUtils:

  val isTuple       = "^Tuple\\d+".r
  val isSwizzle     = "^([xyzwrgba]+)$".r
  val isSwizzleable = "^(vec2|vec3|vec4|bvec2|bvec3|bvec4|ivec2|ivec3|ivec4)$".r

  val intTypes: Set[String]   = Set("int", "ivec2", "ivec3", "ivec4")
  val floatTypes: Set[String] = Set("float", "vec2", "vec3", "vec4", "mat2", "mat3", "mat4")

  /** GLSL has no implicit conversion between integer and floating point types, so an operator with one of each can
    * never be translated. Types we do not model - anything that isn't in the two sets above - are left alone.
    */
  def isMixedNumericOperands(lhsType: String, rhsType: String): Boolean =
    (intTypes.contains(lhsType) && floatTypes.contains(rhsType)) ||
      (floatTypes.contains(lhsType) && intTypes.contains(rhsType))

  def mixedNumericOperandsMsg(op: String, lhsType: String, rhsType: String): String =
    s"Shaders do not support mixing integer and floating point types, found '$lhsType $op $rhsType'. " +
      "GLSL requires both operands to share a base type, so please cast one of them, e.g. 'a.toFloat * b'."

  def findReturnType: ShaderAST => ShaderAST =
    case _: ShaderAST.Empty       => ShaderAST.void
    case _: ShaderAST.Discard     => ShaderAST.void
    case v: ShaderAST.Block       => v.statements.reverse.headOption.map(findReturnType).getOrElse(ShaderAST.void)
    case v: ShaderAST.Neg         => findReturnType(v.value)
    case v: ShaderAST.Not         => findReturnType(v.value)
    case _: ShaderAST.UBO         => ShaderAST.void
    case v: ShaderAST.Struct      => ShaderAST.DataTypes.ident(v.name)
    case v: ShaderAST.New         => ShaderAST.DataTypes.ident(v.name)
    case v: ShaderAST.ShaderBlock =>
      v.statements.reverse.headOption.map(findReturnType).getOrElse(ShaderAST.void)
    case v: ShaderAST.Function                => v.returnType
    case v: ShaderAST.CallFunction            => v.returnType
    case v: ShaderAST.CallExternalFunction    => v.returnType
    case v: ShaderAST.FunctionRef             => v.returnType
    case v: ShaderAST.Cast                    => v.typeIdent
    case v: ShaderAST.Infix                   => v.returnType
    case v: ShaderAST.Assign                  => findReturnType(v.right)
    case _: ShaderAST.If                      => ShaderAST.void
    case _: ShaderAST.While                   => ShaderAST.void
    case _: ShaderAST.For                     => ShaderAST.void
    case _: ShaderAST.Switch                  => ShaderAST.void
    case v: ShaderAST.Val                     => findReturnType(v.value)
    case v: ShaderAST.MultiStatements         => findReturnType(v.statements.head)
    case v: ShaderAST.Annotated               => findReturnType(v.value)
    case _: ShaderAST.RawLiteral              => ShaderAST.void
    case _: ShaderAST.Field                   => ShaderAST.void
    case v: ShaderAST.DataTypes.ident         => v.typeIdent
    case v: ShaderAST.DataTypes.external      => v.typeIdent
    case v: ShaderAST.DataTypes.index         => v.typeIdent
    case v: ShaderAST.DataTypes.externalIndex => v.typeIdent
    case v: ShaderAST.DataTypes.bool          => v.typeIdent
    case v: ShaderAST.DataTypes.float         => v.typeIdent
    case v: ShaderAST.DataTypes.int           => v.typeIdent
    case v: ShaderAST.DataTypes.vec2          => v.typeIdent
    case v: ShaderAST.DataTypes.vec3          => v.typeIdent
    case v: ShaderAST.DataTypes.vec4          => v.typeIdent
    case v: ShaderAST.DataTypes.bvec2         => v.typeIdent
    case v: ShaderAST.DataTypes.bvec3         => v.typeIdent
    case v: ShaderAST.DataTypes.bvec4         => v.typeIdent
    case v: ShaderAST.DataTypes.ivec2         => v.typeIdent
    case v: ShaderAST.DataTypes.ivec3         => v.typeIdent
    case v: ShaderAST.DataTypes.ivec4         => v.typeIdent
    case v: ShaderAST.DataTypes.mat2          => v.typeIdent
    case v: ShaderAST.DataTypes.mat3          => v.typeIdent
    case v: ShaderAST.DataTypes.mat4          => v.typeIdent
    case v: ShaderAST.DataTypes.array         => v.typeIdent
    case v: ShaderAST.DataTypes.swizzle       => v.typeIdent
