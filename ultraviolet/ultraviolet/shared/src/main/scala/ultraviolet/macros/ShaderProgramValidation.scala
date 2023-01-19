package ultraviolet.macros

import ultraviolet.datatypes.ShaderAST
import ultraviolet.datatypes.ShaderAST.Annotated
import ultraviolet.datatypes.ShaderAST.Assign
import ultraviolet.datatypes.ShaderAST.Block
import ultraviolet.datatypes.ShaderAST.CallExternalFunction
import ultraviolet.datatypes.ShaderAST.CallFunction
import ultraviolet.datatypes.ShaderAST.Cast
import ultraviolet.datatypes.ShaderAST.DataTypes.*
import ultraviolet.datatypes.ShaderAST.Empty
import ultraviolet.datatypes.ShaderAST.Field
import ultraviolet.datatypes.ShaderAST.For
import ultraviolet.datatypes.ShaderAST.FunctionRef
import ultraviolet.datatypes.ShaderAST.If
import ultraviolet.datatypes.ShaderAST.Infix
import ultraviolet.datatypes.ShaderAST.Neg
import ultraviolet.datatypes.ShaderAST.New
import ultraviolet.datatypes.ShaderAST.RawLiteral
import ultraviolet.datatypes.ShaderAST.ShaderBlock
import ultraviolet.datatypes.ShaderAST.Struct
import ultraviolet.datatypes.ShaderAST.Switch
import ultraviolet.datatypes.ShaderAST.UBO
import ultraviolet.datatypes.ShaderAST.Val
import ultraviolet.datatypes.ShaderAST.While
import ultraviolet.datatypes.*

import scala.annotation.tailrec

object ShaderProgramValidation:

  val ErrorMsgNestedFunction: String =
    "It is not permitted to nest named functions, however, you can declare nested anonymous functions."

  private def forwardRefMsg(name: String): String =
    s"${name} is an illegal forward reference."

  private def mustBeConstantMsg(name: String): String =
    s"${name} is a top level variable, and so must be a constant value or null."

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def validate(level: Int, knownRefs: List[String]): ShaderAST => ShaderAST = {
    case ast @ Empty() =>
      ast

    case Block(statements) =>
      Block(validateStatementBlock(statements, level, knownRefs))

    case ast @ Neg(value) =>
      Neg(validate(level, knownRefs)(value))

    case ast @ UBO(uboDef) =>
      ast

    case ast @ Struct(name, members) =>
      ast

    case ast @ New(name, args) =>
      ast

    case ShaderBlock(inType, outType, envVarName, statements) =>
      ShaderBlock(inType, outType, envVarName, validateStatementBlock(statements, level, knownRefs))

    case ShaderAST.Function(id, args, body, returnType) =>
      // Should not contain function
      body.find {
        case ShaderAST.Function(_, _, _, _) => true
        case _                              => false
      } match
        case Some(_) =>
          throw ShaderError.Validation(ErrorMsgNestedFunction)

        case None =>
          ()

      ShaderAST.Function(id, args, validate(level + 1, knownRefs ++ args.map(_._2))(body), returnType)

    case ast @ CallFunction(id, args, returnType) =>
      if knownRefs.contains(id) then ast
      else throw ShaderError.Validation(forwardRefMsg(id))

    case ast @ CallExternalFunction(id, args, returnType) =>
      ast

    case ast @ FunctionRef(id, arg, returnType) =>
      if knownRefs.contains(id) then ast
      else throw ShaderError.Validation(forwardRefMsg(id))

    case Cast(value, as) =>
      Cast(validate(level, knownRefs)(value), as)

    case Infix(op, left, right, returnType) =>
      Infix(op, validate(level + 1, knownRefs)(left), validate(level + 1, knownRefs)(right), returnType)

    case Assign(left, right) =>
      Assign(validate(level + 1, knownRefs)(left), validate(level + 1, knownRefs)(right))

    case If(condition, thenTerm, elseTerm) =>
      If(
        validate(level + 1, knownRefs)(condition),
        validate(level + 1, knownRefs)(thenTerm),
        elseTerm.map(validate(level + 1, knownRefs))
      )

    case While(condition, body) =>
      While(validate(level + 1, knownRefs)(condition), validate(level + 1, knownRefs)(body))

    case For(initial, condition, next, body) =>
      val checkedInit = validate(level + 1, knownRefs)(initial)

      val additionalRefs =
        checkedInit
          .findAll {
            case ShaderAST.Val(_, _, _) => true
            case _                      => false
          }
          .flatMap {
            case ShaderAST.Val(ref, _, _) => List(ref)
            case _                        => Nil
          }

      For(
        checkedInit,
        validate(level + 1, knownRefs ++ additionalRefs)(condition),
        validate(level + 1, knownRefs ++ additionalRefs)(next),
        validate(level + 1, knownRefs ++ additionalRefs)(body)
      )

    case Switch(on, cases) =>
      Switch(validate(level + 1, knownRefs)(on), cases.map(c => c._1 -> validate(level + 1, knownRefs)(c._2)))

    case Val(id, value, typeOf) =>
      if level == 0 then
        value.find {
          case ShaderAST.Function(_, _, _, _)                         => true
          case ShaderAST.FunctionRef(_, _, _)                         => true
          case ShaderAST.CallFunction(_, _, _)                        => true
          case ShaderAST.Block(List(ShaderAST.Function(_, _, _, _)))  => true
          case ShaderAST.Block(List(ShaderAST.FunctionRef(_, _, _)))  => true
          case ShaderAST.Block(List(ShaderAST.CallFunction(_, _, _))) => true
          case _                                                      => false
        } match
          case Some(_) =>
            throw ShaderError.Validation(mustBeConstantMsg(id))

          case None =>
            Val(id, validate(level + 1, knownRefs)(value), typeOf)
      else Val(id, validate(level + 1, knownRefs)(value), typeOf)

    case Annotated(name, param, value) =>
      Annotated(name, param, validate(level, knownRefs)(value))

    case ast @ RawLiteral(value) =>
      ast

    case Field(term, field) =>
      Field(validate(level, knownRefs)(term), field)

    case ast @ ident(id) =>
      if knownRefs.contains(id) then ast
      else throw ShaderError.Validation(forwardRefMsg(id))

    case ast @ external(id) =>
      ast

    case ast @ index(id, at) =>
      if knownRefs.contains(id) then ast
      else throw ShaderError.Validation(forwardRefMsg(id))

    case ast @ externalIndex(_, _) =>
      ast

    case ast @ bool(_) =>
      ast

    case ast @ float(_) =>
      ast

    case ast @ int(_) =>
      ast

    case vec2(args) =>
      vec2(args.map(validate(level, knownRefs)))

    case vec3(args) =>
      vec3(args.map(validate(level, knownRefs)))

    case vec4(args) =>
      vec4(args.map(validate(level, knownRefs)))

    case bvec2(args) =>
      bvec2(args.map(validate(level, knownRefs)))

    case bvec3(args) =>
      bvec3(args.map(validate(level, knownRefs)))

    case bvec4(args) =>
      bvec4(args.map(validate(level, knownRefs)))

    case ivec2(args) =>
      ivec2(args.map(validate(level, knownRefs)))

    case ivec3(args) =>
      ivec3(args.map(validate(level, knownRefs)))

    case ivec4(args) =>
      ivec4(args.map(validate(level, knownRefs)))

    case mat2(args) =>
      mat2(args.map(validate(level, knownRefs)))

    case mat3(args) =>
      mat3(args.map(validate(level, knownRefs)))

    case mat4(args) =>
      mat4(args.map(validate(level, knownRefs)))

    case array(size, args, typeOf) =>
      array(size, args.map(validate(level, knownRefs)), typeOf)

    case swizzle(genType, swzl, returnType) =>
      swizzle(validate(level, knownRefs)(genType), swzl, returnType)

  }

  def validateStatementBlock(statements: List[ShaderAST], level: Int, knownRefs: List[String]): List[ShaderAST] =
    @tailrec
    def rec(remaining: List[ShaderAST], newRefs: List[String], acc: List[ShaderAST]): List[ShaderAST] =
      remaining match
        case Nil =>
          acc

        case x :: xs =>
          val checked = validate(level, newRefs)(x)

          val foundRefs =
            checked
              .findAll {
                case ShaderAST.Function(_, _, _, _) => true
                case ShaderAST.FunctionRef(_, _, _) => true
                case ShaderAST.Val(_, _, _)         => true
                case _                              => false
              }
              .flatMap {
                case ShaderAST.Function(ref, _, _, _) => List(ref)
                case ShaderAST.FunctionRef(ref, _, _) => List(ref)
                case ShaderAST.Val(ref, _, _)         => List(ref)
                case _                                => Nil
              }

          rec(xs, newRefs ++ foundRefs, acc :+ checked)

    rec(statements, knownRefs, Nil)

  def validateFunctionList(statements: List[ShaderAST.Function], knownRefs: List[String]): List[ShaderAST.Function] =
    @tailrec
    def rec(
        remaining: List[ShaderAST.Function],
        newRefs: List[String],
        acc: List[ShaderAST.Function]
    ): List[ShaderAST.Function] =
      remaining match
        case Nil =>
          acc

        case x :: xs =>
          val checked = List(validate(0, newRefs)(x)).collect { case f: ShaderAST.Function =>
            f
          }

          val foundRefs =
            checked.map(_.id)

          rec(xs, newRefs ++ foundRefs, acc ++ checked)

    rec(statements, knownRefs, Nil)
