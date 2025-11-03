package ultraviolet.datatypes

enum ShaderValid derives CanEqual:
  case Valid
  case Invalid(reasons: List[String])

object ShaderValid:
  extension (v: ShaderValid)
    def |+|(other: ShaderValid): ShaderValid =
      (v, other) match
        case (ShaderValid.Valid, ShaderValid.Valid)             => ShaderValid.Valid
        case (ShaderValid.Valid, invalid)                       => invalid
        case (invalid, ShaderValid.Valid)                       => invalid
        case (ShaderValid.Invalid(r1), ShaderValid.Invalid(r2)) => ShaderValid.Invalid(r1 ++ r2)
