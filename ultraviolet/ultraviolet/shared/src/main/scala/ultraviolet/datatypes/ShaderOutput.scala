package ultraviolet.datatypes

enum ShaderResult:
  case Error(reason: String)
  case Output(code: String, metadata: ShaderMetadata)

object ShaderResult:

  extension (r: ShaderResult)
    def toOutput: ShaderResult.Output =
      r match
        case Error(reason)    => ShaderResult.Output(reason, ShaderMetadata.empty)
        case o @ Output(_, _) => o

final case class ShaderMetadata(
    uniforms: List[ShaderField],
    ubos: List[UBODef],
    varyings: List[ShaderField]
):
  def withUniforms(newUniforms: List[ShaderField]): ShaderMetadata =
    this.copy(uniforms = newUniforms)

  def withUBOs(newUBOs: List[UBODef]): ShaderMetadata =
    this.copy(ubos = newUBOs)

  def withVaryings(newVaryings: List[ShaderField]): ShaderMetadata =
    this.copy(varyings = newVaryings)

object ShaderMetadata:
  def empty: ShaderMetadata =
    ShaderMetadata(Nil, Nil, Nil)

final case class ShaderField(name: String, typeOf: String)
