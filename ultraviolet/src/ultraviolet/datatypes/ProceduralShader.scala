package ultraviolet.datatypes

import scala.annotation.tailrec
import scala.quoted.*

final case class ProceduralShader(
    defs: List[ShaderAST],
    ubos: List[ShaderAST.UBO],
    annotations: List[ShaderAST],
    main: ShaderAST
):

  def validate(requirements: List[ProgramRequirement]): ShaderValid =
    @tailrec
    def rec(remaining: List[ProgramRequirement], proc: ProceduralShader, acc: ShaderValid): ShaderValid =
      remaining match
        case Nil =>
          acc

        case r :: rs =>
          val res =
            ProceduralShader.requirementToValidation(r)(proc)

          rec(rs, proc, acc |+| res)

    rec(requirements, this, ShaderValid.Valid)

  def applyTransformers(transformers: List[ProgramTransformer]): ProceduralShader =
    // Convert each transformer to a partial function
    val transformFunctions: List[PartialFunction[ShaderAST, ShaderAST]] =
      transformers.map {
        case ProgramTransformer.RenameAnnotation(from, to) => {
          case ShaderAST.Annotated(ShaderAST.DataTypes.ident(id), param, v @ ShaderAST.Val(_, _, _)) if id == from =>
            ShaderAST.Annotated(ShaderAST.DataTypes.ident(to), param, v)
        }

        case ProgramTransformer.RenameFunctionAtCallSite(from, to) => {
          case ShaderAST.CallFunction(id, args, returnType) if id == from =>
            ShaderAST.CallFunction(to, args, returnType)
        }

        case ProgramTransformer.AnnotateFunctionArgument(functionName, argumentName, annotation) => {
          case fn @ ShaderAST.Function(
                fnName,
                args,
                body,
                returnType
              ) if fnName == functionName =>
            val updatedArgs =
              args.map {
                case (tpe @ ShaderAST.DataTypes.ident(_), argName) if argName == argumentName =>
                  ShaderAST.Annotated(ShaderAST.DataTypes.ident(annotation), ShaderAST.Empty(), tpe) -> argName

                case arg =>
                  arg
              }

            ShaderAST.Function(
              fnName,
              updatedArgs,
              body,
              returnType
            )
        }

        case ProgramTransformer.ChangeFunctionReturnType(functionName, newReturnType) => {
          case ShaderAST.Function(name, args, body, _) if name == functionName =>
            ShaderAST.Function(name, args, body, ShaderAST.DataTypes.ident(newReturnType))
        }

        case ProgramTransformer.AssignFunctionReturnValueToVariable(functionName: String, outVariableName: String) => {
          case ShaderAST.Function(
                fnName,
                args,
                ShaderAST.Block(statements),
                returnType
              ) if fnName == functionName =>
            val nonEmpty = statements
              .filterNot(_.isEmpty)

            val (init, last) =
              if nonEmpty.length > 1 then (nonEmpty.dropRight(1), nonEmpty.takeRight(1))
              else (Nil, nonEmpty)

            ShaderAST.Function(
              fnName,
              args,
              ShaderAST.Block(
                init ++
                  List(
                    ShaderAST
                      .Assign(ShaderAST.DataTypes.ident(outVariableName), last.headOption.getOrElse(ShaderAST.Empty()))
                  )
              ),
              returnType
            )

          case ShaderAST.Function(
                fnName,
                args,
                body,
                returnType
              ) if fnName == functionName =>
            ShaderAST.Function(
              fnName,
              args,
              ShaderAST.Assign(ShaderAST.DataTypes.ident(outVariableName), body),
              returnType
            )
        }

        case ProgramTransformer.ConvertPureFunctionToAssignment(functionName: String, outVariableName: String) => {
          case ShaderAST.Function(
                fnName,
                args,
                ShaderAST.Block(statements),
                _
              ) if fnName == functionName =>
            val nonEmpty = statements
              .filterNot(_.isEmpty)

            val (init, last) =
              if nonEmpty.length > 1 then (nonEmpty.dropRight(1), nonEmpty.takeRight(1))
              else (Nil, nonEmpty)

            ShaderAST.Function(
              fnName,
              args,
              ShaderAST.Block(
                init ++
                  List(
                    ShaderAST
                      .Assign(ShaderAST.DataTypes.ident(outVariableName), last.headOption.getOrElse(ShaderAST.Empty()))
                  )
              ),
              ShaderAST.void
            )

          case ShaderAST.Function(
                fnName,
                args,
                body,
                _
              ) if fnName == functionName =>
            ShaderAST.Function(
              fnName,
              args,
              ShaderAST.Assign(ShaderAST.DataTypes.ident(outVariableName), body),
              ShaderAST.void
            )
        }
      }

    @tailrec
    def rec(remaining: List[PartialFunction[ShaderAST, ShaderAST]], acc: ShaderAST): ShaderAST =
      remaining match
        case Nil =>
          acc

        case t :: ts =>
          rec(ts, acc.traverse(t))

    // Apply to all the AST parts, and return
    this.copy(
      defs = defs.map(d => rec(transformFunctions, d)),
      annotations = annotations.map(a => rec(transformFunctions, a)),
      main = rec(transformFunctions, main)
    )

object ProceduralShader:
  given ToExpr[ProceduralShader] with {
    def apply(x: ProceduralShader)(using Quotes): Expr[ProceduralShader] =
      '{ ProceduralShader(${ Expr(x.defs) }, ${ Expr(x.ubos) }, ${ Expr(x.annotations) }, ${ Expr(x.main) }) }
  }

  def requirementToValidation(requirement: ProgramRequirement): ProceduralShader => ShaderValid =
    proc =>
      requirement match {
        case r @ ProgramRequirement.Function0Exists(fnName, rt) =>
          val exists =
            proc.main.find {
              case ShaderAST.Function(
                    fn,
                    Nil,
                    body,
                    ShaderAST.DataTypes.ident(rType)
                  ) if fn == fnName && rType == rt =>
                true

              case _ =>
                false
            }.isDefined

          if exists then ShaderValid.Valid
          else ShaderValid.Invalid(List(r.msg))

        case r @ ProgramRequirement.Function1Exists(fnName, arg, rt) =>
          val exists =
            proc.main.find {
              case ShaderAST.Function(
                    fn,
                    List(
                      ShaderAST.DataTypes.ident(argType1) -> _
                    ),
                    body,
                    ShaderAST.DataTypes.ident(rType)
                  ) if fn == fnName && argType1 == arg && rType == rt =>
                true

              case _ =>
                false
            }.isDefined

          if exists then ShaderValid.Valid
          else ShaderValid.Invalid(List(r.msg))

        case r @ ProgramRequirement.Function2Exists(fnName, arg1, arg2, rt) =>
          val exists =
            proc.main.find {
              case ShaderAST.Function(
                    fn,
                    List(
                      (ShaderAST.Annotated(
                        ShaderAST.DataTypes.ident(_),
                        ShaderAST.Empty(),
                        ShaderAST.DataTypes.ident(argType1)
                      ) -> _),
                      (ShaderAST.DataTypes.ident(argType2) -> _)
                    ),
                    body,
                    ShaderAST.DataTypes.ident(rType)
                  ) if fn == fnName && argType1 == arg1 && argType2 == arg2 && rType == rt =>
                true

              case ShaderAST.Function(
                    fn,
                    List(
                      (ShaderAST.DataTypes.ident(argType1) -> _),
                      (ShaderAST.DataTypes.ident(argType2) -> _)
                    ),
                    body,
                    ShaderAST.DataTypes.ident(rType)
                  ) if fn == fnName && argType1 == arg1 && argType2 == arg2 && rType == rt =>
                true

              case _ =>
                false
            }.isDefined

          if exists then ShaderValid.Valid
          else ShaderValid.Invalid(List(r.msg))

        case r @ ProgramRequirement.Function3Exists(fnName, arg1, arg2, arg3, rt) =>
          val exists =
            proc.main.find {
              case ShaderAST.Function(
                    fn,
                    List(
                      (ShaderAST.DataTypes.ident(argType1) -> _),
                      (ShaderAST.DataTypes.ident(argType2) -> _),
                      (ShaderAST.DataTypes.ident(argType3) -> _)
                    ),
                    body,
                    ShaderAST.DataTypes.ident(rType)
                  ) if fn == fnName && argType1 == arg1 && argType2 == arg2 && argType3 == arg3 && rType == rt =>
                true

              case _ =>
                false
            }.isDefined

          if exists then ShaderValid.Valid
          else ShaderValid.Invalid(List(r.msg))

        case r @ ProgramRequirement.UsesRequiredEnvironment(env) =>
          if proc.main.inType.exists(_ == env) then ShaderValid.Valid
          else ShaderValid.Invalid(List(r.msg))

        case r @ ProgramRequirement.ReturnsRequiredType(rt) =>
          if proc.main.outType.exists(_ == rt) then ShaderValid.Valid
          else ShaderValid.Invalid(List(r.msg))
      }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def render(p: ProceduralShader, headers: List[ShaderHeader]): ShaderResult = {
    import ShaderAST.*

    val renderedUBOs = p.ubos.map(u => ShaderPrinter.print(u).mkString("\n"))
    val renderedAnnotations = p.annotations
      .map(u => ShaderPrinter.print(u).mkString("\n"))
      .map(s => if s.startsWith("#") then s else s + ";")
    val renderedDefs = p.defs.map(d => ShaderPrinter.print(d).mkString("\n"))
    val renderedBody = ShaderPrinter.print(p.main)

    val code =
      (headers.map(_.value) ++ renderedUBOs ++ renderedAnnotations ++ renderedDefs ++ renderedBody)
        .mkString("\n")
        .trim

    val extractedUniforms: List[ShaderField] =
      p.annotations
        .filter {
          case ShaderAST.Annotated(ShaderAST.DataTypes.ident("uniform"), _, ShaderAST.Val(_, _, _)) => true
          case _                                                                                    => false
        }
        .flatMap {
          case ShaderAST.Annotated(name, param, ShaderAST.Val(id, value, typeOf)) =>
            List(
              ShaderField(
                id,
                ShaderPrinter
                  .print(typeOf)
                  .headOption
                  .getOrElse(throw ShaderError.Metadata("Uniform declaration missing return type."))
              )
            )

          case _ => Nil
        }

    val extractedVaryings: List[ShaderField] =
      p.annotations
        .filter {
          case ShaderAST.Annotated(ShaderAST.DataTypes.ident("varying"), _, ShaderAST.Val(_, _, _)) => true
          case ShaderAST.Annotated(ShaderAST.DataTypes.ident("in"), _, ShaderAST.Val(_, _, _))      => true
          case ShaderAST.Annotated(ShaderAST.DataTypes.ident("out"), _, ShaderAST.Val(_, _, _))     => true
          case _                                                                                    => false
        }
        .flatMap {
          case ShaderAST.Annotated(name, param, ShaderAST.Val(id, value, typeOf)) =>
            List(
              ShaderField(
                id,
                ShaderPrinter
                  .print(typeOf)
                  .headOption
                  .getOrElse(throw ShaderError.Metadata("Varying declaration missing return type."))
              )
            )

          case _ =>
            Nil
        }

    ShaderResult(
      code,
      ShaderMetadata(
        extractedUniforms,
        p.ubos.map(_.uboDef),
        extractedVaryings
      )
    )
  }

  extension (p: ProceduralShader)
    def exists(q: ShaderAST): Boolean =
      p.main.exists(q) || p.defs.exists(_.exists(q))

    def find(q: ShaderAST => Boolean): Option[ShaderAST] =
      p.main.find(q).orElse(p.defs.find(_.find(q).isDefined))

    def findAll(q: ShaderAST => Boolean): List[ShaderAST] =
      p.main.findAll(q) ++ p.defs.flatMap(_.findAll(q))
