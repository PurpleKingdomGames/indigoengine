package ultraviolet.datatypes

import ShaderAST.*

trait ShaderPrinter:
  def transformer: PartialFunction[ShaderAST, ShaderAST]
  def printer: PartialFunction[ShaderAST, List[String]]

object ShaderPrinter:

  given ShaderPrinter = new ShaderPrinter:
    def transformer: PartialFunction[ShaderAST, ShaderAST] = PartialFunction.empty
    def printer: PartialFunction[ShaderAST, List[String]]  = PartialFunction.empty

  def print(ast: ShaderAST)(using pp: ShaderPrinter): List[String] =
    render(ast)

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def render(ast: ShaderAST)(using pp: ShaderPrinter): List[String] =
    val p =
      pp.printer.orElse {
        case Empty() =>
          Nil

        case Block(statements) =>
          renderStatements(statements)

        case ShaderBlock(_, _, envVarName, headers, statements) =>
          renderStatements(statements)

        case b @ NamedBlock(namespace, id, statements) =>
          throw new Exception("NamedBlock found, this is probably an error: " + b)

        case Function(id, args, body, returnType) if id.isEmpty =>
          throw new Exception("Failed to render shader, unnamed function definition found.")

        case Function(id, args, Block(statements), returnType) =>
          val (body, rt) = processFunctionStatements(statements, returnType.flatMap(r => render(r).headOption))
          List(
            List(s"""$rt $id(${args.map(s => s"in ${render(s._1).mkString} ${s._2}").mkString(",")}){"""),
            body.map(addIndent),
            List("}")
          ).flatten

        case b @ Function(id, args, NamedBlock(_, _, statements), returnType) =>
          throw new Exception("Function with a NamedBlock body found, this is probably an error: " + b)

        case Function(id, args, statement, returnType) =>
          val (body, rt) = processFunctionStatements(List(statement), returnType.flatMap(r => render(r).headOption))
          List(
            List(s"""$rt $id(${args.map(s => s"in ${render(s._1).mkString} ${s._2}").mkString(",")}){"""),
            body.map(addIndent),
            List("}")
          ).flatten

        case CallFunction(id, args, _, _) =>
          List(s"""$id(${args.flatMap(render).mkString(",")})""")

        case FunctionRef(_, _) =>
          Nil

        case Cast(value, as) =>
          List(s"""$as(${render(value).mkString})""")

        case Infix(op, left, right, returnType) =>
          val isSimpleValue = """^([a-zA-Z0-9\[\]\(\)\.\,_]+)$""".r
          val hasBrackets   = """^\((.*)\)$""".r

          def cleanUp(str: String): String =
            str match
              case s if hasBrackets.matches(s) || isSimpleValue.matches(s) => s
              case s                                                       => s"""($s)"""

          val l = render(left).mkString
          val r = render(right).mkString
          List(s"""${cleanUp(l)}$op${cleanUp(r)}""")

        case Assign(left, right) =>
          List(s"""${render(left).mkString}=${render(right).mkString}""")

        case If(cond, thenTerm, elseTerm) =>
          elseTerm match
            case None =>
              List(
                List(s"""if(${render(cond).mkString}){"""),
                render(thenTerm).map(addIndent),
                List(s"""}""")
              ).flatten

            case Some(els) =>
              List(
                List(s"""if(${render(cond).mkString}){"""),
                render(thenTerm).map(addIndent),
                List(s"""}else{"""),
                render(els).map(addIndent),
                List(s"""}""")
              ).flatten

        case While(cond, body) =>
          List(
            List(s"""while(${render(cond).mkString}){"""),
            render(body).map(addIndent),
            List("""}""")
          ).flatten

        case Switch(on, cases) =>
          val cs =
            cases.flatMap {
              case (Some(i), body) =>
                List(
                  List(s"""case $i:"""),
                  render(body).map(addIndent),
                  List(s"""  break;""")
                ).flatten

              case (None, body) =>
                List(
                  List(s"""default:"""),
                  render(body).map(addIndent),
                  List(s"""  break;""")
                ).flatten
            }

          List(
            List(s"""switch(${render(on).mkString}){"""),
            cs.map(addIndent),
            List(s"""}""")
          ).flatten

        case c @ DataTypes.closure(body, typeOf) =>
          throw new Exception("Closure found, this is probably an error: " + c)

        case DataTypes.ident(id) if id.endsWith(".length") =>
          List(s"$id()")

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

        case DataTypes.array(size, typeOf) =>
          List(s"array goes here...")

        case DataTypes.swizzle(genType, swizzle, returnType) =>
          List(s"${render(genType).mkString}.$swizzle")

        case Val(id, value, typeOf) =>
          val tOf = typeOf.getOrElse("void")
          value match
            // This rearranges `vec2[16] foo` to `vec2 foo[16]`, both are valid,
            // however the original is easier once we get to multidimensional arrays
            // (not available until GLSL 4!)
            // case Empty() if tOf.endsWith("]") && tOf.contains("[") =>
            //   // array
            //   val (tName, tSize) = tOf.splitAt(tOf.indexOf("["))
            //   List(s"""$tName $id$tSize""")

            case Empty() =>
              List(s"""$tOf $id""")

            case _ =>
              List(s"""$tOf $id=${render(value).mkString}""")

        case Annotated(label, _, value) =>
          val lbl = render(label).mkString
          value match
            case v @ Val(id, value, typeOf) if lbl == "const" =>
              List(s"""$lbl ${render(v).mkString}""")

            case v @ Val(id, value, typeOf) =>
              List(s"""$lbl ${render(Val(id, Empty(), typeOf)).mkString}""")

            case _ =>
              Nil

        case RawLiteral(body) =>
          List(body)
      }

    p(ast.traverse(pp.transformer.orElse(n => n)))

  private def renderStatements(statements: List[ShaderAST])(using pp: ShaderPrinter): List[String] =
    val p =
      pp.printer.orElse {
        case ShaderAST.RawLiteral(raw) =>
          List(raw)

        case f: ShaderAST.Function =>
          render(f)

        case ShaderAST.Block(ss) =>
          renderStatements(ss)

        case x =>
          render(x)
            .map {
              case s if s.isEmpty()     => s
              case s if s.endsWith(";") => s
              case s if s.endsWith(":") => s
              case s if s.endsWith("{") => s
              case s if s.endsWith("}") => s
              case s                    => s + ";"
            }
      }

    statements
      .map(_.traverse(pp.transformer.orElse(n => n)))
      .flatMap(p)
      .filterNot(_.isEmpty)

  private def addIndent: String => String = str => "  " + str

  private def decideType(a: ShaderAST)(using pp: ShaderPrinter): Option[String] =
    a match
      case Empty()                      => None
      case Block(_)                     => None
      case NamedBlock(_, _, _)          => None
      case ShaderBlock(_, _, _, _, _)   => None
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
      case Annotated(_, _, value)       => decideType(value)
      case RawLiteral(_)                => None
      case n @ DataTypes.ident(_)       => None
      case DataTypes.closure(_, typeOf) => typeOf
      case DataTypes.float(v)           => Option("float")
      case DataTypes.int(v)             => Option("int")
      case DataTypes.vec2(args)         => Option("vec2")
      case DataTypes.vec3(args)         => Option("vec3")
      case DataTypes.vec4(args)         => Option("vec4")
      case DataTypes.array(_, typeOf)   => typeOf
      case DataTypes.swizzle(v, _, rt)  => rt.toList.flatMap(render).headOption

  private def rf(f: Float): String =
    val s = f.toString
    if s.contains(".") then s else s + ".0"

  private def processFunctionStatements(
      statements: List[ShaderAST],
      maybeReturnType: Option[String]
  )(using pp: ShaderPrinter): (List[String], String) =
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
