package ultraviolet.datatypes

opaque type ProgramVersionId = String
object ProgramVersionId:

  def apply(id: String): ProgramVersionId = id

  extension (id: ProgramVersionId) def value: String = id
