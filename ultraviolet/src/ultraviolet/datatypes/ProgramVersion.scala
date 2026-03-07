package ultraviolet.datatypes

final case class ProgramVersion(
    id: ProgramVersionId,
    rules: List[ProgramValidationRule],
    transformers: List[ProgramTransformer]
)

object ProgramVersion:

  val GLSL_100: ProgramVersion =
    ProgramVersion(
      ProgramVersionId("GLSL 100"),
      ProgramValidationRule.GLSL_100,
      ProgramTransformer.GLSL_100
    )

  val GLSL_300: ProgramVersion =
    ProgramVersion(
      ProgramVersionId("GLSL 300"),
      ProgramValidationRule.GLSL_300,
      ProgramTransformer.GLSL_300
    )
