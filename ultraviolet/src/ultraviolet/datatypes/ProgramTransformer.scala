package ultraviolet.datatypes

import scala.quoted.*

enum ProgramTransformer:
  /** Find all invocations of a function, and rename, e.g. texture2d(1) renamed to texture(1) */
  case RenameFunctionAtCallSite(from: String, to: String)

  /** Find all annotations with the 'from' name, and rename, e.g. varying renamed to in */
  case RenameAnnotation(from: String, to: String)

  /** Find a function with a given argument, and annotate it, e.g. void foo(vec4 x) annotated to void foo(out vec4 x)
    */
  case AnnotateFunctionArgument(functionName: String, argumentName: String, annotation: String)

  /** Alter a functions return type, e.g. vec4 foo() becomes void foo() */
  case ChangeFunctionReturnType(functionName: String, newReturnType: String)

  /** Assign instead of returning, e.g. the last line of a function is vec4(1.0f) assign to a variable called COLOR:
    * `COLOR = vec4(1.0f)`
    */
  case AssignFunctionReturnValueToVariable(functionName: String, outVariableName: String)

  /** Combines convert a pure function into a mutation.
    *
    * Example: `def foo(): vec4 = vec4(1.0f)` becomes `def foo(): Unit = COLOR = vec4(1.0f)`
    *
    * This is a combination of ChangeFunctionReturnType + AssignFunctionReturnValueToVariable done in a single action.
    */
  case ConvertPureFunctionToAssignment(functionName: String, outVariableName: String)

object ProgramTransformer:

  given ToExpr[ProgramTransformer] with {
    def apply(x: ProgramTransformer)(using Quotes): Expr[ProgramTransformer] =
      x match
        case ProgramTransformer.RenameFunctionAtCallSite(from, to) =>
          '{ ProgramTransformer.RenameFunctionAtCallSite(${ Expr(from) }, ${ Expr(to) }) }

        case ProgramTransformer.RenameAnnotation(from, to) =>
          '{ ProgramTransformer.RenameAnnotation(${ Expr(from) }, ${ Expr(to) }) }

        case ProgramTransformer.AnnotateFunctionArgument(functionName, argumentName, annotation) =>
          '{
            ProgramTransformer.AnnotateFunctionArgument(
              ${ Expr(functionName) },
              ${ Expr(argumentName) },
              ${ Expr(annotation) }
            )
          }

        case ProgramTransformer.ChangeFunctionReturnType(functionName, newReturnType) =>
          '{ ProgramTransformer.ChangeFunctionReturnType(${ Expr(functionName) }, ${ Expr(newReturnType) }) }

        case ProgramTransformer.AssignFunctionReturnValueToVariable(functionName, outVariableName) =>
          '{
            ProgramTransformer.AssignFunctionReturnValueToVariable(${ Expr(functionName) }, ${ Expr(outVariableName) })
          }

        case ProgramTransformer.ConvertPureFunctionToAssignment(functionName, outVariableName) =>
          '{ ProgramTransformer.ConvertPureFunctionToAssignment(${ Expr(functionName) }, ${ Expr(outVariableName) }) }
  }

  given FromExpr[ProgramTransformer] with
    def unapply(x: Expr[ProgramTransformer])(using Quotes): Option[ProgramTransformer] =
      x match
        case '{ ProgramTransformer.RenameFunctionAtCallSite(${ Expr(from) }, ${ Expr(to) }) } =>
          Some(ProgramTransformer.RenameFunctionAtCallSite(from, to))

        case '{ ProgramTransformer.RenameAnnotation(${ Expr(from) }, ${ Expr(to) }) } =>
          Some(ProgramTransformer.RenameAnnotation(from, to))

        case '{
              ProgramTransformer.AnnotateFunctionArgument(
                ${ Expr(functionName) },
                ${ Expr(argumentName) },
                ${ Expr(annotation) }
              )
            } =>
          Some(ProgramTransformer.AnnotateFunctionArgument(functionName, argumentName, annotation))

        case '{ ProgramTransformer.ChangeFunctionReturnType(${ Expr(functionName) }, ${ Expr(newReturnType) }) } =>
          Some(ProgramTransformer.ChangeFunctionReturnType(functionName, newReturnType))

        case '{
              ProgramTransformer.AssignFunctionReturnValueToVariable(
                ${ Expr(functionName) },
                ${ Expr(outVariableName) }
              )
            } =>
          Some(ProgramTransformer.AssignFunctionReturnValueToVariable(functionName, outVariableName))

        case '{
              ProgramTransformer.ConvertPureFunctionToAssignment(${ Expr(functionName) }, ${ Expr(outVariableName) })
            } =>
          Some(ProgramTransformer.ConvertPureFunctionToAssignment(functionName, outVariableName))

        case _ =>
          None

  val GLSL_100: List[ProgramTransformer] =
    List(
      ProgramTransformer.RenameAnnotation("in", "varying"),
      ProgramTransformer.RenameAnnotation("out", "varying")
    )

  val GLSL_300: List[ProgramTransformer] =
    List(
      ProgramTransformer.RenameAnnotation("attribute", "in"),
      ProgramTransformer.RenameFunctionAtCallSite("texture2D", "texture"),
      ProgramTransformer.RenameFunctionAtCallSite("textureCube", "texture")
    )
