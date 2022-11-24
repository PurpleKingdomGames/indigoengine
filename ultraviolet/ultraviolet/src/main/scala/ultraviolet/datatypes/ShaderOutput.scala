package ultraviolet.datatypes

final case class ShaderOutput(code: String, metadata: ShaderMetadata):
  def withCode(newCode: String): ShaderOutput =
    this.copy(code = newCode)

  def withMetaData(newMetadata: ShaderMetadata): ShaderOutput =
    this.copy(metadata = newMetadata)

object ShaderOutput:
  def empty: ShaderOutput =
    ShaderOutput("", ShaderMetadata.empty)

  def apply(code: String): ShaderOutput =
    ShaderOutput(code, ShaderMetadata.empty)

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
