package ultraviolet.datatypes

import scala.quoted.*

enum ProgramTransformer:
  /** Find all invocations of a function, and rename, e.g. texture2d(1) renamed to texture(1) */
  case RenameFunctionAtCallSite(from: String, to: String)

  /** Find all annotations with the 'from' name, and rename, e.g. varying renamed to in */
  case RenameAnnotation(from: String, to: String)

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

        case ProgramTransformer.ChangeFunctionReturnType(functionName, newReturnType) =>
          '{ ProgramTransformer.ChangeFunctionReturnType(${ Expr(functionName) }, ${ Expr(newReturnType) }) }

        case ProgramTransformer.AssignFunctionReturnValueToVariable(functionName, outVariableName) =>
          '{
            ProgramTransformer.AssignFunctionReturnValueToVariable(${ Expr(functionName) }, ${ Expr(outVariableName) })
          }

        case ProgramTransformer.ConvertPureFunctionToAssignment(functionName, outVariableName) =>
          '{ ProgramTransformer.ConvertPureFunctionToAssignment(${ Expr(functionName) }, ${ Expr(outVariableName) }) }
  }

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
