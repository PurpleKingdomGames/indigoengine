package ultraviolet.macros

import ultraviolet.datatypes.ShaderAST
import ultraviolet.datatypes.ShaderError

import scala.collection.mutable.ListBuffer
import scala.quoted.Quotes

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class CreateShaderAST[Q <: Quotes](using val qq: Q) extends ShaderMacroUtils:
  import qq.reflect.*

  val uboUtils                                  = new ExtractUBOUtils[qq.type](using qq)
  val proxies                                   = new ProxyManager
  val shaderDefs: ListBuffer[FunctionLookup]    = new ListBuffer()
  val structRegister: ListBuffer[String]        = new ListBuffer()
  val uboRegister: ListBuffer[ShaderAST.UBO]    = new ListBuffer()
  val annotationRegister: ListBuffer[ShaderAST] = new ListBuffer()

  def inferSwizzleType(swizzle: String): ShaderAST =
    swizzle.length match
      case 0 =>
        throw ShaderError.Unsupported("Swizzle of length 0 found.")
      case 1 => ShaderAST.DataTypes.ident("float")
      case 2 => ShaderAST.DataTypes.ident("vec2")
      case 3 => ShaderAST.DataTypes.ident("vec3")
      case 4 => ShaderAST.DataTypes.ident("vec4")
      case l =>
        throw ShaderError.Unsupported(s"Swizzle of length $l found, which is greater than the max length of 4.")

  def extractInferredType(typ: TypeTree): String =
    def mapName(name: String): String =
      Option(name)
        .map {
          case "Boolean"      => "bool"
          case "Float"        => "float"
          case "Int"          => "int"
          case "vec2"         => "vec2"
          case "vec3"         => "vec3"
          case "vec4"         => "vec4"
          case "bvec2"        => "bvec2"
          case "bvec3"        => "bvec3"
          case "bvec4"        => "bvec4"
          case "ivec2"        => "ivec2"
          case "ivec3"        => "ivec3"
          case "ivec4"        => "ivec4"
          case "mat2"         => "mat2"
          case "mat3"         => "mat3"
          case "mat4"         => "mat4"
          case "sampler2D$"   => "sampler2D"
          case "samplerCube$" => "samplerCube"
          case n              => n
        }
        .filterNot {
          case "Unit" | "array" | "Any" =>
            true
          case n if n.startsWith("Function") =>
            true
          case _ =>
            false
        }
        .getOrElse("void")

    typ match
      case Applied(TypeIdent("array"), List(Singleton(Literal(IntConstant(size))), TypeIdent(typeName))) =>
        mapName(typeName) + s"[${size.toString()}]"

      case Applied(TypeIdent("array"), List(Singleton(Ident(varName)), TypeIdent(typeName))) =>
        mapName(typeName) + s"[$varName]"

      case _ =>
        val res = mapName(typ.tpe.classSymbol.map(_.name).getOrElse("void"))

        res match
          case "void" =>
            // One last roll of the dice. If we think this is a Shader, then it
            // is a function x => Shader[?, rt], and we can grab the rt name...
            if typ.tpe.show.contains("Shader") then
              typ.tpe.typeArgs.lastOption match
                case Some(TypeRef(_, value)) =>
                  mapName(value)

                case _ =>
                  res
            else res

          case _ =>
            res

  def extractInferredTypeParam(typ: TypeTree): Option[String] =
    def extract(t: Tree): Option[String] =
      t match
        case Applied(TypeIdent("&"), List(t1, t2)) =>
          for {
            a <- extract(t1)
            b <- extract(t2)
          } yield s"""$a & $b"""

        case TypeIdent(typeName) =>
          Option(typeName)

        case _ =>
          None

    typ match
      case Applied(TypeIdent("&"), List(t1, t2)) =>
        for {
          a <- extract(t1)
          b <- extract(t2)
        } yield s"""$a & $b"""

      case TypeIdent(typeName) =>
        Option(typeName)

      case x =>
        typ.tpe.classSymbol.map(_.name)

  def isOneLineLambda(args: List[Statement]): Boolean =
    args.nonEmpty &&
      args.forall {
        case ValDef(name, _, Some(Literal(_))) if name.contains("$proxy") =>
          true

        case ValDef(name, _, Some(Ident(_))) if name.contains("$proxy") =>
          true

        case ValDef(name, _, Some(_)) =>
          name.contains("$proxy") || name.matches("""_\$[0-9]+""")

        case _ =>
          false
      }

  def assignToLast(lhs: ShaderAST): ShaderAST => ShaderAST = {
    case ShaderAST.Block(statements :+ last) =>
      ShaderAST.Block(statements :+ ShaderAST.Assign(lhs, last))

    case last =>
      ShaderAST.Assign(lhs, last)
  }

  def recursivelyAssignIf(assignTo: ShaderAST.DataTypes.ident, maybeIf: ShaderAST): ShaderAST =
    maybeIf match
      case ShaderAST.If(cond, thn, Some(els @ ShaderAST.If(_, _, Some(_)))) =>
        ShaderAST.If(
          cond,
          assignToLast(assignTo)(recursivelyAssignIf(assignTo, thn)),
          Option(recursivelyAssignIf(assignTo, els))
        )

      case ShaderAST.If(cond, thn, Some(ShaderAST.Block(statements :+ (els @ ShaderAST.If(_, _, Some(_)))))) =>
        ShaderAST.If(
          cond,
          assignToLast(assignTo)(recursivelyAssignIf(assignTo, thn)),
          Option(
            ShaderAST.Block(
              statements :+ recursivelyAssignIf(assignTo, els)
            )
          )
        )

      case ShaderAST.If(cond, thn, Some(els)) =>
        ShaderAST.If(
          cond,
          assignToLast(assignTo)(recursivelyAssignIf(assignTo, thn)),
          Option(assignToLast(assignTo)(recursivelyAssignIf(assignTo, els)))
        )

      case x =>
        x

  def buildAnnotations(v: ValDef, envVarName: Option[String], body: ShaderAST): ShaderAST =
    v.symbol.annotations
      .map(p => walkTerm(p, envVarName))
      .foldLeft(body) { case (acc, ann) =>
        ann match
          case a: ShaderAST.DataTypes.ident =>
            ShaderAST.Annotated(a, ShaderAST.Empty(), acc)

          case ShaderAST.New(anno, List(annoTerm)) =>
            ShaderAST.Annotated(ShaderAST.DataTypes.ident(anno), annoTerm, acc)

          case _ =>
            acc
      }

  def walkStatement(s: Statement, envVarName: Option[String]): ShaderAST =
    s match
      case Import(_, _) =>
        ShaderAST.Empty()

      case Export(_, _) =>
        throw ShaderError.Unsupported("Shaders do not support exports.")

      case ClassDef(name, _, _, _, _) if name.endsWith("$") =>
        throw ShaderError.Unsupported(
          "Looks like you're trying to use a case class. Shaders only support simple, flat classes."
        )

      case ClassDef(_, DefDef("<init>", List(TermParamClause(Nil)), _, None), _, _, _) =>
        throw ShaderError.Unsupported(
          "Looks like you're trying to use a trait or a class with no members. Shaders only support simple, flat classes with members."
        )

      case ClassDef(name, DefDef("<init>", List(TermParamClause(params)), _, None), _, _, _) =>
        structRegister += name
        ShaderAST.Struct(name, params.map(p => walkTree(p, envVarName)))

      case ClassDef(_, _, _, _, _) =>
        throw ShaderError.Unsupported("Shaders only support simple, flat classes.")

      case TypeDef(_, _) =>
        throw ShaderError.Unsupported("Shaders do not support fancy types. :-)")

      case ValDef(name, _, _) if isGLSLReservedWord(name) =>
        throw ShaderError.GLSLReservedWord(name)

      case v @ ValDef(name, typ, Some(term)) =>
        val body = walkTerm(term, envVarName)

        val typeOf: ShaderAST =
          extractInferredType(typ) match
            case "void" =>
              body match
                case ShaderAST.CallFunction(name, _, _) =>
                  shaderDefs
                    .find(_.fn.id == name)
                    .map(_.fn.returnType)
                    .getOrElse(ShaderAST.unknownType)

                case _ =>
                  ShaderAST.unknownType

            case s =>
              ShaderAST.DataTypes.ident(s)

        body match
          case ShaderAST.FunctionRef(fnName, fnArg, fnOutType) =>
            proxies.add(name, fnName, fnArg, fnOutType)
            ShaderAST.Empty()

          case ShaderAST.Block(List(r @ ShaderAST.FunctionRef(id, arg, rt))) =>
            proxies.add(name, id, arg, rt)
            ShaderAST.Empty()

          case ShaderAST.Block(statements :+ (ifs @ ShaderAST.If(_, _, Some(_)))) =>
            val resVal: ShaderAST.DataTypes.ident = ShaderAST.DataTypes.ident(name)
            ShaderAST.Block(
              statements ++ List(
                ShaderAST.Val(name, ShaderAST.Empty(), typeOf),
                recursivelyAssignIf(resVal, ifs)
              )
            )

          case ShaderAST.Block(statements :+ ShaderAST.Switch(on, cases)) =>
            val resVal = ShaderAST.DataTypes.ident(name)
            ShaderAST.Block(
              statements ++ List(
                ShaderAST.Val(name, ShaderAST.Empty(), typeOf),
                ShaderAST.Switch(
                  on,
                  cases.map { case (i, c) =>
                    i -> assignToLast(resVal)(c)
                  }
                )
              )
            )

          case ifs @ ShaderAST.If(_, _, Some(_)) =>
            val resVal: ShaderAST.DataTypes.ident = ShaderAST.DataTypes.ident(name)
            ShaderAST.Block(
              List(
                ShaderAST.Val(name, ShaderAST.Empty(), typeOf),
                recursivelyAssignIf(resVal, ifs)
              )
            )

          case ShaderAST.Switch(on, cases) =>
            val resVal = ShaderAST.DataTypes.ident(name)
            ShaderAST.Block(
              List(
                ShaderAST.Val(name, ShaderAST.Empty(), typeOf),
                ShaderAST.Switch(
                  on,
                  cases.map { case (i, c) =>
                    i -> assignToLast(resVal)(c)
                  }
                )
              )
            )

          case _ =>
            val vv =
              body match
                case arr @ ShaderAST.DataTypes.array(size, args, typeOfArray) =>
                  ShaderAST.Val(
                    name,
                    body,
                    typeOfArray
                  )

                case _ =>
                  ShaderAST.Val(name, body, typeOf)

            buildAnnotations(v, envVarName, vv) match
              case a @ ShaderAST.Annotated(_, _, _) =>
                annotationRegister += buildAnnotations(v, envVarName, vv)
                ShaderAST.Empty()

              case a =>
                a

      case v @ ValDef(name, typ, None) =>
        val typeOf = ShaderAST.DataTypes.ident(extractInferredType(typ))
        val vv     = ShaderAST.Val(name, ShaderAST.Empty(), typeOf)

        buildAnnotations(v, envVarName, vv) match
          case a @ ShaderAST.Annotated(_, _, _) =>
            annotationRegister += buildAnnotations(v, envVarName, vv)
            ShaderAST.Empty()

          case a =>
            a

      case DefDef(name, _, _, _) if isGLSLReservedWord(name) =>
        throw ShaderError.GLSLReservedWord(name)

      case d @ DefDef(fnName, args, rt, Some(term)) =>
        val argNamesTypes =
          args
            .collect { case TermParamClause(ps) => ps }
            .flatten
            .collect { case ValDef(name, typ, _) =>
              val vName =
                if name.contains("$") then
                  val n = proxies.makeVarName
                  proxies.add(name, n)
                  n
                else name
              val typeOf = extractInferredType(typ)
              (typeOf, vName)
            }

        val isAnon = fnName == "$anonfun"
        val fn     = if isAnon then proxies.makeDefName else fnName
        val body   = walkTerm(term, envVarName)

        val returnType =
          extractInferredType(rt) match
            case "void" =>
              rt match
                case rtt @ TypeIdent(_) =>
                  walkTree(rtt, envVarName)

                case _ =>
                  findReturnType(body)

            case s =>
              ShaderAST.DataTypes.ident(s)

        def register(fnDef: ShaderAST.Function, isAnon: Boolean): ShaderAST =
          shaderDefs += FunctionLookup(fnDef, !isAnon)

          if isAnon then ShaderAST.FunctionRef(fn, fnDef.args.map(_._1), returnType)
          else fnDef

        body match
          case ShaderAST.Block(List(ref @ ShaderAST.FunctionRef(id, arg, rt))) =>
            proxies.add(fn, id, arg, rt)
            ref

          case ShaderAST.Block(statements :+ (ifs @ ShaderAST.If(_, _, Some(_)))) =>
            val name                              = proxies.makeVarName
            val resVal: ShaderAST.DataTypes.ident = ShaderAST.DataTypes.ident(name)
            val typeOf                            = ShaderAST.DataTypes.ident(extractInferredType(rt))
            val fnBody =
              ShaderAST.Block(
                List(
                  ShaderAST.Val(name, ShaderAST.Empty(), typeOf),
                  recursivelyAssignIf(resVal, ifs),
                  resVal
                )
              )

            register(
              ShaderAST.Function(
                fn,
                argNamesTypes.map(p => ShaderAST.DataTypes.ident(p._1) -> p._2),
                ShaderAST.Block(statements ++ fnBody.statements),
                returnType
              ),
              isAnon
            )

          case ShaderAST.Block(statements :+ ShaderAST.Switch(on, cases))
              if !(returnType == ShaderAST.DataTypes.ident("void")) =>
            val name   = proxies.makeVarName
            val resVal = ShaderAST.DataTypes.ident(name)
            val typeOf = ShaderAST.DataTypes.ident(extractInferredType(rt))
            val fnBody =
              ShaderAST.Block(
                List(
                  ShaderAST.Val(name, ShaderAST.Empty(), typeOf),
                  ShaderAST.Switch(
                    on,
                    cases.map { case (i, c) =>
                      i -> assignToLast(resVal)(c)
                    }
                  ),
                  resVal
                )
              )

            register(
              ShaderAST.Function(
                fn,
                argNamesTypes.map(p => ShaderAST.DataTypes.ident(p._1) -> p._2),
                ShaderAST.Block(statements ++ fnBody.statements),
                returnType
              ),
              isAnon
            )

          case ifs @ ShaderAST.If(_, _, Some(_)) =>
            val name                              = proxies.makeVarName
            val resVal: ShaderAST.DataTypes.ident = ShaderAST.DataTypes.ident(name)
            val typeOf                            = ShaderAST.DataTypes.ident(extractInferredType(rt))
            val fnBody =
              ShaderAST.Block(
                List(
                  ShaderAST.Val(name, ShaderAST.Empty(), typeOf),
                  recursivelyAssignIf(resVal, ifs),
                  resVal
                )
              )

            register(
              ShaderAST.Function(
                fn,
                argNamesTypes.map(p => ShaderAST.DataTypes.ident(p._1) -> p._2),
                fnBody,
                returnType
              ),
              isAnon
            )

          case ShaderAST.Switch(on, cases) if !(returnType == ShaderAST.DataTypes.ident("void")) =>
            val name   = proxies.makeVarName
            val resVal = ShaderAST.DataTypes.ident(name)
            val typeOf = ShaderAST.DataTypes.ident(extractInferredType(rt))
            val fnBody =
              ShaderAST.Block(
                List(
                  ShaderAST.Val(name, ShaderAST.Empty(), typeOf),
                  ShaderAST.Switch(
                    on,
                    cases.map { case (i, c) =>
                      i -> assignToLast(resVal)(c)
                    }
                  ),
                  resVal
                )
              )

            register(
              ShaderAST.Function(
                fn,
                argNamesTypes.map(p => ShaderAST.DataTypes.ident(p._1) -> p._2),
                fnBody,
                returnType
              ),
              isAnon
            )

          case b =>
            register(
              ShaderAST.Function(
                fn,
                argNamesTypes.map(p => ShaderAST.DataTypes.ident(p._1) -> p._2),
                body,
                returnType
              ),
              isAnon
            )

      case DefDef(_, _, _, _) =>
        throw ShaderError.UnexpectedConstruction("Unexpected def construction")

      case t: Term =>
        walkTerm(t, envVarName)

      case x =>
        val sample = Printer.TreeStructure.show(x).take(100)
        throw ShaderError.UnexpectedConstruction("Unexpected Statement: " + sample + "(..)")

  def walkTree(t: Tree, envVarName: Option[String]): ShaderAST =
    t match
      case TypeIdent("Unit") =>
        ShaderAST.DataTypes.ident("void")

      case TypeIdent("Boolean") =>
        ShaderAST.DataTypes.ident("bool")

      case TypeIdent("Float") =>
        ShaderAST.DataTypes.ident("float")

      case TypeIdent("Int") =>
        ShaderAST.DataTypes.ident("int")

      case TypeIdent(name) =>
        ShaderAST.DataTypes.ident(name)

      case PackageClause(_, _) =>
        throw ShaderError.Unsupported("Shaders do not support packages.")

      case s: Statement =>
        walkStatement(s, envVarName)

      case Applied(TypeIdent("Shader"), _) =>
        ShaderAST.DataTypes.ident("void")

      case x =>
        val sample = Printer.TreeStructure.show(x).take(100)
        throw ShaderError.UnexpectedConstruction("Unexpected Tree: " + sample + "(..)")

  def walkTerm(t: Term, envVarName: Option[String]): ShaderAST =
    t match

      // Specific hooks we care about

      // Entry point 'from file'
      case Inlined(
            Some(
              Apply(Select(Ident("Shader"), "fromFile"), List(Literal(StringConstant(_))))
            ),
            _,
            Typed(
              TypeApply(
                Select(
                  Inlined(
                    Some(
                      Apply(
                        Select(Ident("Shader"), "apply"),
                        List(
                          Inlined(
                            Some(_),
                            Nil,
                            Typed(
                              Inlined(
                                Some(TypeIdent(_)),
                                Nil,
                                term
                              ),
                              _
                            )
                          )
                        )
                      )
                    ),
                    _,
                    _
                  ),
                  _
                ),
                _
              ),
              _
            )
          ) =>
        walkTree(term, None)

      // Entry point (with type params) (block)
      case Apply(
            TypeApply(Select(Ident("Shader"), "apply"), types),
            List(
              Block(
                Nil,
                Block(
                  List(
                    DefDef(
                      "$anonfun",
                      List(TermParamClause(List(ValDef(env, Inferred(), None)))),
                      Inferred(),
                      Some(term)
                    )
                  ),
                  Closure(Ident("$anonfun"), None)
                )
              )
            )
          ) =>
        val e          = Option(env)
        val statements = List(walkTerm(term, e))

        types.map(extractInferredTypeParam) match
          case List(in, out) =>
            ShaderAST.ShaderBlock(in, out, e, statements)

          case List(in) =>
            ShaderAST.ShaderBlock(in, None, e, statements)

          case _ =>
            ShaderAST.ShaderBlock(None, None, e, statements)

      // Entry point (with type params) (single line)
      case Apply(
            TypeApply(Select(Ident("Shader"), "apply"), types),
            List(
              Block(
                List(
                  DefDef(
                    "$anonfun",
                    List(TermParamClause(List(ValDef(env, Inferred(), None)))),
                    Inferred(),
                    Some(term)
                  )
                ),
                Closure(Ident("$anonfun"), None)
              )
            )
          ) =>
        val e          = Option(env)
        val statements = List(walkTerm(term, e))

        types.map(extractInferredTypeParam) match
          case List(in, out) =>
            ShaderAST.ShaderBlock(in, out, e, statements)

          case List(in) =>
            ShaderAST.ShaderBlock(in, None, e, statements)

          case _ =>
            ShaderAST.ShaderBlock(None, None, e, statements)

      // Entry point 'run' ignored
      case x @ Apply(Apply(TypeApply(Select(Ident("Shader"), "run"), _), List(term)), _) =>
        walkTerm(term, envVarName)

      // Entry point (no type params)
      case Apply(Select(Ident("Shader"), "apply"), args) =>
        ShaderAST.ShaderBlock(None, None, None, args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Ident("RawGLSL"), "apply"), List(term)) =>
        walkTerm(term, envVarName)

      // For loops

      case Apply(
            Apply(
              TypeApply(Ident(forLoopName), _),
              List(
                Apply(
                  TypeApply(Select(Ident(tupleIdent), "apply"), _),
                  tupleArgs
                ),
                Block(
                  List(DefDef("$anonfun", _, _, Some(condition))),
                  Closure(Ident("$anonfun"), None)
                ),
                Block(
                  List(DefDef("$anonfun", _, Inferred(), Some(Apply(_, nextArgs)))),
                  Closure(Ident("$anonfun"), None)
                )
              )
            ),
            List(
              Block(
                Nil,
                Block(
                  List(
                    DefDef(
                      "$anonfun",
                      List(TermParamClause(valdefs)),
                      _,
                      Some(body)
                    )
                  ),
                  Closure(Ident("$anonfun"), None)
                )
              )
            )
          ) if (forLoopName == "cfor" || forLoopName == "_for") && isTuple.matches(tupleIdent) && valdefs.forall {
            case ValDef(_, _, None) => true
          } =>
        val valNames = valdefs.map { case ValDef(name, _, _) =>
          if name.contains("$") then proxies.makeVarName else name
        }
        val vals = tupleArgs.zip(valNames).map { case (init, name) =>
          ShaderAST.Val(
            name,
            walkTerm(init, envVarName),
            findReturnType(walkTerm(init, envVarName))
          )
        }
        val retType   = findReturnType(vals.head)
        val multiVals = ShaderAST.MultiStatements(retType, vals)

        val n = ShaderAST.MultiStatements(
          ShaderAST.Empty(),
          (valNames zip nextArgs).map { (varName, next) =>
            ShaderAST.Assign(
              ShaderAST.DataTypes.ident(varName),
              walkTerm(next, envVarName)
            )
          }
        )

        val b = walkTerm(body, envVarName)
        val c = walkTerm(condition, envVarName)
        ShaderAST.For(multiVals, c, n, b)

      case Apply(
            Apply(
              TypeApply(Ident(forLoopName), _),
              List(
                initial,
                Block(
                  List(DefDef("$anonfun", _, _, Some(condition))),
                  Closure(Ident("$anonfun"), None)
                ),
                Block(
                  List(DefDef("$anonfun", _, _, Some(next))),
                  Closure(Ident("$anonfun"), None)
                )
              )
            ),
            List(
              Block(
                Nil,
                Block(
                  List(
                    DefDef(
                      "$anonfun",
                      List(TermParamClause(List(ValDef(maybeName, _, None)))),
                      _,
                      Some(body)
                    )
                  ),
                  _
                )
              )
            )
          ) if forLoopName == "cfor" || forLoopName == "_for" =>
        val varName = if maybeName.contains("$") then proxies.makeVarName else maybeName
        val init    = walkTerm(initial, envVarName)
        val i = ShaderAST.Val(
          varName,
          init,
          findReturnType(init)
        )

        val c = walkTerm(condition, envVarName).traverse {
          case ShaderAST.DataTypes.ident(id) if id.startsWith("_") =>
            ShaderAST.DataTypes.ident(varName)

          case ShaderAST.Infix(op, left, right, rt) =>
            ShaderAST.Infix(op, left, right, ShaderAST.DataTypes.ident("int"))

          case x =>
            x
        }

        def replaceName: PartialFunction[ShaderAST, ShaderAST] = {
          case ShaderAST.DataTypes.ident(id) if id.startsWith("_") =>
            ShaderAST.DataTypes.ident(varName)
        }

        val n = ShaderAST.Assign(
          ShaderAST.DataTypes.ident(varName),
          walkTerm(next, envVarName).traverse(replaceName)
        )

        val b = walkTerm(body, envVarName).traverse(replaceName)

        ShaderAST.For(i, c, n, b)

      // Primitives

      case Apply(Select(Ident("vec2"), "apply"), args) =>
        args match
          case List(Typed(Repeated(args2, _), _)) =>
            ShaderAST.DataTypes.vec2(args2.map(p => walkTerm(p, envVarName)))
          case _ =>
            ShaderAST.DataTypes.vec2(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Ident("vec3"), "apply"), args) =>
        args match
          case List(Typed(Repeated(args2, _), _)) =>
            ShaderAST.DataTypes.vec3(args2.map(p => walkTerm(p, envVarName)))
          case _ =>
            ShaderAST.DataTypes.vec3(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Ident("vec4"), "apply"), args) =>
        args match
          case List(Typed(Repeated(args2, _), _)) =>
            ShaderAST.DataTypes.vec4(args2.map(p => walkTerm(p, envVarName)))
          case _ =>
            ShaderAST.DataTypes.vec4(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Ident("bvec2"), "apply"), args) =>
        args match
          case List(Typed(Repeated(args2, _), _)) =>
            ShaderAST.DataTypes.bvec2(args2.map(p => walkTerm(p, envVarName)))
          case _ =>
            ShaderAST.DataTypes.bvec2(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Ident("bvec3"), "apply"), args) =>
        args match
          case List(Typed(Repeated(args2, _), _)) =>
            ShaderAST.DataTypes.bvec3(args2.map(p => walkTerm(p, envVarName)))
          case _ =>
            ShaderAST.DataTypes.bvec3(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Ident("bvec4"), "apply"), args) =>
        args match
          case List(Typed(Repeated(args2, _), _)) =>
            ShaderAST.DataTypes.bvec4(args2.map(p => walkTerm(p, envVarName)))
          case _ =>
            ShaderAST.DataTypes.bvec4(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Ident("ivec2"), "apply"), args) =>
        args match
          case List(Typed(Repeated(args2, _), _)) =>
            ShaderAST.DataTypes.ivec2(args2.map(p => walkTerm(p, envVarName)))
          case _ =>
            ShaderAST.DataTypes.ivec2(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Ident("ivec3"), "apply"), args) =>
        args match
          case List(Typed(Repeated(args2, _), _)) =>
            ShaderAST.DataTypes.ivec3(args2.map(p => walkTerm(p, envVarName)))
          case _ =>
            ShaderAST.DataTypes.ivec3(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Ident("ivec4"), "apply"), args) =>
        args match
          case List(Typed(Repeated(args2, _), _)) =>
            ShaderAST.DataTypes.ivec4(args2.map(p => walkTerm(p, envVarName)))
          case _ =>
            ShaderAST.DataTypes.ivec4(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Ident("mat2"), "apply"), args) =>
        args match
          case List(Typed(Repeated(args2, _), _)) =>
            ShaderAST.DataTypes.mat2(args2.map(p => walkTerm(p, envVarName)))
          case _ =>
            ShaderAST.DataTypes.mat2(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Ident("mat3"), "apply"), args) =>
        args match
          case List(Typed(Repeated(args2, _), _)) =>
            ShaderAST.DataTypes.mat3(args2.map(p => walkTerm(p, envVarName)))
          case _ =>
            ShaderAST.DataTypes.mat3(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Ident("mat4"), "apply"), args) =>
        args match
          case List(Typed(Repeated(args2, _), _)) =>
            ShaderAST.DataTypes.mat4(args2.map(p => walkTerm(p, envVarName)))
          case _ =>
            ShaderAST.DataTypes.mat4(args.map(p => walkTerm(p, envVarName)))

      //

      case Apply(Select(Ident(id), "apply"), args) =>
        val proxy = proxies.lookUp(id, Proxy(id, Nil, ShaderAST.unknownType))
        ShaderAST.CallFunction(proxy.name, args.map(x => walkTerm(x, envVarName)), proxy.returnType)

      // map / flatMap

      case Apply(TypeApply(Apply(TypeApply(Select(Ident("Shader"), op), _), List(shaderf)), _), List(mapf))
          if op == "map" || op == "flatMap" =>
        (walkTerm(shaderf, envVarName), walkTerm(mapf, envVarName)) match
          case (
                ShaderAST.ShaderBlock(
                  _,
                  _,
                  _,
                  List(
                    ShaderAST.Block(statements :+ last)
                  )
                ),
                ShaderAST.Block(List(ShaderAST.FunctionRef(fnName, _, rt)))
              ) =>
            ShaderAST.Block(
              statements :+
                ShaderAST.CallFunction(fnName, List(last), rt)
            )

          case (
                ShaderAST.ShaderBlock(_, _, _, List(only)),
                ShaderAST.Block(List(ShaderAST.FunctionRef(fnName, _, rt)))
              ) =>
            ShaderAST.Block(
              ShaderAST.CallFunction(fnName, List(only), rt)
            )

          case (
                ShaderAST.Block(List(only)),
                ShaderAST.Block(List(ShaderAST.FunctionRef(fnName, _, rt)))
              ) =>
            ShaderAST.Block(
              ShaderAST.CallFunction(fnName, List(only), rt)
            )

          case (
                ShaderAST.ShaderBlock(
                  _,
                  _,
                  _,
                  List(
                    ShaderAST.Block(statements :+ last)
                  )
                ),
                ShaderAST.FunctionRef(fnName, _, rt)
              ) =>
            ShaderAST.Block(
              statements :+
                ShaderAST.CallFunction(fnName, List(last), rt)
            )

          case (
                ShaderAST.ShaderBlock(_, _, _, List(only)),
                ShaderAST.FunctionRef(fnName, _, rt)
              ) =>
            ShaderAST.Block(
              ShaderAST.CallFunction(fnName, List(only), rt)
            )

          case (
                ShaderAST.Block(List(only)),
                ShaderAST.FunctionRef(fnName, _, rt)
              ) =>
            ShaderAST.Block(
              ShaderAST.CallFunction(fnName, List(only), rt)
            )

          case (
                only,
                ShaderAST.Block(List(ShaderAST.FunctionRef(fnName, _, rt)))
              ) =>
            ShaderAST.Block(
              ShaderAST.CallFunction(fnName, List(only), rt)
            )

          case _ =>
            throw ShaderError.UnexpectedConstruction(s"Unexpected structure when processing Shader.$op operation.")

      //

      case Apply(TypeApply(Select(g, op), _), List(f)) if op == "compose" || op == "andThen" =>
        def toProxy(t: Term): Proxy =
          t match
            case Ident(name) =>
              proxies.lookUp(name)

            case x =>
              walkTerm(x, envVarName) match
                case r: ShaderAST.FunctionRef =>
                  Proxy(r.id, r.arg, r.returnType)

                case ShaderAST.Block(List(r @ ShaderAST.FunctionRef(_, _, _))) =>
                  Proxy(r.id, r.arg, r.returnType)

                case ShaderAST.CallFunction(id, args, returnType) =>
                  Proxy(id, args, returnType)

                case _ =>
                  throw ShaderError.UnexpectedConstruction(
                    "You appear to be composing something other that a function."
                  )

        val gg: Proxy = toProxy(g)
        val ff: Proxy = toProxy(f)

        val gProxy = if op == "compose" then gg else ff
        val fProxy = if op == "compose" then ff else gg

        val fnName = proxies.makeDefName
        val vName  = proxies.makeVarName
        val fnInType =
          fProxy.argType.headOption
            .getOrElse(ShaderAST.DataTypes.ident("void"))

        val body =
          ShaderAST.CallFunction(
            id = gProxy.name,
            args = List(
              ShaderAST.CallFunction(
                id = fProxy.name,
                args = List(ShaderAST.DataTypes.ident(vName)),
                returnType = fProxy.returnType
              )
            ),
            returnType = gProxy.returnType
          )

        shaderDefs += FunctionLookup(
          ShaderAST.Function(
            fnName,
            List(fnInType -> vName),
            body,
            gProxy.returnType
          ),
          false
        )
        ShaderAST.FunctionRef(fnName, gProxy.argType, gProxy.returnType)

      // Generally walking the tree

      case Apply(TypeApply(term, _), List(x)) =>
        walkTerm(x, envVarName)

      // Extension method applies...
      case Apply(Select(Select(Inlined(_, _, _), "vec2"), "apply"), args) =>
        ShaderAST.DataTypes.vec2(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Select(Inlined(_, _, _), "vec3"), "apply"), args) =>
        ShaderAST.DataTypes.vec3(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Select(Inlined(_, _, _), "vec4"), "apply"), args) =>
        ShaderAST.DataTypes.vec4(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Select(Inlined(_, _, _), "bvec2"), "apply"), args) =>
        ShaderAST.DataTypes.bvec2(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Select(Inlined(_, _, _), "bvec3"), "apply"), args) =>
        ShaderAST.DataTypes.bvec3(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Select(Inlined(_, _, _), "bvec4"), "apply"), args) =>
        ShaderAST.DataTypes.bvec4(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Select(Inlined(_, _, _), "ivec2"), "apply"), args) =>
        ShaderAST.DataTypes.ivec2(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Select(Inlined(_, _, _), "ivec3"), "apply"), args) =>
        ShaderAST.DataTypes.ivec3(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Select(Inlined(_, _, _), "ivec4"), "apply"), args) =>
        ShaderAST.DataTypes.ivec4(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Select(Inlined(_, _, _), "mat2"), "apply"), args) =>
        ShaderAST.DataTypes.mat2(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Select(Inlined(_, _, _), "mat3"), "apply"), args) =>
        ShaderAST.DataTypes.mat3(args.map(p => walkTerm(p, envVarName)))

      case Apply(Select(Select(Inlined(_, _, _), "mat4"), "apply"), args) =>
        ShaderAST.DataTypes.mat4(args.map(p => walkTerm(p, envVarName)))

      // Casting

      case Select(term, "toInt") =>
        ShaderAST.Cast(walkTerm(term, envVarName), "int")

      case Select(term, "toFloat") =>
        ShaderAST.Cast(walkTerm(term, envVarName), "float")

      case Select(term, "toBoolean") =>
        ShaderAST.Cast(walkTerm(term, envVarName), "bool")

      case Apply(Ident("toInt"), List(term)) =>
        ShaderAST.Cast(walkTerm(term, envVarName), "int")

      case Apply(Ident("toFloat"), List(term)) =>
        ShaderAST.Cast(walkTerm(term, envVarName), "float")

      case Apply(Ident("toBoolean"), List(term)) =>
        ShaderAST.Cast(walkTerm(term, envVarName), "bool")

      // Read a field

      case Select(Ident(name), "unary_-") =>
        val n = proxies.lookUp(name).name
        ShaderAST.Neg(ShaderAST.DataTypes.ident(n))

      case Select(Ident(name), "unary_!") =>
        val n = proxies.lookUp(name).name
        ShaderAST.Not(ShaderAST.DataTypes.ident(n))

      case Select(Ident(namespace), name) =>
        val ns = proxies.lookUp(namespace).name
        val n  = proxies.lookUp(name).name
        envVarName match
          case Some(value) if value == ns =>
            ShaderAST.DataTypes.external(n)

          case _ =>
            ShaderAST.Field(
              ShaderAST.DataTypes.ident(ns),
              ShaderAST.DataTypes.ident(n)
            )

      // Read a field - but of something inlined, negated, e.g. an argument of -position
      case Select(Inlined(None, Nil, Ident(name)), "unary_-") =>
        val n = proxies.lookUp(name).name
        ShaderAST.Neg(ShaderAST.DataTypes.ident(n))

      case Select(Inlined(None, Nil, Ident(obj)), fieldName) =>
        ShaderAST.Field(
          ShaderAST.DataTypes.ident(obj),
          ShaderAST.DataTypes.ident(fieldName)
        )

      // Read a field - but of something namespaced, negated, e.g. -position.x
      case Select(Select(Ident(namespace), name), "unary_-") =>
        envVarName match
          case Some(value) if value == namespace =>
            ShaderAST.Neg(ShaderAST.DataTypes.external(s"$name"))

          case _ =>
            ShaderAST.Neg(
              ShaderAST.Field(
                ShaderAST.DataTypes.ident(namespace),
                ShaderAST.DataTypes.ident(name)
              )
            )

      // Read a field - but of something namespaced, e.g. env.Position.x
      case Select(Select(Ident(namespace), name), field) =>
        envVarName match
          case Some(value) if value == namespace =>
            ShaderAST.Field(
              ShaderAST.DataTypes.external(name),
              ShaderAST.DataTypes.ident(field)
            )

          case _ =>
            ShaderAST.Field(
              ShaderAST.DataTypes.ident(namespace),
              ShaderAST.Field(
                ShaderAST.DataTypes.ident(name),
                ShaderAST.DataTypes.ident(field)
              )
            )

      // Read a field - but of something namespaced, negated, e.g. -env.Position.x
      case Select(Select(Select(Ident(namespace), name), field), "unary_-") =>
        envVarName match
          case Some(value) if value == namespace =>
            ShaderAST.Neg(
              ShaderAST.Field(
                ShaderAST.DataTypes.external(name),
                ShaderAST.DataTypes.ident(field)
              )
            )

          case _ =>
            ShaderAST.Neg(
              ShaderAST.Field(
                ShaderAST.DataTypes.ident(namespace),
                ShaderAST.Field(
                  ShaderAST.DataTypes.ident(name),
                  ShaderAST.DataTypes.ident(field)
                )
              )
            )

      // Read a component of an array
      case Select(
            term @ Apply(
              Apply(TypeApply(Select(Ident("array"), "apply"), _), List(_)),
              List(_)
            ),
            component
          ) =>
        ShaderAST.Field(
          walkTerm(term, envVarName),
          ShaderAST.DataTypes.ident(component)
        )

      // Native method call.
      case Apply(Ident(name), List(Inlined(None, Nil, Ident(defRef)))) =>
        val proxy = proxies.lookUp(defRef)
        val args: List[ShaderAST] =
          proxies.lookUpInlineReplace(proxy.name) match
            case Some(value) =>
              List(value)

            case None =>
              List(ShaderAST.DataTypes.ident(proxy.name))

        ShaderAST.CallFunction(name, args, ShaderAST.unknownType)

      case Apply(Select(term, "apply"), xs) =>
        val body = walkTerm(term, envVarName)

        body.find {
          case ShaderAST.CallFunction(_, _, _) => true
          case ShaderAST.FunctionRef(_, _, _)  => true
          case _                               => false
        } match
          case Some(ShaderAST.CallFunction(id, Nil, rt)) =>
            ShaderAST.CallFunction(id, xs.map(tt => walkTerm(tt, envVarName)), rt)

          case Some(ShaderAST.CallFunction(id, args, rt)) =>
            ShaderAST.CallFunction(id, xs.map(tt => walkTerm(tt, envVarName)), rt)

          case Some(ShaderAST.FunctionRef(id, _, rt)) =>
            ShaderAST.CallFunction(id, xs.map(tt => walkTerm(tt, envVarName)), rt)

          case _ =>
            throw ShaderError.UnexpectedConstruction(
              "Tried to set up a call to a native function, but did not find a function reference or callback."
            )

      case Apply(Select(Ident(maybeEnv), funcName), args) if envVarName.isDefined && maybeEnv == envVarName.get =>
        ShaderAST.CallExternalFunction(funcName, args.map(tt => walkTerm(tt, envVarName)), ShaderAST.unknownType)

      //

      case Select(term, "unary_-") =>
        ShaderAST.Neg(walkTerm(term, envVarName))

      // Annotations

      case Apply(Select(New(tree), _), List()) =>
        walkTree(tree, envVarName)

      //

      // Infix operations

      case Apply(
            Select(New(TypeIdent(name)), "<init>"),
            args
          ) =>
        // 'New' is allowed for layout annotations specifically (as they have arguments) or
        // for any struct that has been previously registered.
        if name == "layout" || structRegister.contains(name) then
          ShaderAST.New(name, args.map(a => walkTerm(a, envVarName)))
        else
          throw ShaderError.UnexpectedConstruction(
            "You cannot use classes (structs) not previously declared within the Shader body. This is either an illegal forward reference or you declared the class outside the shader."
          )

      case Apply(
            Select(Ident(id), "update"),
            List(
              index,
              rhs
            )
          ) =>
        // Update mutable collections - array's and mat's in our case.
        val idx  = walkTerm(index, envVarName)
        val body = walkTerm(rhs, envVarName)
        ShaderAST.Infix(
          "=",
          ShaderAST.DataTypes.index(id, idx),
          body,
          findReturnType(body)
        )

      case Apply(Select(term, op), xs) =>
        op match
          case "<" | "<=" | ">" | ">=" | "==" | "!=" =>
            // Vector Relational Functions, according to the spec TL;DR: the left and right side types must be the same.
            // However, we don't have a nice way to do that check yet.
            val lhs = walkTerm(term, envVarName)
            val rhs = xs.headOption.map(tt => walkTerm(tt, envVarName)).getOrElse(ShaderAST.Empty())
            val rt  = findReturnType(lhs)
            ShaderAST.Infix(op, lhs, rhs, rt)

          case "+" | "-" | "*" | "/" =>
            // Math operators.
            val lhs = walkTerm(term, envVarName)
            val rhs = xs.headOption.map(tt => walkTerm(tt, envVarName)).getOrElse(ShaderAST.Empty())
            val rt  = findReturnType(lhs)
            ShaderAST.Infix(op, lhs, rhs, rt)

          case "&&" | "||" =>
            // Logical operators.
            val lhs = walkTerm(term, envVarName)
            val rhs = xs.headOption.map(tt => walkTerm(tt, envVarName)).getOrElse(ShaderAST.Empty())
            val rt  = findReturnType(lhs)
            ShaderAST.Infix(op, lhs, rhs, rt)

          case "%" =>
            val lhs = walkTerm(term, envVarName)
            val rhs = xs.headOption.map(tt => walkTerm(tt, envVarName)).getOrElse(ShaderAST.Empty())
            val rt  = findReturnType(lhs)

            val isInt = List(lhs.typeIdent, rhs.typeIdent, rt.typeIdent).map(_.id).contains("int")

            if isInt then ShaderAST.Infix("%", lhs, rhs, rt)
            else
              ShaderAST.CallFunction(
                "mod",
                List(lhs, rhs),
                rt
              )

          case "<<" | ">>" | "&" | "^" | "|" =>
            // Bitwise ops
            val lhs = walkTerm(term, envVarName)
            val rhs = xs.headOption.map(tt => walkTerm(tt, envVarName)).getOrElse(ShaderAST.Empty())
            val rt  = findReturnType(lhs)
            ShaderAST.Infix(op, lhs, rhs, rt)

          case _ =>
            throw ShaderError.Unsupported("Shaders do not support infix operator: " + op)

      case Apply(
            Apply(Ident(op), List(Apply(_, List(Typed(Repeated(List(Literal(StringConstant(value))), _), _))))),
            _
          ) =>
        op match
          case "hex" =>
            import ultraviolet.syntax.interpolators.hex.*
            value.toVec3.map(v => ShaderAST.DataTypes.vec3(v.x, v.y, v.z)).getOrElse {
              throw ShaderError.Unsupported(
                "Hex values must be 6 or 8 characters long using the `hex` or `hexa` interpolators, respectively (e.g. #FF00FF)."
              )
            }

          case "hexa" =>
            import ultraviolet.syntax.interpolators.hex.*
            value.toVec4.map(v => ShaderAST.DataTypes.vec4(v.x, v.y, v.z, v.a)).getOrElse {
              throw ShaderError.Unsupported(
                "Hex values must be 6 or 8 characters long using the `hex` or `hexa` interpolators, respectively (e.g. #FF00FF00)."
              )
            }

          case "rgb" =>
            import ultraviolet.syntax.interpolators.rgb.*
            value.toVec3
              .map(v => ShaderAST.DataTypes.vec3(v.x, v.y, v.z))
              .getOrElse {
                throw ShaderError.Unsupported(
                  "RGB values must be 3 or 4 integers long using the `rgb` or `rgba` interpolators, respectively (e.g. 255,0,255)."
                )
              }

          case "rgba" =>
            import ultraviolet.syntax.interpolators.rgb.*
            value.toVec4
              .map(v => ShaderAST.DataTypes.vec4(v.x, v.y, v.z, v.a))
              .getOrElse {
                throw ShaderError.Unsupported(
                  "RGB values must be 3 or 4 integers long using the `rgb` or `rgba` interpolators, respectively (e.g. 255,0,255,0)."
                )
              }

          case _ =>
            throw ShaderError.Unsupported("Shaders do not support interpolators of type: " + op)

      case Apply(
            Apply(Ident(op), List(Apply(_, List(Typed(Repeated(_, _), _))))),
            _
          ) =>
        throw ShaderError.Unsupported("Shader interpolated colours must be string literals, e.g. #ff0000")

      case Apply(Apply(Ident(op), List(l)), List(r)) =>
        op match
          case "<" | "<=" | ">" | ">=" | "==" | "!=" =>
            // Vector Relational Functions, according to the spec. TL;DR: the left and right side types must be the same.
            // However, we don't have a nice way to do that check yet.
            val lhs = walkTerm(l, envVarName)
            val rhs = walkTerm(r, envVarName)
            val rt  = findReturnType(lhs)
            ShaderAST.Infix(op, lhs, rhs, rt)

          case "+" | "-" | "*" | "/" =>
            // Math operators.
            val lhs = walkTerm(l, envVarName)
            val rhs = walkTerm(r, envVarName)
            val rt  = findReturnType(lhs)
            ShaderAST.Infix(op, lhs, rhs, rt)

          case "&&" | "||" =>
            // Logical operators.
            val lhs = walkTerm(l, envVarName)
            val rhs = walkTerm(r, envVarName)
            val rt  = findReturnType(lhs)
            ShaderAST.Infix(op, lhs, rhs, rt)

          case "%" =>
            val lhs = walkTerm(l, envVarName)
            val rhs = walkTerm(r, envVarName)
            val rt  = findReturnType(lhs)

            val isInt = List(lhs.typeIdent, rhs.typeIdent, rt.typeIdent).map(_.id).contains("int")

            if isInt then ShaderAST.Infix("%", lhs, rhs, rt)
            else
              ShaderAST.CallFunction(
                "mod",
                List(lhs, rhs),
                rt
              )

          case "<<" | ">>" | "&" | "^" | "|" =>
            // Bitwise ops
            val lhs = walkTerm(l, envVarName)
            val rhs = walkTerm(r, envVarName)
            val rt  = findReturnType(lhs)
            ShaderAST.Infix(op, lhs, rhs, rt)

          case _ =>
            throw ShaderError.Unsupported("Shaders do not support infix operator: " + op)

      // Arrays

      // array constuctor
      case Apply(
            Apply(
              foo @ TypeApply(
                Select(Ident("array"), "apply"),
                List(Singleton(Literal(IntConstant(size))), typ)
              ),
              List(Typed(Repeated(args, _), _))
            ),
            _
          ) =>
        val typeOf = ShaderAST.DataTypes.ident(extractInferredType(typ) + s"[${size.toString()}]")
        ShaderAST.DataTypes.array(size, args.map(a => walkTerm(a, envVarName)), typeOf)

      // array component access from a env var
      case Apply(
            Apply(
              TypeApply(Select(Ident("array"), "apply"), _),
              List(Select(Ident(namespace), name))
            ),
            List(index)
          ) =>
        val idx = walkTerm(index, envVarName)
        envVarName match
          case Some(value) if value == namespace =>
            ShaderAST.DataTypes.externalIndex(name, idx)

          case _ =>
            ShaderAST.Field(
              ShaderAST.DataTypes.ident(namespace),
              ShaderAST.DataTypes.index(name, idx)
            )

      // array component access
      case Apply(
            Apply(TypeApply(Select(Ident("array"), "apply"), _), List(Ident(name))),
            List(index)
          ) =>
        ShaderAST.DataTypes.index(name, walkTerm(index, envVarName))

      // array - unexpected build
      case x @ Apply(
            Apply(
              TypeApply(
                Select(Ident("array"), "apply"),
                _
              ),
              _
            ),
            _
          ) =>
        throw ShaderError.UnexpectedConstruction(
          "Shader arrays must be constructed with full type information, e.g.: array[3, Float] (where 3 is the size of the array)"
        )

      //

      case Apply(Ident(name), terms) =>
        ShaderAST.CallFunction(name, terms.map(tt => walkTerm(tt, envVarName)), ShaderAST.unknownType)

      case Inlined(None, _, term) =>
        walkTerm(term, envVarName)

      case Inlined(Some(Ident(_)), _, term) =>
        walkTerm(term, envVarName)

      // Raw
      case Inlined(
            Some(Select(Ident(_), _)),
            Nil,
            Typed(Apply(Select(_, _), List(Literal(StringConstant(raw)))), TypeIdent("RawGLSL"))
          ) =>
        ShaderAST.RawLiteral(raw)

      // GLSLHeader
      case Inlined(
            Some(Select(Ident(_), _)),
            Nil,
            Typed(Apply(Select(_, _), List(Literal(StringConstant(raw)))), TypeIdent("GLSLHeader"))
          ) =>
        ShaderAST.RawLiteral(raw)

      // raw
      case Inlined(Some(Apply(Ident("raw"), List(term))), _, _) =>
        walkTerm(term, envVarName)

      // Swizzle
      case Inlined(Some(Apply(Ident(swzl), List(id @ Select(Ident(env), varName)))), _, rt)
          if isSwizzle.matches(swzl) && envVarName.contains(env) =>
        ShaderAST.DataTypes.swizzle(
          walkTerm(id, envVarName),
          swzl,
          Option(walkTree(rt, envVarName)).getOrElse(inferSwizzleType(swzl))
        )

      case Inlined(
            Some(Apply(Ident(swzl), List(term))),
            _,
            Typed(
              Apply(
                Select(Select(Inlined(None, Nil, Ident("ShaderDSLTypeExtensions_this")), _), _),
                _
              ),
              _
            )
          ) if isSwizzle.matches(swzl) =>
        val body = walkTerm(term, envVarName)
        ShaderAST.DataTypes.swizzle(
          body,
          swzl,
          inferSwizzleType(swzl)
        )

      // Swizzle a function call
      case Select(term @ Apply(Ident(_), _), swzl) if isSwizzle.matches(swzl) =>
        ShaderAST.DataTypes.swizzle(
          walkTerm(term, envVarName),
          swzl,
          inferSwizzleType(swzl)
        )

      // Inlined external def

      case Inlined(Some(Apply(Ident(name), args)), ds, x @ Typed(term, typeTree)) =>
        ds.map(s => walkStatement(s, envVarName))
          .flatMap {
            case v @ ShaderAST.Val(proxy, value, _) =>
              List(v)
            case _ =>
              Nil
          }
          .foreach { case ShaderAST.Val(proxy, value, _) =>
            proxies.addInlineReplace(proxy, value)
          }

        walkTerm(x, envVarName)

      case Inlined(Some(Select(This(_), _)), _, term) =>
        walkTerm(term, envVarName)

      case tt @ Inlined(
            Some(
              Apply(
                TypeApply(Ident("ubo"), List(TypeIdent(_))),
                _
              )
            ),
            _,
            _
          ) =>
        uboRegister += ShaderAST.UBO(uboUtils.extractUBO(tt))
        ShaderAST.Empty()

      // Inlined call to a method on a class

      case Inlined(
            Some(
              Select(
                Inlined(_, _, Apply(Select(New(TypeIdent(_ /*class name*/ )), "<init>"), Nil)),
                _ // method name
              )
            ),
            _,
            term
          ) =>
        walkTerm(term, envVarName)

      //

      case Inlined(Some(_: Tree), _, Typed(TypeApply(term, _), _)) =>
        walkTree(term, envVarName)

      case Inlined(Some(tree: Tree), _, _) =>
        walkTree(tree, envVarName)

      case TypeApply(term, _) =>
        walkTerm(term, envVarName)

      // Anonymous function?
      case Typed(
            Block(
              List(
                fn @ DefDef(_, args, rt, Some(term))
              ),
              Closure(Ident("$anonfun"), None)
            ),
            _
          ) =>
        walkTree(fn, envVarName)

      case Typed(term, _) =>
        walkTerm(term, envVarName)

      case Block(args, fnBody) if isOneLineLambda(args) =>
        val fnName = proxies.makeDefName

        val body = walkTerm(fnBody, envVarName)

        val arguments =
          args.collect {
            case ValDef(name, Inferred(), Some(value)) if name.contains("$proxy") =>
              walkTerm(value, envVarName) -> name.substring(0, name.indexOf("$"))
          }

        val returnType =
          findReturnType(body)

        shaderDefs += FunctionLookup(
          ShaderAST.Function(
            id = fnName,
            args = arguments.map((typ, n) => findReturnType(typ) -> n),
            body = body,
            returnType = returnType
          ),
          false
        )

        ShaderAST.CallFunction(
          id = fnName,
          args = arguments.map(_._1),
          returnType = returnType
        )

      case Block(statements, Closure(Ident("$anonfun"), None)) =>
        val ss = statements
          .map(s => walkStatement(s, envVarName))

        ShaderAST.Block(ss)

      case Block(Nil, term) =>
        walkTerm(term, envVarName)

      case Block(statements, term) =>
        val ss =
          statements.map(s => walkStatement(s, envVarName)) :+ walkTerm(term, envVarName)

        ShaderAST.Block(ss)

      // Literals

      case Literal(FloatConstant(f)) =>
        ShaderAST.DataTypes.float(f)

      case Literal(IntConstant(i)) =>
        ShaderAST.DataTypes.int(i)

      case Literal(BooleanConstant(b)) =>
        ShaderAST.DataTypes.bool(b)

      case Literal(UnitConstant()) =>
        ShaderAST.Empty()

      case Literal(NullConstant()) =>
        ShaderAST.Empty()

      case Literal(StringConstant(raw)) =>
        ShaderAST.RawLiteral(raw)

      case Literal(constant) =>
        throw ShaderError.Unsupported("Shaders do not support constant type: " + constant.show)

      // Refs

      case Ident(name) =>
        val resolvedName = proxies.lookUp(name).name

        shaderDefs.toList.find(_.fn.id == resolvedName).map(_.fn) match
          case None =>
            ShaderAST.DataTypes.ident(resolvedName)

          case Some(ShaderAST.Function(_, _, _, rt)) =>
            ShaderAST.CallFunction(resolvedName, Nil, rt)

      case Closure(_, _) =>
        ShaderAST.Empty()

      case Wildcard() =>
        throw ShaderError.Unsupported("Shaders do not support wildcards.")

      case Select(term, _) => // term, name
        walkTerm(term, envVarName)

      // Unsupported (yet?)

      case This(_) =>
        throw new ShaderError.Unsupported("Shaders do not support references to 'this'.")

      case New(_) =>
        throw new ShaderError.Unsupported("Shaders do not support 'new' instances.")

      case NamedArg(_, _) =>
        throw new ShaderError.Unsupported("Shaders do not support named args.")

      case Super(_, _) =>
        throw new ShaderError.Unsupported("Shaders do not support calls to super.")

      case Assign(lhs, rhs) =>
        val l = walkTerm(lhs, envVarName)
        val r = walkTerm(rhs, envVarName)

        (l, r) match
          case (i @ ShaderAST.DataTypes.ident(_), f @ ShaderAST.If(_, _, Some(_))) =>
            recursivelyAssignIf(i, f)

          case (_, _) =>
            ShaderAST.Assign(l, r)

      case If(condTerm, thenTerm, elseTerm) =>
        walkTerm(elseTerm, envVarName) match
          case ShaderAST.Empty() =>
            ShaderAST.If(
              walkTerm(condTerm, envVarName),
              walkTerm(thenTerm, envVarName),
              None
            )

          case e =>
            ShaderAST.If(
              walkTerm(condTerm, envVarName),
              walkTerm(thenTerm, envVarName),
              Option(e)
            )

      case Match(term, cases) =>
        val cs =
          cases.map {
            case CaseDef(Literal(IntConstant(i)), None, caseTerm) =>
              (Option(i), walkTerm(caseTerm, envVarName))

            case CaseDef(Wildcard(), None, caseTerm) =>
              (None, walkTerm(caseTerm, envVarName))

            case _ =>
              throw ShaderError.Unsupported("Shaders only support pattern matching on `Int` values or `_` wildcards.")
          }

        ShaderAST.Switch(walkTerm(term, envVarName), cs)

      case SummonFrom(_) =>
        throw ShaderError.Unsupported("Shaders do not support summoning.")

      case Try(_, _, _) =>
        throw ShaderError.Unsupported("Shaders do not support try blocks.")

      case Return(_, _) =>
        throw ShaderError.Unsupported("Shaders do not support return statements.")

      case Repeated(args, _) =>
        ShaderAST.Block(args.map(p => walkTerm(p, envVarName)))

      case SelectOuter(_, _, _) =>
        throw ShaderError.Unsupported("Shaders do not support outer selectors.")

      case While(cond, body) =>
        ShaderAST.While(walkTerm(cond, envVarName), walkTerm(body, envVarName))

      case x =>
        val sample = Printer.TreeStructure.show(x).take(100)
        throw ShaderError.UnexpectedConstruction("Unexpected Term: " + sample + "(..)")
