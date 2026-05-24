package ultraviolet.utils

import ultraviolet.macros.ShaderMacros
import ultraviolet.datatypes.ProceduralShader
import ultraviolet.datatypes.ShaderAST
import ultraviolet.datatypes.ShaderAST.*
import ultraviolet.datatypes.Shader
import ultraviolet.datatypes.ProgramVersion

/** Renders a `ProceduralShader` or `ShaderAST` as an indented tree, intended for debugging.
  *
  * The output is a multi-line `String` showing the structure of the AST itself (not the GLSL it would
  * produce - for that, see [[ShaderPrinter]]). Useful in unit tests for `println` inspection or string
  * matching, and for comparing the AST before and after transformers are applied (see
  * `Shader.toAST` and `Shader.toASTTransformed`).
  */
object ShaderASTPrinter:
  
  inline def printAST[In, Out](inline shader: Shader[In, Out]): String =
    val proc = ShaderMacros.toAST(shader)
    print(proc)
  
  inline def printASTTransformed[In, Out](inline shader: Shader[In, Out], inline version: ProgramVersion): String =
    val proc = ShaderMacros.toASTTransformed(shader, version)
    print(proc)

  def print(p: ProceduralShader): String =
    val parts = List(
      "ProceduralShader",
      section("defs", p.defs, 1),
      section("ubos", p.ubos, 1),
      section("annotations", p.annotations, 1),
      "  main:",
      render(p.main, 2)
    )
    parts.mkString("\n")

  def print(ast: ShaderAST): String =
    render(ast, 0)

  private def section(name: String, items: List[ShaderAST], indent: Int): String =
    val pad = "  " * indent
    if items.isEmpty then s"$pad$name: (empty)"
    else s"$pad$name:\n" + items.map(a => render(a, indent + 1)).mkString("\n")

  private def summary(ast: ShaderAST): String =
    ast match
      case Empty()                => "_"
      case DataTypes.ident(id)    => id
      case DataTypes.external(id) => s"ext:$id"
      case other                  => other.typeIdent.id

  private def render(ast: ShaderAST, indent: Int): String =
    val pad = "  " * indent

    def withChildren(header: String, children: List[ShaderAST]): String =
      val rendered = children.filterNot(_ => false).map(c => render(c, indent + 1))
      if rendered.isEmpty then s"$pad$header"
      else s"$pad$header\n${rendered.mkString("\n")}"

    ast match
      case Empty() =>
        s"${pad}Empty"

      case Block(stmts) =>
        withChildren("Block", stmts)

      case Neg(v) =>
        withChildren("Neg", List(v))

      case Not(v) =>
        withChildren("Not", List(v))

      case UBO(uboDef) =>
        val head = s"${pad}UBO '${uboDef.name}'"
        if uboDef.fields.isEmpty then head
        else
          val inner = "  " * (indent + 1)
          val fieldLines = uboDef.fields.map { f =>
            val prec = f.precision.fold("")(p => s"$p ")
            s"$inner${prec}${f.typeOf} ${f.name}"
          }
          s"$head\n${fieldLines.mkString("\n")}"

      case Struct(name, members) =>
        withChildren(s"Struct '$name'", members)

      case New(name, args) =>
        withChildren(s"New '$name'", args)

      case ShaderBlock(in, out, envVar, stmts) =>
        val info = s"in=${in.getOrElse("?")} out=${out.getOrElse("?")} env=${envVar.getOrElse("_")}"
        withChildren(s"ShaderBlock $info", stmts)

      case Function(id, args, body, rt) =>
        val argStr = args.map { case (typeAst, name) => s"${summary(typeAst)} $name" }.mkString(", ")
        withChildren(s"Function '$id'($argStr) : ${summary(rt)}", List(body))

      case CallFunction(id, args, rt) =>
        withChildren(s"CallFunction '$id' : ${summary(rt)}", args)

      case CallExternalFunction(id, args, rt) =>
        withChildren(s"CallExternalFunction '$id' : ${summary(rt)}", args)

      case FunctionRef(id, args, rt) =>
        withChildren(s"FunctionRef '$id' : ${summary(rt)}", args)

      case Cast(value, as) =>
        withChildren(s"Cast as $as", List(value))

      case Infix(op, l, r, rt) =>
        withChildren(s"Infix '$op' : ${summary(rt)}", List(l, r))

      case Assign(l, r) =>
        withChildren("Assign", List(l, r))

      case If(c, t, e) =>
        withChildren("If", List(c, t) ++ e.toList)

      case While(c, b) =>
        withChildren("While", List(c, b))

      case For(i, c, n, b) =>
        withChildren("For", List(i, c, n, b))

      case Switch(on, cases) =>
        val head    = s"${pad}Switch"
        val onLine  = render(on, indent + 1)
        val inner   = "  " * (indent + 1)
        val caseLines = cases.map { case (label, body) =>
          val l = label.fold("default")(_.toString)
          s"$inner case $l:\n${render(body, indent + 2)}"
        }
        (head :: onLine :: caseLines).mkString("\n")

      case Val(id, value, typeOf) =>
        withChildren(s"Val '$id' : ${summary(typeOf)}", List(value))

      case MultiStatements(leading, stmts) =>
        withChildren("MultiStatements", leading :: stmts)

      case Annotated(name, param, value) =>
        withChildren(s"Annotated ${summary(name)}", List(param, value))

      case RawLiteral(v) =>
        s"${pad}RawLiteral '$v'"

      case Field(term, field) =>
        withChildren("Field", List(term, field))

      case DataTypes.ident(id) =>
        s"${pad}ident '$id'"

      case DataTypes.external(id) =>
        s"${pad}external '$id'"

      case DataTypes.index(id, at) =>
        withChildren(s"index '$id'", List(at))

      case DataTypes.externalIndex(id, at) =>
        withChildren(s"externalIndex '$id'", List(at))

      case DataTypes.bool(b) =>
        s"${pad}bool $b"

      case DataTypes.float(v) =>
        s"${pad}float $v"

      case DataTypes.int(v) =>
        s"${pad}int $v"

      case DataTypes.vec2(args)  => withChildren("vec2", args)
      case DataTypes.vec3(args)  => withChildren("vec3", args)
      case DataTypes.vec4(args)  => withChildren("vec4", args)
      case DataTypes.bvec2(args) => withChildren("bvec2", args)
      case DataTypes.bvec3(args) => withChildren("bvec3", args)
      case DataTypes.bvec4(args) => withChildren("bvec4", args)
      case DataTypes.ivec2(args) => withChildren("ivec2", args)
      case DataTypes.ivec3(args) => withChildren("ivec3", args)
      case DataTypes.ivec4(args) => withChildren("ivec4", args)
      case DataTypes.mat2(args)  => withChildren("mat2", args)
      case DataTypes.mat3(args)  => withChildren("mat3", args)
      case DataTypes.mat4(args)  => withChildren("mat4", args)

      case DataTypes.array(size, args, typeOf) =>
        withChildren(s"array[$size] : ${summary(typeOf)}", args)

      case DataTypes.swizzle(gen, sw, rt) =>
        withChildren(s"swizzle '$sw' : ${summary(rt)}", List(gen))
