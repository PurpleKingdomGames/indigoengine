package ultraviolet.datatypes

import scala.quoted.*

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

  given ToExpr[ShaderMetadata] with
    def apply(x: ShaderMetadata)(using Quotes): Expr[ShaderMetadata] =
      '{ ShaderMetadata(${ Expr(x.uniforms) }, ${ Expr(x.ubos) }, ${ Expr(x.varyings) }) }

  def empty: ShaderMetadata =
    ShaderMetadata(Nil, Nil, Nil)
