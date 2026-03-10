package ultraviolet.datatypes

import scala.quoted.*

enum ProgramRequirement(val msg: String):

  /** Ensure a named function that takes no arguments exists that returns an expected type. */
  case Function0Exists(functionName: String, returnType: String)
      extends ProgramRequirement(
        s"Program was missing a required function called '${functionName}' of type: () => $returnType"
      )

  /** Ensure a named function with a single argument exists that returns an expected type. */
  case Function1Exists(functionName: String, argumentType: String, returnType: String)
      extends ProgramRequirement(
        s"Program was missing a required function called '${functionName}' of type: $argumentType => $returnType"
      )

  /** Ensure a named function with two arguments exists that returns an expected type. */
  case Function2Exists(functionName: String, argumentType1: String, argumentType2: String, returnType: String)
      extends ProgramRequirement(
        s"Program was missing a required function called '${functionName}' of type: ($argumentType1, $argumentType2) => $returnType"
      )

  /** Ensure a named function with three arguments exists that returns an expected type. */
  case Function3Exists(
      functionName: String,
      argumentType1: String,
      argumentType2: String,
      argumentType3: String,
      returnType: String
  ) extends ProgramRequirement(
        s"Program was missing a required function called '${functionName}' of type: ($argumentType1, $argumentType2, $argumentType3) => $returnType"
      )

  /** Ensure shader program has the expected environment type. */
  case UsesRequiredEnvironment(environment: String)
      extends ProgramRequirement(s"Program did not have the required environment: '${environment}'")

  /** Ensure shader program has the expected return type. */
  case ReturnsRequiredType(returnType: String)
      extends ProgramRequirement(s"Program does not return the required '${returnType}' return type.")

object ProgramRequirement:

  given ToExpr[ProgramRequirement] with {
    def apply(x: ProgramRequirement)(using Quotes): Expr[ProgramRequirement] =
      x match
        case Function0Exists(fn, rt) =>
          '{ Function0Exists(${ Expr(fn) }, ${ Expr(rt) }) }

        case Function1Exists(fn, a, rt) =>
          '{ Function1Exists(${ Expr(fn) }, ${ Expr(a) }, ${ Expr(rt) }) }

        case Function2Exists(fn, a1, a2, rt) =>
          '{ Function2Exists(${ Expr(fn) }, ${ Expr(a1) }, ${ Expr(a2) }, ${ Expr(rt) }) }

        case Function3Exists(fn, a1, a2, a3, rt) =>
          '{ Function3Exists(${ Expr(fn) }, ${ Expr(a1) }, ${ Expr(a2) }, ${ Expr(a3) }, ${ Expr(rt) }) }

        case UsesRequiredEnvironment(env) =>
          '{ UsesRequiredEnvironment(${ Expr(env) }) }

        case ReturnsRequiredType(rt) =>
          '{ ReturnsRequiredType(${ Expr(rt) }) }
  }

  given FromExpr[ProgramRequirement] with
    def unapply(x: Expr[ProgramRequirement])(using Quotes): Option[ProgramRequirement] =
      import quotes.reflect.*

      def unwrap(term: Term): Term =
        term match
          case Inlined(_, _, inner) => unwrap(inner)
          case Typed(inner, _)      => unwrap(inner)
          case other                => other

      unwrap(x.asTerm).asExprOf[ProgramRequirement] match
        case '{ Function0Exists(${ Expr(fn) }, ${ Expr(rt) }) } =>
          Some(Function0Exists(fn, rt))

        case '{ Function1Exists(${ Expr(fn) }, ${ Expr(a) }, ${ Expr(rt) }) } =>
          Some(Function1Exists(fn, a, rt))

        case '{ Function2Exists(${ Expr(fn) }, ${ Expr(a1) }, ${ Expr(a2) }, ${ Expr(rt) }) } =>
          Some(Function2Exists(fn, a1, a2, rt))

        case '{ Function3Exists(${ Expr(fn) }, ${ Expr(a1) }, ${ Expr(a2) }, ${ Expr(a3) }, ${ Expr(rt) }) } =>
          Some(Function3Exists(fn, a1, a2, a3, rt))

        case '{ UsesRequiredEnvironment(${ Expr(env) }) } =>
          Some(UsesRequiredEnvironment(env))

        case '{ ReturnsRequiredType(${ Expr(rt) }) } =>
          Some(ReturnsRequiredType(rt))

        case e =>
          report.errorAndAbort(
            s"[Ultraviolet macro error, please report.] ProgramRequirement after unwrap expr:\n${e.asTerm
                .show(using Printer.TreeStructure)}"
          )
          None
