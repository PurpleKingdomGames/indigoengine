package ultraviolet.macros

import ultraviolet.datatypes.ShaderAST
import ultraviolet.datatypes.ShaderError
import ultraviolet.datatypes.UBODef
import ultraviolet.datatypes.UBOField

import scala.collection.mutable.ListBuffer
import scala.quoted.Quotes

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class CreateShaderAST[Q <: Quotes](using val qq: Q) extends ShaderMacroUtils:
  import qq.reflect.*

  val uboUtils                               = new ExtractUBOUtils[qq.type](using qq)
  val proxies                                = new ProxyManager
  val shaderDefs: ListBuffer[FunctionLookup] = new ListBuffer()

  def assignToLast(lhs: ShaderAST): ShaderAST => ShaderAST = {
    case ShaderAST.Block(statements :+ last) =>
      ShaderAST.Block(statements :+ ShaderAST.Assign(lhs, last))

    case last =>
      ShaderAST.Assign(lhs, last)
  }

  def extractInferredType(typ: TypeTree): Option[String] =
    def mapName(name: Option[String]): Option[String] =
      name
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
          case "Unit" | "array" =>
            true
          case n if n.startsWith("Function") =>
            true
          case _ =>
            false
        }

    typ match
      case Applied(TypeIdent("array"), List(Singleton(Literal(IntConstant(size))), TypeIdent(typeName))) =>
        mapName(Option(typeName)).map(_ + s"[${size.toString()}]")

      case Applied(TypeIdent("array"), List(Singleton(Ident(varName)), TypeIdent(typeName))) =>
        mapName(Option(typeName)).map(_ + s"[$varName]")

      case _ =>
        mapName(typ.tpe.classSymbol.map(_.name))

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
        ShaderAST.Struct(name, params.map(p => walkTree(p, envVarName)))

      case ClassDef(_, _, _, _, _) =>
        throw ShaderError.Unsupported("Shaders only support simple, flat classes.")

      case TypeDef(_, _) =>
        throw ShaderError.Unsupported("Shaders do not support fancy types. :-)")

      // Compose
      case ValDef(
            name,
            Applied(_, List(argType, returnType)),
            Some(
              Apply(TypeApply(Select(Ident(g), op), List(Inferred())), List(Ident(f)))
            )
          ) if op == "compose" || op == "andThen" =>
        val fnInType  = walkTree(argType, envVarName)
        val fnOutType = Option(walkTree(returnType, envVarName))
        val fnName    = proxies.makeDefName
        val vName     = proxies.makeVarName

        val ff = if op == "compose" then f else g
        val gg = if op == "compose" then g else f

        val fProxy = proxies.lookUp(ff)
        val gProxy = proxies.lookUp(gg)

        val body =
          ShaderAST.CallFunction(
            gProxy._1,
            List(
              ShaderAST.CallFunction(
                fProxy._1,
                List(ShaderAST.DataTypes.ident(vName)),
                Nil,
                fProxy._2
              )
            ),
            List(ShaderAST.DataTypes.ident(fProxy._1)),
            gProxy._2
          )

        shaderDefs += FunctionLookup(
          ShaderAST.Function(fnName, List(fnInType -> vName), body, fnOutType),
          false
        )
        proxies.add(name, fnName, fnOutType)
        ShaderAST.Empty()

      case v @ ValDef(name, typ, Some(term)) =>
        val typeOf = extractInferredType(typ)
        val body   = walkTerm(term, envVarName)

        val maybeAnnotation: Option[ShaderAST.DataTypes.ident] =
          v.symbol.annotations.headOption.map(p => walkTerm(p, envVarName)).flatMap {
            case a: ShaderAST.DataTypes.ident => Option(a)
            case _                            => None
          }

        body match
          case ShaderAST.Block(List(ShaderAST.FunctionRef(id, rt))) =>
            proxies.add(name, id, rt)
            ShaderAST.Empty()

          case ShaderAST.Block(statements :+ ShaderAST.If(cond, thn, els)) =>
            val resVal = ShaderAST.DataTypes.ident(name)
            ShaderAST.Block(
              statements ++ List(
                ShaderAST.Val(name, ShaderAST.Empty(), typeOf),
                ShaderAST.If(
                  cond,
                  assignToLast(resVal)(thn),
                  els.map(e => assignToLast(resVal)(e))
                )
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

          case ShaderAST.If(cond, thn, els) =>
            val resVal = ShaderAST.DataTypes.ident(name)
            ShaderAST.Block(
              List(
                ShaderAST.Val(name, ShaderAST.Empty(), typeOf),
                ShaderAST.If(
                  cond,
                  assignToLast(resVal)(thn),
                  els.map(e => assignToLast(resVal)(e))
                )
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
            val v =
              body match
                case arr @ ShaderAST.DataTypes.array(size, args, typeOfArray) =>
                  typeOfArray match
                    case None =>
                      throw ShaderError.Unsupported("Shader arrays must be fully typed")

                    case Some(tOf) =>
                      val (tName, tSize) = tOf.splitAt(tOf.indexOf("["))

                      ShaderAST.Val(
                        name + tSize,
                        body,
                        Some(tName)
                      )

                case _ =>
                  ShaderAST.Val(name, body, typeOf)

            maybeAnnotation match
              case None =>
                v

              case Some(label) =>
                ShaderAST.Annotated(label, ShaderAST.Empty(), v)

      case v @ ValDef(name, typ, None) =>
        val typeOf = extractInferredType(typ)

        val maybeAnnotation: Option[ShaderAST.DataTypes.ident] =
          v.symbol.annotations.headOption.map(p => walkTerm(p, envVarName)).flatMap {
            case a: ShaderAST.DataTypes.ident => Option(a)
            case _                            => None
          }

        val vv = ShaderAST.Val(name, ShaderAST.Empty(), typeOf)

        maybeAnnotation match
          case None =>
            vv

          case Some(label) =>
            ShaderAST.Annotated(label, ShaderAST.Empty(), vv)

      case d @ DefDef(fnName, args, rt, Some(term)) =>
        val maybeAnnotation: Option[ShaderAST.Annotated] =
          d.symbol.annotations.headOption.flatMap {
            case Apply(Select(New(TypeIdent(id @ "ShaderDef")), _), List(namespace)) =>
              Option(
                ShaderAST.Annotated(
                  ShaderAST.DataTypes.ident(id),
                  walkTerm(namespace, envVarName),
                  ShaderAST.Empty()
                )
              )

            case x =>
              None
          }

        val argNamesTypes =
          args
            .collect { case TermParamClause(ps) => ps }
            .flatten
            .collect { case ValDef(name, typ, _) =>
              val typeOf = extractInferredType(typ)
              (typeOf.getOrElse("void"), name)
            }

        val isAnon = fnName == "$anonfun"
        val fn     = if isAnon then proxies.makeDefName else fnName
        val body   = walkTerm(term, envVarName)

        val returnType =
          extractInferredType(rt)
            .map(s => ShaderAST.DataTypes.ident(s))
            .orElse {
              rt match
                case rtt @ TypeIdent(_) =>
                  Option(walkTree(rtt, envVarName))

                case _ =>
                  findReturnType(body)
            }

        body match
          case ShaderAST.Block(List(ShaderAST.FunctionRef(id, rt))) =>
            proxies.add(fn, id, rt)
            ShaderAST.Empty()

          case ShaderAST.Block(statements :+ ShaderAST.If(cond, thn, els)) =>
            val name   = proxies.makeVarName
            val resVal = ShaderAST.DataTypes.ident(name)
            val typeOf = extractInferredType(rt)
            val fnBody =
              ShaderAST.Block(
                List(
                  ShaderAST.Val(name, ShaderAST.Empty(), typeOf),
                  ShaderAST.If(
                    cond,
                    assignToLast(resVal)(thn),
                    els.map(e => assignToLast(resVal)(e))
                  ),
                  resVal
                )
              )

            ShaderAST.Function(
              fn,
              argNamesTypes.map(p => ShaderAST.DataTypes.ident(p._1) -> p._2),
              ShaderAST.Block(statements ++ fnBody.statements),
              returnType
            )

          case ShaderAST.Block(statements :+ ShaderAST.Switch(on, cases)) =>
            val name   = proxies.makeVarName
            val resVal = ShaderAST.DataTypes.ident(name)
            val typeOf = extractInferredType(rt)
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

            ShaderAST.Function(
              fn,
              argNamesTypes.map(p => ShaderAST.DataTypes.ident(p._1) -> p._2),
              ShaderAST.Block(statements ++ fnBody.statements),
              returnType
            )

          case ShaderAST.If(cond, thn, els) =>
            val name   = proxies.makeVarName
            val resVal = ShaderAST.DataTypes.ident(name)
            val typeOf = extractInferredType(rt)
            val fnBody =
              ShaderAST.Block(
                List(
                  ShaderAST.Val(name, ShaderAST.Empty(), typeOf),
                  ShaderAST.If(
                    cond,
                    assignToLast(resVal)(thn),
                    els.map(e => assignToLast(resVal)(e))
                  ),
                  resVal
                )
              )

            ShaderAST.Function(
              fn,
              argNamesTypes.map(p => ShaderAST.DataTypes.ident(p._1) -> p._2),
              fnBody,
              returnType
            )

          case ShaderAST.Switch(on, cases) =>
            val name   = proxies.makeVarName
            val resVal = ShaderAST.DataTypes.ident(name)
            val typeOf = extractInferredType(rt)
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

            ShaderAST.Function(
              fn,
              argNamesTypes.map(p => ShaderAST.DataTypes.ident(p._1) -> p._2),
              fnBody,
              returnType
            )

          case _ =>
            shaderDefs += FunctionLookup(
              ShaderAST.Function(
                fn,
                argNamesTypes.map(p => ShaderAST.DataTypes.ident(p._1) -> p._2),
                body,
                returnType
              ),
              !isAnon
            )

            if isAnon then ShaderAST.FunctionRef(fn, returnType)
            else
              ShaderAST.Function(
                fn,
                argNamesTypes.map(p => ShaderAST.DataTypes.ident(p._1) -> p._2),
                body,
                returnType
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

      // Entry point (with type params, no headers)
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
            ShaderAST.ShaderBlock(in, out, e, Nil, statements)

          case List(in) =>
            ShaderAST.ShaderBlock(in, None, e, Nil, statements)

          case _ =>
            ShaderAST.ShaderBlock(None, None, e, Nil, statements)

      // Entry point (with type params, with headers)
      case Apply(
            Apply(
              TypeApply(Select(Ident("Shader"), "apply"), types),
              headers
            ),
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
        val e                = Option(env)
        val headerStatements = headers.map(p => walkTerm(p, e))
        val statements       = List(walkTerm(term, e))

        types.map(extractInferredTypeParam) match
          case List(in, out) =>
            ShaderAST.ShaderBlock(in, out, e, headerStatements, statements)

          case List(in) =>
            ShaderAST.ShaderBlock(in, None, e, headerStatements, statements)

          case _ =>
            ShaderAST.ShaderBlock(None, None, e, headerStatements, statements)

      // Entry point (no type params, no headers)
      case Apply(Select(Ident("Shader"), "apply"), args) =>
        ShaderAST.ShaderBlock(None, None, None, Nil, args.map(p => walkTerm(p, envVarName)))

      // Entry point (no type params, with headers)
      case Apply(Apply(Select(Ident("Shader"), "apply"), headers), args) =>
        ShaderAST.ShaderBlock(
          None,
          None,
          None,
          headers.map(p => walkTerm(p, envVarName)),
          args.map(p => walkTerm(p, envVarName))
        )

      case Apply(Select(Ident("RawGLSL"), "apply"), List(term)) =>
        walkTerm(term, envVarName)

      // For loops

      case Apply(
            Apply(
              TypeApply(Ident("cfor"), _),
              List(
                Literal(IntConstant(initial)),
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
                  List(DefDef("$anonfun", _, _, Some(body))),
                  _
                )
              )
            )
          ) =>
        val varName = proxies.makeVarName
        val i       = ShaderAST.Val(varName, ShaderAST.DataTypes.int(initial), Option("int"))

        val c = walkTerm(condition, envVarName).traverse {
          case ShaderAST.DataTypes.ident(id) if id.startsWith("_") =>
            ShaderAST.DataTypes.ident(varName)

          case ShaderAST.Infix(op, left, right, rt) =>
            ShaderAST.Infix(op, left, right, Option(ShaderAST.DataTypes.ident("int")))

          case x =>
            x
        }

        def replaceName: ShaderAST => ShaderAST = {
          case ShaderAST.DataTypes.ident(id) if id.startsWith("_") =>
            ShaderAST.DataTypes.ident(varName)

          case x =>
            x
        }

        val n = ShaderAST.Assign(
          ShaderAST.DataTypes.ident(varName),
          walkTerm(next, envVarName).traverse(replaceName)
        )

        val b = walkTerm(body, envVarName).traverse(replaceName)

        ShaderAST.For(i, c, n, b)

      case Apply(
            Apply(
              TypeApply(Ident("cfor"), _),
              List(
                _,
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
                  List(DefDef("$anonfun", _, _, Some(body))),
                  _
                )
              )
            )
          ) =>
        throw ShaderError.Unsupported(
          "Shaders do not support for-loops (cfor) constructed using types other than `Int`."
        )

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
        val (fnName, rt) = proxies.lookUp(id, id -> Option(ShaderAST.DataTypes.ident("void")))
        ShaderAST.CallFunction(fnName, args.map(x => walkTerm(x, envVarName)), Nil, rt)

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

      case Select(Inlined(None, Nil, Ident(obj)), fieldName) =>
        ShaderAST.DataTypes.ident(s"$obj.$fieldName")

      case Select(Ident(name), "unary_-") =>
        ShaderAST.DataTypes.ident(s"-$name")

      case Select(Ident(namespace), name) =>
        envVarName match
          case Some(value) if value == namespace =>
            ShaderAST.DataTypes.ident(s"$name")

          case _ =>
            ShaderAST.DataTypes.ident(s"$namespace.$name")

      // Read a field - but of something namespaced, negated, e.g. -position.x
      case Select(Select(Ident(namespace), name), "unary_-") =>
        envVarName match
          case Some(value) if value == namespace =>
            ShaderAST.DataTypes.ident(s"-$name")

          case _ =>
            ShaderAST.DataTypes.ident(s"-$namespace.$name")

      // Read a field - but of something namespaced, e.g. env.Position.x
      case Select(Select(Ident(namespace), name), field) =>
        envVarName match
          case Some(value) if value == namespace =>
            ShaderAST.DataTypes.ident(s"$name.$field")

          case _ =>
            ShaderAST.DataTypes.ident(s"$namespace.$name.$field")

      // Read a field - but of something namespaced, negated, e.g. -env.Position.x
      case Select(Select(Select(Ident(namespace), name), field), "unary_-") =>
        envVarName match
          case Some(value) if value == namespace =>
            ShaderAST.DataTypes.ident(s"-$name.$field")

          case _ =>
            ShaderAST.DataTypes.ident(s"-$namespace.$name.$field")

      // Native method call.
      case Apply(Ident(name), List(Inlined(None, Nil, Ident(defRef)))) =>
        val (fnName, _)           = proxies.lookUp(defRef)
        val args: List[ShaderAST] = List(ShaderAST.DataTypes.ident(fnName))
        ShaderAST.CallFunction(name, args, args, None)

      case Apply(Select(term, "apply"), xs) =>
        walkTerm(term, envVarName).find {
          case ShaderAST.CallFunction(_, _, _, _) => true
          case _                                  => false
        } match
          case Some(ShaderAST.CallFunction(id, Nil, Nil, rt)) =>
            ShaderAST.CallFunction(id, xs.map(tt => walkTerm(tt, envVarName)), Nil, rt)

          case Some(ShaderAST.CallFunction(id, args, argNames, rt)) =>
            ShaderAST.CallFunction(id, xs.map(tt => walkTerm(tt, envVarName)), argNames, rt)

          case _ =>
            ShaderAST.Block(xs.map(tt => walkTerm(tt, envVarName)))

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
        ShaderAST.New(name, args.map(a => walkTerm(a, envVarName)))

      case Apply(
            Select(Ident(id), "update"),
            List(
              index,
              rhs
            )
          ) =>
        // Update mutable collections - array's and mat's in our case.
        val idx = walkTerm(index, envVarName)
        ShaderAST.Infix(
          "=",
          ShaderAST.DataTypes.index(id, idx),
          walkTerm(rhs, envVarName),
          None
        )

      case Apply(Select(term, op), xs) =>
        op match
          case "+" | "-" | "*" | "/" | "<" | ">" | "==" | "<=" | ">=" | "&&" | "||" =>
            val lhs = walkTerm(term, envVarName)
            val rhs = xs.headOption.map(tt => walkTerm(tt, envVarName)).getOrElse(ShaderAST.Empty())
            val rt  = findReturnType(lhs)
            ShaderAST.Infix(op, lhs, rhs, rt)

          case "%" =>
            val lhs = walkTerm(term, envVarName)
            val rhs = xs.headOption.map(tt => walkTerm(tt, envVarName)).getOrElse(ShaderAST.Empty())
            val rt  = findReturnType(lhs)
            ShaderAST.CallFunction(
              "mod",
              List(lhs, rhs),
              List(
                ShaderAST.DataTypes.ident("x"),
                ShaderAST.DataTypes.ident("y")
              ),
              rt
            )

          case _ =>
            throw ShaderError.Unsupported("Shaders do not support infix operator: " + op)

      case Apply(Apply(Ident(op), List(l)), List(r)) =>
        op match
          case "+" | "-" | "*" | "/" | "<" | ">" | "==" | "<=" | ">=" | "&&" | "||" =>
            val lhs = walkTerm(l, envVarName)
            val rhs = walkTerm(r, envVarName)
            val rt  = findReturnType(lhs)
            ShaderAST.Infix(op, lhs, rhs, rt)

          case "%" =>
            val lhs = walkTerm(l, envVarName)
            val rhs = walkTerm(r, envVarName)
            val rt  = findReturnType(lhs)
            ShaderAST.CallFunction(
              "mod",
              List(lhs, rhs),
              List(
                ShaderAST.DataTypes.ident("x"),
                ShaderAST.DataTypes.ident("y")
              ),
              rt
            )

          case _ =>
            throw ShaderError.Unsupported("Shaders do not support infix operator: " + op)

      // Arrays

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
        val typeOf = extractInferredType(typ).map(_ + s"[${size.toString()}]")
        ShaderAST.DataTypes.array(size, args.map(a => walkTerm(a, envVarName)), typeOf)

      case Apply(
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
        ShaderAST.CallFunction(name, terms.map(tt => walkTerm(tt, envVarName)), Nil, None)

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
      case Inlined(Some(Apply(Ident(name), List(id @ Select(Ident(env), varName)))), _, rt)
          if isSwizzle.matches(name) && envVarName.contains(env) =>
        ShaderAST.DataTypes.swizzle(
          walkTerm(id, envVarName),
          name,
          Option(walkTree(rt, envVarName))
        )

      case Inlined(Some(Apply(Ident(name), List(gt @ Apply(Select(Ident(genType), "apply"), args)))), _, _)
          if isSwizzle.matches(name) && isSwizzleable.matches(genType) =>
        ShaderAST.DataTypes.swizzle(
          walkTerm(gt, envVarName),
          name,
          Option(ShaderAST.DataTypes.ident(genType))
        )

      case Inlined(Some(Apply(Ident(name), List(Ident(id)))), _, _) if isSwizzle.matches(name) =>
        ShaderAST.DataTypes.swizzle(
          ShaderAST.DataTypes.ident(id),
          name,
          None
        )
      //

      case Inlined(Some(Apply(Ident(name), args)), ds, Typed(term, typeTree)) =>
        val argNames   = args.map(_ => proxies.makeVarName)
        val callArgs   = args.map(tt => walkTerm(tt, envVarName))
        val pairedArgs = callArgs.zip(argNames)
        val fnArgs: List[(ShaderAST, String)] =
          pairedArgs.map { p =>
            val typ = p._1.typeIdent.getOrElse(ShaderAST.DataTypes.ident("void"))
            typ -> p._2
          }

        ds.map(s => walkStatement(s, envVarName))
          .flatMap {
            case ShaderAST.Val(proxy, value, _) =>
              pairedArgs.find(p => p._1 == value) match
                case None    => Nil
                case Some(v) => List(proxy -> v._2)

            case _ =>
              Nil
          }
          .foreach { case (originalName, refName) =>
            proxies.add(originalName, refName)
          }

        val body       = walkTerm(term, envVarName)
        val returnType = findReturnType(walkTree(typeTree, envVarName))

        shaderDefs += FunctionLookup(
          ShaderAST.Function(name, fnArgs, body, returnType),
          false // Should be true, refactor when I revisit inline defs...
        )
        val nmes = argNames.map(ShaderAST.DataTypes.ident.apply)
        ShaderAST.CallFunction(name, callArgs, nmes, returnType)

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
        ShaderAST.UBO(uboUtils.extractUBO(tt))

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

      case Inlined(Some(tree: Tree), _, _) =>
        walkTree(tree, envVarName)

      case TypeApply(term, _) =>
        walkTerm(term, envVarName)

      // Anonymous function?
      case Typed(
            Block(List(DefDef(_, args, _, Some(term))), Closure(Ident("$anonfun"), None)),
            Applied(_, types)
          ) =>
        val typesRendered: List[ShaderAST] = types.map(p => walkTree(p, envVarName))

        val returnType: Option[ShaderAST] =
          typesRendered.reverse.headOption

        val argNames =
          args
            .collect { case TermParamClause(ps) => ps }
            .flatten
            .collect { case ValDef(name, _, _) => name }

        val arguments = typesRendered
          .dropRight(1)
          .zip(argNames)
          .map { case (typ, nme) => typ -> nme }

        val fn = proxies.makeDefName
        shaderDefs += FunctionLookup(
          ShaderAST.Function(fn, arguments, walkTerm(term, envVarName), returnType),
          false
        )
        val nmes = argNames.map(ShaderAST.DataTypes.ident.apply)
        ShaderAST.CallFunction(fn, Nil, nmes, returnType)

      case Typed(term, _) =>
        walkTerm(term, envVarName)

      case Block(statements, Closure(Ident("$anonfun"), None)) =>
        val ss = statements
          .map(s => walkStatement(s, envVarName))

        ShaderAST.Block(ss)

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
        val resolvedName = proxies.lookUp(name)._1

        shaderDefs.toList.find(_.fn.id == resolvedName).map(_.fn) match
          case None =>
            ShaderAST.DataTypes.ident(resolvedName)

          case Some(ShaderAST.Function(_, _, _, rt)) =>
            ShaderAST.CallFunction(resolvedName, Nil, Nil, rt)

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
        ShaderAST.Assign(
          walkTerm(lhs, envVarName),
          walkTerm(rhs, envVarName)
        )

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
