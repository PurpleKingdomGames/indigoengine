package ultraviolet.macros

import ultraviolet.datatypes.ShaderAST

trait ShaderMacroUtils:

  val isSwizzle                                 = "^([xyzwrgba]+)$".r
  val isSwizzleable                             = "^(vec2|vec3|vec4|bvec2|bvec3|bvec4|ivec2|ivec3|ivec4)$".r
  def isGLSLReservedWord(word: String): Boolean = allReservedWords.contains(word)

  def findReturnType: ShaderAST => Option[ShaderAST] =
    case v: ShaderAST.Empty             => None
    case v: ShaderAST.Block             => v.statements.reverse.headOption.flatMap(findReturnType)
    case v: ShaderAST.Neg               => findReturnType(v.value)
    case v: ShaderAST.UBO               => None
    case v: ShaderAST.Struct            => Option(ShaderAST.DataTypes.ident(v.name))
    case v: ShaderAST.New               => Option(ShaderAST.DataTypes.ident(v.name))
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
    case v: ShaderAST.DataTypes.bool    => v.typeIdent
    case v: ShaderAST.DataTypes.float   => v.typeIdent
    case v: ShaderAST.DataTypes.int     => v.typeIdent
    case v: ShaderAST.DataTypes.vec2    => v.typeIdent
    case v: ShaderAST.DataTypes.vec3    => v.typeIdent
    case v: ShaderAST.DataTypes.vec4    => v.typeIdent
    case v: ShaderAST.DataTypes.bvec2   => v.typeIdent
    case v: ShaderAST.DataTypes.bvec3   => v.typeIdent
    case v: ShaderAST.DataTypes.bvec4   => v.typeIdent
    case v: ShaderAST.DataTypes.ivec2   => v.typeIdent
    case v: ShaderAST.DataTypes.ivec3   => v.typeIdent
    case v: ShaderAST.DataTypes.ivec4   => v.typeIdent
    case v: ShaderAST.DataTypes.mat2    => v.typeIdent
    case v: ShaderAST.DataTypes.mat3    => v.typeIdent
    case v: ShaderAST.DataTypes.mat4    => v.typeIdent
    case v: ShaderAST.DataTypes.array   => v.typeIdent
    case v: ShaderAST.DataTypes.swizzle => v.typeIdent

  val cReservedWords: List[String] =
    List(
      "auto",
      "else",
      "long",
      "switch",
      "break",
      "enum",
      "register",
      "typedef",
      "case",
      "extern",
      "return",
      "union",
      "char",
      "float",
      "short",
      "unsigned",
      "const",
      "for",
      "signed",
      "void",
      "continue",
      "goto",
      "sizeof",
      "volatile",
      "default",
      "if",
      "static",
      "while",
      "do",
      "int",
      "struct",
      "_Packed",
      "double"
    )

  val GLSLReservedWords: List[String] =
    List(
      "attribute",
      "uniform",
      "varying",
      "layout",
      "centroid",
      "flat",
      "smooth",
      "noperspective",
      "patch",
      "sample",
      "subroutine",
      "in",
      "out",
      "inout",
      "invariant",
      "discard",
      "mat2",
      "mat3",
      "mat4",
      "dmat2",
      "dmat3",
      "dmat4",
      "mat2x2",
      "mat2x3",
      "mat2x4",
      "dmat2x2",
      "dmat2x3",
      "dmat2x4",
      "mat3x2",
      "mat3x3",
      "mat3x4",
      "dmat3x2",
      "dmat3x3",
      "dmat3x4",
      "mat4x2",
      "mat4x3",
      "mat4x4",
      "dmat4x2",
      "dmat4x3",
      "dmat4x4",
      "vec2",
      "vec3",
      "vec4",
      "ivec2",
      "ivec3",
      "ivec4",
      "bvec2",
      "bvec3",
      "bvec4",
      "dvec2",
      "dvec3",
      "dvec4",
      "uvec2",
      "uvec3",
      "uvec4",
      "lowp",
      "mediump",
      "highp",
      "precision",
      "sampler1D",
      "sampler2D",
      "sampler3D",
      "samplerCube",
      "sampler1DShadow",
      "sampler2DShadow",
      "samplerCubeShadow",
      "sampler1DArray",
      "sampler2DArray",
      "sampler1DArrayShadow",
      "sampler2DArrayShadow",
      "isampler1D",
      "isampler2D",
      "isampler3D",
      "isamplerCube",
      "isampler1DArray",
      "isampler2DArray",
      "usampler1D",
      "usampler2D",
      "usampler3D",
      "usamplerCube",
      "usampler1DArray",
      "usampler2DArray",
      "sampler2DRect",
      "sampler2DRectShadow",
      "isampler2DRect",
      "usampler2DRect",
      "samplerBuffer",
      "isamplerBuffer",
      "usamplerBuffer",
      "sampler2DMS",
      "isampler2DMS",
      "usampler2DMS",
      "sampler2DMSArray",
      "isampler2DMSArray",
      "usampler2DMSArray",
      "samplerCubeArray",
      "samplerCubeArrayShadow",
      "isamplerCubeArray",
      "usamplerCubeArray",
      "common",
      "partition",
      "active",
      "asm",
      "class",
      "union",
      "enum",
      "typedef",
      "template",
      "this",
      "packed",
      "goto",
      "inline",
      "noinline",
      "volatile",
      "public",
      "static",
      "extern",
      "external",
      "interface",
      "long",
      "short",
      "half",
      "fixed",
      "unsigned",
      "superp",
      "input",
      "output",
      "hvec2",
      "hvec3",
      "hvec4",
      "fvec2",
      "fvec3",
      "fvec4",
      "sampler3DRect",
      "filter",
      "image1D",
      "image2D",
      "image3D",
      "imageCube",
      "iimage1D",
      "iimage2D",
      "iimage3D",
      "iimageCube",
      "uimage1D",
      "uimage2D",
      "uimage3D",
      "uimageCube",
      "image1DArray",
      "image2DArray",
      "iimage1DArray",
      "iimage2DArray",
      "uimage1DArray",
      "uimage2DArray",
      "image1DShadow",
      "image2DShadow",
      "image1DArrayShadow",
      "image2DArrayShadow",
      "imageBuffer",
      "iimageBuffer",
      "uimageBuffer",
      "sizeof",
      "cast",
      "namespace",
      "using",
      "row_major"
    )

  val allReservedWords: List[String] =
    cReservedWords ++ GLSLReservedWords
