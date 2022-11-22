package ultraviolet.macros

import ultraviolet.datatypes.ShaderAST

trait ShaderMacroUtils:

  val isSwizzle     = "^([xyzw]+)$".r
  val isSwizzleable = "^(vec2|vec3|vec4)$".r

  def findReturnType: ShaderAST => Option[ShaderAST] =
    case v: ShaderAST.Empty             => None
    case v: ShaderAST.Block             => v.statements.reverse.headOption.flatMap(findReturnType)
    case v: ShaderAST.ShaderBlock       => v.statements.reverse.headOption.flatMap(findReturnType)
    case v: ShaderAST.Function          => v.returnType
    case v: ShaderAST.CallFunction      => v.returnType
    case v: ShaderAST.FunctionRef       => v.returnType
    case v: ShaderAST.Cast              => v.typeIdent
    case v: ShaderAST.Infix             => v.returnType
    case v: ShaderAST.Assign            => findReturnType(v.right)
    case v: ShaderAST.If                => None
    case v: ShaderAST.While             => None
    case v: ShaderAST.For               => None
    case v: ShaderAST.Switch            => None
    case v: ShaderAST.Val               => findReturnType(v.value)
    case v: ShaderAST.Annotated         => findReturnType(v.value)
    case v: ShaderAST.RawLiteral        => None
    case v: ShaderAST.DataTypes.ident   => v.typeIdent
    case v: ShaderAST.DataTypes.index   => v.typeIdent
    case v: ShaderAST.DataTypes.float   => v.typeIdent
    case v: ShaderAST.DataTypes.int     => v.typeIdent
    case v: ShaderAST.DataTypes.vec2    => v.typeIdent
    case v: ShaderAST.DataTypes.vec3    => v.typeIdent
    case v: ShaderAST.DataTypes.vec4    => v.typeIdent
    case v: ShaderAST.DataTypes.mat2    => v.typeIdent
    case v: ShaderAST.DataTypes.mat3    => v.typeIdent
    case v: ShaderAST.DataTypes.mat4    => v.typeIdent
    case v: ShaderAST.DataTypes.array   => v.typeIdent
    case v: ShaderAST.DataTypes.swizzle => v.typeIdent
