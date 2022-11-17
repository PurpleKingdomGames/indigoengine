package ultraviolet.macros

import ultraviolet.datatypes.ShaderAST
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

  def extractInferredType(typ: TypeTree): Option[String] =
    typ.tpe.classSymbol
      .map(_.name)
      .map {
        case "Float" => "float"
        case "Int"   => "int"
        case n       => n
      }
      .filter {
        case "float" | "int" | "vec2" | "vec3" | "vec4" => true
        case _                                          => false
      }

  def walkStatement(s: Statement): ShaderAST =
    s match
      case Import(_, _) =>
        ShaderAST.Empty()

      case Export(_, _) =>
        throw new Exception("Shaders do not support exports.")

      case ClassDef(_, _, _, _, _) =>
        throw new Exception("Shaders do not support classes.")

      case TypeDef(_, _) =>
        throw new Exception("Shaders do not support fancy types.")

      // Compose
      case ValDef(
            name,
            Applied(_, List(argType, returnType)),
            Some(
              Apply(TypeApply(Select(Ident(g), op), List(Inferred())), List(Ident(f)))
            )
          ) if op == "compose" || op == "andThen" =>
        val fnInType  = walkTree(argType)
        val fnOutType = Option(walkTree(returnType))
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
        val body   = walkTerm(term)

        val maybeAnnotation: Option[ShaderAST.DataTypes.ident] =
          v.symbol.annotations.headOption.map(walkTerm).flatMap {
            case a: ShaderAST.DataTypes.ident => Option(a)
            case _                            => None
          }

        body match
          case ShaderAST.Block(List(ShaderAST.FunctionRef(id, rt))) =>
            proxies.add(name, id, rt)
            ShaderAST.Empty()

          case _ =>
            maybeAnnotation match
              case None =>
                ShaderAST.Val(name, body, typeOf)
              case Some(label) =>
                ShaderAST.Annotated(label, ShaderAST.Val(name, body, typeOf))

      case ValDef(name, _, None) =>
        throw new Exception("Shaders do not support val's with no values.")

      case DefDef(fnName, args, rt, Some(term)) =>
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
        val body   = walkTerm(term)

        val returnType =
          rt match
            case rtt @ TypeIdent(_) =>
              Option(walkTree(rtt))

            case _ =>
              findReturnType(body)

        body match
          case ShaderAST.Block(List(ShaderAST.FunctionRef(id, rt))) =>
            proxies.add(fn, id, rt)
            ShaderAST.Empty()

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
        throw new Exception("Unexpected def construction")

      case t: Term =>
        walkTerm(t)

  def walkTree(t: Tree): ShaderAST =
    t match
      case TypeIdent("Unit") =>
        ShaderAST.DataTypes.ident("void")

      case TypeIdent("Float") =>
        ShaderAST.DataTypes.ident("float")

      case TypeIdent("Int") =>
        ShaderAST.DataTypes.ident("int")

      case TypeIdent(name) =>
        ShaderAST.DataTypes.ident(name)

      case PackageClause(_, _) =>
        throw new Exception("Shaders do not support packages.")

      case s: Statement =>
        walkStatement(s)

  def walkTerm(t: Term): ShaderAST =
    t match

      // Specific hooks we care about

      // Entry point (with type params, no headers)
      case Apply(
            TypeApply(Select(Ident("Shader"), "apply"), _),
            List(
              Block(
                Nil,
                Block(
                  List(
                    DefDef(
                      "$anonfun",
                      List(TermParamClause(List(ValDef(envVarName, Inferred(), None)))),
                      Inferred(),
                      Some(term)
                    )
                  ),
                  Closure(Ident("$anonfun"), None)
                )
              )
            )
          ) =>
        ShaderAST.ShaderBlock(Option(envVarName), Nil, List(walkTerm(term)))

      // Entry point (with type params, with headers)
      case Apply(
            Apply(
              TypeApply(Select(Ident("Shader"), "apply"), _),
              headers
            ),
            List(
              Block(
                Nil,
                Block(
                  List(
                    DefDef(
                      "$anonfun",
                      List(TermParamClause(List(ValDef(envVarName, Inferred(), None)))),
                      Inferred(),
                      Some(term)
                    )
                  ),
                  Closure(Ident("$anonfun"), None)
                )
              )
            )
          ) =>
        ShaderAST.ShaderBlock(Option(envVarName), headers.map(walkTerm), List(walkTerm(term)))

      // Entry point (no type params, no headers)
      case Apply(Select(Ident("Shader"), "apply"), args) =>
        ShaderAST.ShaderBlock(None, Nil, args.map(walkTerm))

      // Entry point (no type params, with headers)
      case Apply(Apply(Select(Ident("Shader"), "apply"), headers), args) =>
        ShaderAST.ShaderBlock(None, headers.map(walkTerm), args.map(walkTerm))

      case Apply(Select(Ident("RawGLSL"), "apply"), List(term)) =>
        walkTerm(term)

      case Apply(Select(Ident("vec2"), "apply"), args) =>
        args match
          case List(Typed(Repeated(args2, _), _)) =>
            ShaderAST.DataTypes.vec2(args2.map(p => walkTerm(p)))
          case _ =>
            ShaderAST.DataTypes.vec2(args.map(p => walkTerm(p)))

      case Apply(Select(Ident("vec3"), "apply"), args) =>
        args match
          case List(Typed(Repeated(args2, _), _)) =>
            ShaderAST.DataTypes.vec3(args2.map(p => walkTerm(p)))
          case _ =>
            ShaderAST.DataTypes.vec3(args.map(p => walkTerm(p)))

      case Apply(Select(Ident("vec4"), "apply"), args) =>
        args match
          case List(Typed(Repeated(args2, _), _)) =>
            ShaderAST.DataTypes.vec4(args2.map(p => walkTerm(p)))
          case _ =>
            ShaderAST.DataTypes.vec4(args.map(p => walkTerm(p)))

      //

      case Apply(Select(Ident(id), "apply"), args) =>
        val (fnName, rt) = proxies.lookUp(id, id -> Option(ShaderAST.DataTypes.ident("void")))
        ShaderAST.CallFunction(fnName, args.map(x => walkTerm(x)), Nil, rt)

      // Generally walking the tree

      case Apply(TypeApply(Select(Ident(id), "apply"), _), List(x)) =>
        ShaderAST.NamedBlock("", id, walkTerm(x))

      case Apply(TypeApply(Select(Ident(namespace), name), _), List(x)) =>
        ShaderAST.NamedBlock(namespace, name, walkTerm(x))

      case Apply(TypeApply(term, _), List(x)) =>
        walkTerm(x)

      // Extension method applies...
      case Apply(Select(Select(Inlined(_, _, _), "vec2"), "apply"), args) =>
        ShaderAST.DataTypes.vec2(args.map(p => walkTerm(p)))

      case Apply(Select(Select(Inlined(_, _, _), "vec3"), "apply"), args) =>
        ShaderAST.DataTypes.vec3(args.map(p => walkTerm(p)))

      case Apply(Select(Select(Inlined(_, _, _), "vec4"), "apply"), args) =>
        ShaderAST.DataTypes.vec4(args.map(p => walkTerm(p)))

      // Casting

      case Select(term, "toInt") =>
        ShaderAST.Cast(walkTerm(term), "int")

      case Select(term, "toFloat") =>
        ShaderAST.Cast(walkTerm(term), "float")

      // Read a field

      case Select(Inlined(None, Nil, Ident(obj)), fieldName) =>
        ShaderAST.DataTypes.ident(s"$obj.$fieldName")

      case Select(Ident(namespace), name) =>
        ShaderAST.DataTypes.ident(s"$namespace.$name")

      // Native method call.
      case Apply(Ident(name), List(Inlined(None, Nil, Ident(defRef)))) =>
        val (fnName, _)           = proxies.lookUp(defRef)
        val args: List[ShaderAST] = List(ShaderAST.DataTypes.ident(fnName))
        ShaderAST.CallFunction(name, args, args, None)

      // Annotations

      case Apply(Select(New(tree), _), List()) =>
        walkTree(tree)

      //

      case Apply(Select(term, "apply"), xs) =>
        walkTerm(term).find {
          case ShaderAST.CallFunction(_, _, _, _) => true
          case _                                  => false
        } match
          case Some(ShaderAST.CallFunction(id, Nil, Nil, rt)) =>
            ShaderAST.CallFunction(id, xs.map(tt => walkTerm(tt)), Nil, rt)

          case Some(ShaderAST.CallFunction(id, args, argNames, rt)) =>
            ShaderAST.CallFunction(id, xs.map(tt => walkTerm(tt)), argNames, rt)

          case _ =>
            ShaderAST.Block(xs.map(tt => walkTerm(tt)))

      case Apply(Select(term, op), xs) =>
        op match
          case "+" | "-" | "*" | "/" | "<" | ">" | "==" | "<=" | ">=" =>
            val lhs = walkTerm(term)
            val rhs = xs.headOption.map(tt => walkTerm(tt)).getOrElse(ShaderAST.Empty())
            val rt  = findReturnType(lhs)
            ShaderAST.Infix(op, lhs, rhs, rt)

          case _ =>
            throw new Exception("Shaders do not support infix operator: " + op)

      case Apply(Ident(name), terms) =>
        ShaderAST.CallFunction(name, terms.map(tt => walkTerm(tt)), Nil, None)

      case Inlined(None, _, term) =>
        walkTerm(term)

      case Inlined(Some(Ident(_)), _, term) =>
        walkTerm(term)

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
        walkTerm(term)

      // Swizzle
      case Inlined(Some(Apply(Ident(name), List(gt @ Apply(Select(Ident(genType), "apply"), args)))), _, _)
          if isSwizzle.matches(name) && isSwizzleable.matches(genType) =>
        ShaderAST.DataTypes.swizzle(
          walkTerm(gt),
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
        val callArgs   = args.map(tt => walkTerm(tt))
        val pairedArgs = callArgs.zip(argNames)
        val fnArgs: List[(ShaderAST, String)] =
          pairedArgs.map { p =>
            val typ = p._1.typeIdent.getOrElse(ShaderAST.DataTypes.ident("void"))
            typ -> p._2
          }

        ds.map(s => walkStatement(s))
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

        val body       = walkTerm(term)
        val returnType = findReturnType(walkTree(typeTree))

        shaderDefs += FunctionLookup(
          ShaderAST.Function(name, fnArgs, body, returnType),
          false // Should be true, refactor when I revisit inline defs...
        )
        val nmes = argNames.map(ShaderAST.DataTypes.ident.apply)
        ShaderAST.CallFunction(name, callArgs, nmes, returnType)

      case Inlined(Some(Select(This(_), _)), _, term) =>
        walkTerm(term)

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
        ShaderAST.RawLiteral(uboUtils.extractUBO(tt).render + "\n")

      case Inlined(Some(tree: Tree), _, _) =>
        walkTree(tree)

      case TypeApply(term, _) =>
        walkTerm(term)

      // Anonymous function?
      case Typed(
            Block(List(DefDef(_, args, _, Some(term))), Closure(Ident("$anonfun"), None)),
            Applied(_, types)
          ) =>
        val typesRendered: List[ShaderAST] = types.map(p => walkTree(p))

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
          ShaderAST.Function(fn, arguments, walkTerm(term), returnType),
          false
        )
        val nmes = argNames.map(ShaderAST.DataTypes.ident.apply)
        ShaderAST.CallFunction(fn, Nil, nmes, returnType)

      case Typed(term, _) =>
        walkTerm(term)

      case Block(statements, Closure(Ident("$anonfun"), None)) =>
        val ss = statements
          .map(s => walkStatement(s))

        ShaderAST.Block(ss)

      case Block(statements, term) =>
        val ss =
          statements.map(s => walkStatement(s)) :+ walkTerm(term)

        ShaderAST.Block(ss)

      // Literals

      case Literal(FloatConstant(f)) =>
        ShaderAST.DataTypes.float(f)

      case Literal(IntConstant(i)) =>
        ShaderAST.DataTypes.int(i)

      case Literal(UnitConstant()) =>
        ShaderAST.Empty()

      case Literal(NullConstant()) =>
        ShaderAST.Empty()

      case Literal(StringConstant(raw)) =>
        ShaderAST.RawLiteral(raw)

      case Literal(constant) =>
        throw new Exception("Shaders do not support constant type: " + constant.show)

      // Refs

      case Ident(name) =>
        val resolvedName = proxies.lookUp(name)._1

        shaderDefs.toList.find(_.fn.id == resolvedName).map(_.fn) match
          case None =>
            ShaderAST.DataTypes.ident(resolvedName)

          case Some(ShaderAST.Function(_, _, _, rt)) =>
            ShaderAST.CallFunction(resolvedName, Nil, Nil, rt)

      case Closure(Ident("$anonfun"), None) =>
        ShaderAST.Empty()

      case Closure(term, typeRepr) =>
        ShaderAST.DataTypes.closure(walkTerm(term), typeRepr.map(_.toString))

      case Wildcard() =>
        throw new Exception("Shaders do not support wildcards.")

      case Select(term, _) => // term, name
        walkTerm(term)

      // Unsupported (yet?)

      case This(_) =>
        throw new Exception("Shaders do not support 'this'.")

      case New(_) =>
        throw new Exception("Shaders do not support 'new'.")

      case NamedArg(_, _) =>
        throw new Exception("Shaders do not support named args.")

      case Super(_, _) =>
        throw new Exception("Shaders do not support super.")

      case Assign(lhs, rhs) =>
        ShaderAST.Assign(
          walkTerm(lhs),
          walkTerm(rhs)
        )

      case If(condTerm, thenTerm, elseTerm) =>
        walkTerm(elseTerm) match
          case ShaderAST.Empty() =>
            ShaderAST.If(
              walkTerm(condTerm),
              walkTerm(thenTerm),
              None
            )

          case e =>
            ShaderAST.If(
              walkTerm(condTerm),
              walkTerm(thenTerm),
              Option(e)
            )

      case Match(term, cases) =>
        val cs =
          cases.map {
            case CaseDef(Literal(IntConstant(i)), None, caseTerm) =>
              (Option(i), walkTerm(caseTerm))

            case CaseDef(Wildcard(), None, caseTerm) =>
              (None, walkTerm(caseTerm))

            case _ =>
              throw new Exception("Shaders only support pattern matching on `Int` values or `_` wildcards.")
          }

        ShaderAST.Switch(walkTerm(term), cs)

      case SummonFrom(_) =>
        throw new Exception("Shaders do not support summoning.")

      case Try(_, _, _) =>
        throw new Exception("Shaders do not support try blocks.")

      case Return(_, _) =>
        throw new Exception("Shaders do not support return statements.")

      case Repeated(args, _) =>
        ShaderAST.Block(args.map(walkTerm))

      case SelectOuter(_, _, _) =>
        throw new Exception("Shaders do not support outer selectors.")

      case While(cond, body) =>
        ShaderAST.While(walkTerm(cond), walkTerm(body))
