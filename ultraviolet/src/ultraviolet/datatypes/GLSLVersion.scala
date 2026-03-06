package ultraviolet.datatypes

enum GLSLVersion(
    val id: GLSLVersionId,
    val rules: List[ShaderValidationRule],
    val transformers: List[ShaderTransformer]
):
  case GLSL_100 extends GLSLVersion(
    GLSLVersionId("100"),
    GLSLVersion.GLSL100.rules,
    GLSLVersion.GLSL100.transformers
  )

  case GLSL_300 extends GLSLVersion(
    GLSLVersionId("300"),
    GLSLVersion.GLSL300.rules,
    GLSLVersion.GLSL300.transformers
  )

// TODO: Other versions: ShaderToy, Indigo.. or maybe just 'Custom'?

object GLSLVersion:

  object GLSL100:

    val rules: List[ShaderValidationRule]     = Nil
    val transformers: List[ShaderTransformer] = Nil

  object GLSL300:

    val rules: List[ShaderValidationRule]     = Nil
    val transformers: List[ShaderTransformer] = Nil
