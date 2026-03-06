package ultraviolet.datatypes

opaque type GLSLVersionId = String
object GLSLVersionId:

  def apply(id: String): GLSLVersionId = id

  extension (id: GLSLVersionId) def value: String = id
