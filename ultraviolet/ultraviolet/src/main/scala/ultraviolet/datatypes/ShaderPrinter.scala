package ultraviolet.datatypes

trait ShaderPrinter:
  def print(ast: ShaderAST): List[String]

object ShaderPrinter:

  given ShaderPrinter = new DefaultPrinter

  final class DefaultPrinter extends ShaderPrinter:
    import ShaderAST.*

    def print(ast: ShaderAST): List[String] = render(ast)

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    private def render: ShaderAST => List[String] =
      case Empty() =>
        Nil

      case Block(statements) =>
        renderStatements(statements)

      case ShaderBlock(envVarName, headers, statements) =>
        renderStatements(statements)

      case b @ NamedBlock(namespace, id, statements) =>
        throw new Exception("NamedBlock found, this is probably an error: " + b)

      case Function(id, args, body, returnType) if id.isEmpty =>
        throw new Exception("Failed to render shader, unnamed function definition found.")

      case Function(id, args, Block(statements), returnType) =>
        val (body, rt) = processFunctionStatements(statements, returnType.flatMap(r => render(r).headOption))
        List(
          List(s"""$rt $id(${args.map(s => s"in ${render(s._1).mkString} ${s._2}").mkString(",")}){"""),
          body.map(correctIndent),
          List("}")
        ).flatten

      case b @ Function(id, args, NamedBlock(_, _, statements), returnType) =>
        throw new Exception("Function with a NamedBlock body found, this is probably an error: " + b)

      case Function(id, args, statement, returnType) =>
        val (body, rt) = processFunctionStatements(List(statement), returnType.flatMap(r => render(r).headOption))
        List(
          List(s"""$rt $id(${args.map(s => s"in ${render(s._1).mkString} ${s._2}").mkString(",")}){"""),
          body.map(correctIndent),
          List("}")
        ).flatten

      case CallFunction(id, args, _, _) =>
        List(s"""$id(${args.flatMap(render).mkString(",")})""")

      case FunctionRef(_, _) =>
        Nil

      case Cast(value, as) =>
        List(s"""$as(${render(value).mkString})""")

      case Infix(op, left, right, returnType) =>
        List(s"""(${render(left).mkString})$op(${render(right).mkString})""")

      case Assign(left, right) =>
        List(s"""${render(left).mkString}=${render(right).mkString}""")

      case If(cond, thenTerm, elseTerm) =>
        elseTerm match
          case None =>
            List(s"""if(${render(cond).mkString}){${render(thenTerm).mkString};}""")

          case Some(els) =>
            List(s"""if(${render(cond).mkString}){${render(thenTerm).mkString};}else{${render(els).mkString};}""")

      case While(cond, body) =>
        List(
          s"""while(${render(cond).mkString}){${render(body).mkString}}"""
        )

      case Switch(on, cases) =>
        val cs =
          cases.map {
            case (Some(i), body) =>
              s"case $i:${render(body).mkString}break;"

            case (None, body) =>
              s"default:${render(body).mkString}break;"
          }

        List(s"""switch(${render(on).mkString}){${cs.mkString}}""")

      case c @ DataTypes.closure(body, typeOf) =>
        throw new Exception("Closure found, this is probably an error: " + c)

      case DataTypes.ident(id) =>
        List(s"$id")

      case DataTypes.float(v) =>
        List(s"${rf(v)}")

      case DataTypes.int(v) =>
        List(s"${v.toString}")

      case DataTypes.vec2(args) =>
        List(s"vec2(${args.flatMap(render).mkString(",")})")

      case DataTypes.vec3(args) =>
        List(s"vec3(${args.flatMap(render).mkString(",")})")

      case DataTypes.vec4(args) =>
        List(s"vec4(${args.flatMap(render).mkString(",")})")

      case DataTypes.swizzle(genType, swizzle, returnType) =>
        List(s"${render(genType).mkString}.$swizzle")

      case Val(id, value, typeOf) =>
        val tOf = typeOf.getOrElse("void")
        value match
          case Empty() =>
            List(s"""$tOf $id""")

          case _ =>
            List(s"""$tOf $id=${render(value).mkString}""")

      case Annotated(label, value) =>
        value match
          case Val(id, value, typeOf) =>
            List(s"""${render(label).mkString} ${render(Val(id, Empty(), typeOf)).mkString}""")

          case _ =>
            Nil

      case RawLiteral(body) =>
        List(body)

    private def renderStatements(statements: List[ShaderAST]): List[String] =
      statements
        .flatMap {
          case ShaderAST.RawLiteral(raw) =>
            List(raw)

          case f: ShaderAST.Function =>
            render(f)

          case ShaderAST.Block(ss) =>
            renderStatements(ss)

          case x =>
            render(x).map(r => if r.isEmpty then r else r + ";")
        }
        .filterNot(_.isEmpty)

    private def correctIndent: String => String = str =>
      if str.startsWith("  ") then str else "  " + str

    private def decideType(a: ShaderAST): Option[String] =
      a match
        case Empty()                      => None
        case Block(_)                     => None
        case NamedBlock(_, _, _)          => None
        case ShaderBlock(_, _, _)         => None
        case Function(_, _, _, rt)        => rt.toList.flatMap(render).headOption
        case CallFunction(_, _, _, rt)    => rt.toList.flatMap(render).headOption
        case FunctionRef(_, rt)           => rt.toList.flatMap(render).headOption
        case Cast(_, as)                  => Option(as)
        case Infix(_, _, _, rt)           => rt.toList.flatMap(render).headOption
        case Assign(_, _)                 => None
        case If(_, _, _)                  => None
        case While(_, _)                  => None
        case Switch(_, _)                 => None
        case Val(id, value, typeOf)       => typeOf
        case Annotated(id, value)         => decideType(value)
        case RawLiteral(_)                => None
        case n @ DataTypes.ident(_)       => None
        case DataTypes.closure(_, typeOf) => typeOf
        case DataTypes.float(v)           => Option("float")
        case DataTypes.int(v)             => Option("int")
        case DataTypes.vec2(args)         => Option("vec2")
        case DataTypes.vec3(args)         => Option("vec3")
        case DataTypes.vec4(args)         => Option("vec4")
        case DataTypes.swizzle(v, _, rt)  => rt.toList.flatMap(render).headOption

    private def rf(f: Float): String =
      val s = f.toString
      if s.contains(".") then s else s + ".0"

    private def processFunctionStatements(
        statements: List[ShaderAST],
        maybeReturnType: Option[String]
    ): (List[String], String) =
      val nonEmpty = statements
        .filterNot(_.isEmpty)

      val (init, last) =
        if nonEmpty.length > 1 then (nonEmpty.dropRight(1), nonEmpty.takeRight(1))
        else (Nil, nonEmpty)

      val returnType =
        maybeReturnType match
          case None        => last.headOption.flatMap(decideType).getOrElse("void")
          case Some(value) => value

      val body =
        renderStatements(init) ++
          List(
            last.headOption
              .map { ss =>
                (if returnType != "void" then "return " else "") + renderStatements(List(ss)).mkString
              }
              .getOrElse("")
          )

      (body, returnType)

  end DefaultPrinter
