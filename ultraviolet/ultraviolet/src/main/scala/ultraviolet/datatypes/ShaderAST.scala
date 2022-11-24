package ultraviolet.datatypes

import scala.annotation.tailrec
import scala.deriving.Mirror
import scala.quoted.*

sealed trait ShaderAST derives CanEqual
object ShaderAST:

  given ToExpr[ShaderAST] with {
    def apply(x: ShaderAST)(using Quotes): Expr[ShaderAST] =
      x match
        case v: Empty        => Expr(v)
        case v: Block        => Expr(v)
        case v: UBO          => Expr(v)
        case v: ShaderBlock  => Expr(v)
        case v: Function     => Expr(v)
        case v: CallFunction => Expr(v)
        case v: FunctionRef  => Expr(v)
        case v: Cast         => Expr(v)
        case v: Infix        => Expr(v)
        case v: Assign       => Expr(v)
        case v: If           => Expr(v)
        case v: While        => Expr(v)
        case v: For          => Expr(v)
        case v: Switch       => Expr(v)
        case v: DataTypes    => Expr(v)
        case v: Val          => Expr(v)
        case v: Annotated    => Expr(v)
        case v: RawLiteral   => Expr(v)
  }

  final case class Empty() extends ShaderAST
  object Empty:
    given ToExpr[Empty] with {
      def apply(x: Empty)(using Quotes): Expr[Empty] =
        '{ Empty() }
    }

  final case class Block(statements: List[ShaderAST]) extends ShaderAST
  object Block:
    given ToExpr[Block] with {
      def apply(x: Block)(using Quotes): Expr[Block] =
        '{ Block(${ Expr(x.statements) }) }
    }

    def apply(statements: ShaderAST*): Block =
      Block(statements.toList)

  final case class UBO(uboDef: UBODef) extends ShaderAST
  object UBO:
    given ToExpr[UBO] with {
      def apply(x: UBO)(using Quotes): Expr[UBO] =
        '{ UBO(${ Expr(x.uboDef) }) }
    }

  // Specifically handles our 'Shader' type
  final case class ShaderBlock(
      inType: Option[String],
      outType: Option[String],
      envVarName: Option[String],
      headers: List[ShaderAST],
      statements: List[ShaderAST]
  ) extends ShaderAST
  object ShaderBlock:
    given ToExpr[ShaderBlock] with {
      def apply(x: ShaderBlock)(using Quotes): Expr[ShaderBlock] =
        '{
          ShaderBlock(
            ${ Expr(x.inType) },
            ${ Expr(x.outType) },
            ${ Expr(x.envVarName) },
            ${ Expr(x.headers) },
            ${ Expr(x.statements) }
          )
        }
    }

  final case class Function(id: String, args: List[(ShaderAST, String)], body: ShaderAST, returnType: Option[ShaderAST])
      extends ShaderAST
  object Function:
    given ToExpr[Function] with {
      def apply(x: Function)(using Quotes): Expr[Function] =
        '{ Function(${ Expr(x.id) }, ${ Expr(x.args) }, ${ Expr(x.body) }, ${ Expr(x.returnType) }) }
    }

  final case class CallFunction(
      id: String,
      args: List[ShaderAST],
      argNames: List[ShaderAST],
      returnType: Option[ShaderAST]
  ) extends ShaderAST
  object CallFunction:
    given ToExpr[CallFunction] with {
      def apply(x: CallFunction)(using Quotes): Expr[CallFunction] =
        '{ CallFunction(${ Expr(x.id) }, ${ Expr(x.args) }, ${ Expr(x.argNames) }, ${ Expr(x.returnType) }) }
    }

  // Allows things high up the tree to gain a reference to created functions.
  final case class FunctionRef(id: String, returnType: Option[ShaderAST]) extends ShaderAST
  object FunctionRef:
    given ToExpr[FunctionRef] with {
      def apply(x: FunctionRef)(using Quotes): Expr[FunctionRef] =
        '{ FunctionRef(${ Expr(x.id) }, ${ Expr(x.returnType) }) }
    }

  final case class Cast(
      value: ShaderAST,
      as: String
  ) extends ShaderAST
  object Cast:
    given ToExpr[Cast] with {
      def apply(x: Cast)(using Quotes): Expr[Cast] =
        '{ Cast(${ Expr(x.value) }, ${ Expr(x.as) }) }
    }

  final case class Infix(
      op: String,
      left: ShaderAST,
      right: ShaderAST,
      returnType: Option[ShaderAST]
  ) extends ShaderAST
  object Infix:
    given ToExpr[Infix] with {
      def apply(x: Infix)(using Quotes): Expr[Infix] =
        '{ Infix(${ Expr(x.op) }, ${ Expr(x.left) }, ${ Expr(x.right) }, ${ Expr(x.returnType) }) }
    }

  final case class Assign(
      left: ShaderAST,
      right: ShaderAST
  ) extends ShaderAST
  object Assign:
    given ToExpr[Assign] with {
      def apply(x: Assign)(using Quotes): Expr[Assign] =
        '{ Assign(${ Expr(x.left) }, ${ Expr(x.right) }) }
    }

  final case class If(
      condition: ShaderAST,
      thenTerm: ShaderAST,
      elseTerm: Option[ShaderAST]
  ) extends ShaderAST
  object If:
    given ToExpr[If] with {
      def apply(x: If)(using Quotes): Expr[If] =
        '{ If(${ Expr(x.condition) }, ${ Expr(x.thenTerm) }, ${ Expr(x.elseTerm) }) }
    }

  final case class While(
      condition: ShaderAST,
      body: ShaderAST
  ) extends ShaderAST
  object While:
    given ToExpr[While] with {
      def apply(x: While)(using Quotes): Expr[While] =
        '{ While(${ Expr(x.condition) }, ${ Expr(x.body) }) }
    }

  final case class For(
      initial: ShaderAST,
      condition: ShaderAST,
      next: ShaderAST,
      body: ShaderAST
  ) extends ShaderAST
  object For:
    given ToExpr[For] with {
      def apply(x: For)(using Quotes): Expr[For] =
        '{ For(${ Expr(x.initial) }, ${ Expr(x.condition) }, ${ Expr(x.next) }, ${ Expr(x.body) }) }
    }

  final case class Switch(
      on: ShaderAST,
      cases: List[(Option[Int], ShaderAST)]
  ) extends ShaderAST
  object Switch:
    given ToExpr[Switch] with {
      def apply(x: Switch)(using Quotes): Expr[Switch] =
        '{ Switch(${ Expr(x.on) }, ${ Expr(x.cases) }) }
    }

  final case class Val(id: String, value: ShaderAST, typeOf: Option[String]) extends ShaderAST
  object Val:
    given ToExpr[Val] with {
      def apply(x: Val)(using Quotes): Expr[Val] =
        '{ Val(${ Expr(x.id) }, ${ Expr(x.value) }, ${ Expr(x.typeOf) }) }
    }

  final case class Annotated(name: ShaderAST, param: ShaderAST, value: ShaderAST) extends ShaderAST
  object Annotated:
    given ToExpr[Annotated] with {
      def apply(x: Annotated)(using Quotes): Expr[Annotated] =
        '{ Annotated(${ Expr(x.name) }, ${ Expr(x.param) }, ${ Expr(x.value) }) }
    }

  final case class RawLiteral(value: String) extends ShaderAST
  object RawLiteral:
    given ToExpr[RawLiteral] with {
      def apply(x: RawLiteral)(using Quotes): Expr[RawLiteral] =
        '{ RawLiteral(${ Expr(x.value) }) }
    }

  enum DataTypes extends ShaderAST:
    case ident(id: String)
    case index(id: String, at: ShaderAST)
    case bool(b: Boolean)
    case float(v: Float)
    case int(v: Int)
    case vec2(args: List[ShaderAST])
    case vec3(args: List[ShaderAST])
    case vec4(args: List[ShaderAST])
    case mat2(args: List[ShaderAST])
    case mat3(args: List[ShaderAST])
    case mat4(args: List[ShaderAST])
    case array(size: Int, typeOf: Option[String])
    case swizzle(genType: ShaderAST, swizzle: String, returnType: Option[ShaderAST])

  object DataTypes:

    object vec2:
      def apply(args: Float*): DataTypes.vec2 =
        DataTypes.vec2(args.toList.map(DataTypes.float.apply))
    object vec3:
      def apply(args: Float*): DataTypes.vec3 =
        DataTypes.vec3(args.toList.map(DataTypes.float.apply))
    object vec4:
      def apply(args: Float*): DataTypes.vec4 =
        DataTypes.vec4(args.toList.map(DataTypes.float.apply))

    given ToExpr[DataTypes] with {
      def apply(x: DataTypes)(using Quotes): Expr[DataTypes] =
        x match
          case v: DataTypes.ident   => Expr(v)
          case v: DataTypes.index   => Expr(v)
          case v: DataTypes.bool    => Expr(v)
          case v: DataTypes.float   => Expr(v)
          case v: DataTypes.int     => Expr(v)
          case v: DataTypes.vec2    => Expr(v)
          case v: DataTypes.vec3    => Expr(v)
          case v: DataTypes.vec4    => Expr(v)
          case v: DataTypes.mat2    => Expr(v)
          case v: DataTypes.mat3    => Expr(v)
          case v: DataTypes.mat4    => Expr(v)
          case v: DataTypes.array   => Expr(v)
          case v: DataTypes.swizzle => Expr(v)
    }
    given ToExpr[ident] with {
      def apply(x: ident)(using Quotes): Expr[ident] =
        '{ ident(${ Expr(x.id) }) }
    }
    given ToExpr[index] with {
      def apply(x: index)(using Quotes): Expr[index] =
        '{ index(${ Expr(x.id) }, ${ Expr(x.at) }) }
    }
    given ToExpr[bool] with {
      def apply(x: bool)(using Quotes): Expr[bool] =
        '{ bool(${ Expr(x.b) }) }
    }
    given ToExpr[float] with {
      def apply(x: float)(using Quotes): Expr[float] =
        '{ float(${ Expr(x.v) }) }
    }
    given ToExpr[int] with {
      def apply(x: int)(using Quotes): Expr[int] =
        '{ int(${ Expr(x.v) }) }
    }
    given ToExpr[vec2] with {
      def apply(x: vec2)(using Quotes): Expr[vec2] =
        '{ vec2(${ Expr(x.args) }) }
    }
    given ToExpr[vec3] with {
      def apply(x: vec3)(using Quotes): Expr[vec3] =
        '{ vec3(${ Expr(x.args) }) }
    }
    given ToExpr[vec4] with {
      def apply(x: vec4)(using Quotes): Expr[vec4] =
        '{ vec4(${ Expr(x.args) }) }
    }
    given ToExpr[mat2] with {
      def apply(x: mat2)(using Quotes): Expr[mat2] =
        '{ mat2(${ Expr(x.args) }) }
    }
    given ToExpr[mat3] with {
      def apply(x: mat3)(using Quotes): Expr[mat3] =
        '{ mat3(${ Expr(x.args) }) }
    }
    given ToExpr[mat4] with {
      def apply(x: mat4)(using Quotes): Expr[mat4] =
        '{ mat4(${ Expr(x.args) }) }
    }
    given ToExpr[array] with {
      def apply(x: array)(using Quotes): Expr[array] =
        '{ array(${ Expr(x.size) }, ${ Expr(x.typeOf) }) }
    }
    given ToExpr[swizzle] with {
      def apply(x: swizzle)(using Quotes): Expr[swizzle] =
        '{ swizzle(${ Expr(x.genType) }, ${ Expr(x.swizzle) }, ${ Expr(x.returnType) }) }
    }

  extension (ast: ShaderAST)
    def isEmpty: Boolean =
      ast match
        case Empty() => true
        case _       => false

    def exists(p: ShaderAST): Boolean =
      find(_ == p).isDefined

    def find(p: ShaderAST => Boolean): Option[ShaderAST] =
      @tailrec
      def rec(remaining: List[ShaderAST]): Option[ShaderAST] =
        remaining match
          case Nil => None
          case x :: xs =>
            x match
              case v if p(v)                  => Option(v)
              case Empty()                    => rec(xs)
              case Block(s)                   => rec(s ++ xs)
              case UBO(_)                     => rec(xs)
              case ShaderBlock(_, _, _, _, s) => rec(s ++ xs)
              case Function(_, _, body, _)    => rec(body :: xs)
              case CallFunction(_, _, _, _)   => rec(xs)
              case FunctionRef(_, _)          => rec(xs)
              case Cast(v, _)                 => rec(v :: xs)
              case Infix(_, l, r, _)          => rec(l :: r :: xs)
              case Assign(l, r)               => rec(l :: r :: xs)
              case If(_, t, e)                => rec(t :: (e.toList ++ xs))
              case While(_, b)                => rec(b :: xs)
              case For(_, _, _, b)            => rec(b :: xs)
              case Switch(_, cs)              => rec(cs.map(_._2) ++ xs)
              case Val(_, body, _)            => rec(body :: xs)
              case Annotated(_, _, body)      => rec(body :: xs)
              case RawLiteral(_)              => rec(xs)
              case v: DataTypes.ident         => rec(xs)
              case v: DataTypes.index         => rec(xs)
              case v: DataTypes.bool          => rec(xs)
              case v: DataTypes.float         => rec(xs)
              case v: DataTypes.int           => rec(xs)
              case v: DataTypes.vec2          => rec(v.args ++ xs)
              case v: DataTypes.vec3          => rec(v.args ++ xs)
              case v: DataTypes.vec4          => rec(v.args ++ xs)
              case v: DataTypes.mat2          => rec(v.args ++ xs)
              case v: DataTypes.mat3          => rec(v.args ++ xs)
              case v: DataTypes.mat4          => rec(v.args ++ xs)
              case v: DataTypes.array         => rec(xs)
              case v: DataTypes.swizzle       => rec(v.genType :: xs)

      rec(List(ast))

    def findAll(p: ShaderAST => Boolean): List[ShaderAST] =
      @tailrec
      def rec(remaining: List[ShaderAST], acc: List[ShaderAST]): List[ShaderAST] =
        remaining match
          case Nil => acc.reverse
          case x :: xs =>
            x match
              case v if p(v)                  => rec(xs, v :: acc)
              case Empty()                    => rec(xs, acc)
              case Block(s)                   => rec(s ++ xs, acc)
              case UBO(_)                     => rec(xs, acc)
              case ShaderBlock(_, _, _, _, s) => rec(s ++ xs, acc)
              case Function(_, _, body, _)    => rec(body :: xs, acc)
              case CallFunction(_, _, _, _)   => rec(xs, acc)
              case FunctionRef(_, _)          => rec(xs, acc)
              case Cast(v, _)                 => rec(v :: xs, acc)
              case Infix(_, l, r, _)          => rec(l :: r :: xs, acc)
              case Assign(l, r)               => rec(l :: r :: xs, acc)
              case If(_, t, e)                => rec(t :: (e.toList ++ xs), acc)
              case While(_, b)                => rec(b :: xs, acc)
              case For(_, _, _, b)            => rec(b :: xs, acc)
              case Switch(_, cs)              => rec(cs.map(_._2) ++ xs, acc)
              case Val(_, body, _)            => rec(body :: xs, acc)
              case Annotated(_, _, body)      => rec(body :: xs, acc)
              case RawLiteral(_)              => rec(xs, acc)
              case v: DataTypes.ident         => rec(xs, acc)
              case v: DataTypes.index         => rec(xs, acc)
              case v: DataTypes.bool          => rec(xs, acc)
              case v: DataTypes.float         => rec(xs, acc)
              case v: DataTypes.int           => rec(xs, acc)
              case v: DataTypes.vec2          => rec(v.args ++ xs, acc)
              case v: DataTypes.vec3          => rec(v.args ++ xs, acc)
              case v: DataTypes.vec4          => rec(v.args ++ xs, acc)
              case v: DataTypes.mat2          => rec(v.args ++ xs, acc)
              case v: DataTypes.mat3          => rec(v.args ++ xs, acc)
              case v: DataTypes.mat4          => rec(v.args ++ xs, acc)
              case v: DataTypes.array         => rec(xs, acc)
              case v: DataTypes.swizzle       => rec(v.genType :: xs, acc)

      rec(List(ast), Nil)

    def prune: ShaderAST =
      def crush(statements: ShaderAST): ShaderAST =
        statements match
          case b: Block => b.copy(statements = b.statements.filterNot(_.isEmpty).map(crush))
          case other    => other

      crush(ast)

    def traverse(f: ShaderAST => ShaderAST): ShaderAST =
      ast match
        case v @ Empty()                              => f(v)
        case v @ Block(s)                             => f(Block(s.map(f)))
        case v @ UBO(_)                               => f(v)
        case v @ ShaderBlock(in, out, n, h, s)        => f(ShaderBlock(in, out, n, h, s))
        case v @ Function(id, args, body, returnType) => f(Function(id, args, f(body), returnType))
        case v @ CallFunction(_, _, _, _)             => f(v)
        case v @ FunctionRef(_, _)                    => f(v)
        case v @ Cast(value, as)                      => f(Cast(f(value), as))
        case v @ Infix(op, l, r, returnType)          => f(Infix(op, f(l), f(r), returnType))
        case v @ Assign(l, r)                         => f(Assign(f(l), f(r)))
        case v @ If(c, t, e)                          => f(If(c, f(t), e.map(f)))
        case v @ While(c, b)                          => f(While(c, f(b)))
        case v @ For(i, c, n, b)                      => f(For(i, c, n, f(b)))
        case v @ Switch(c, cs)                        => f(Switch(c, cs.map(p => p._1 -> f(p._2))))
        case v @ Val(id, value, typeOf)               => f(Val(id, f(value), typeOf))
        case v @ Annotated(id, param, value)          => f(Annotated(id, param, f(value)))
        case v @ RawLiteral(_)                        => f(v)
        case v @ DataTypes.bool(_)                    => f(v)
        case v @ DataTypes.float(_)                   => f(v)
        case v @ DataTypes.int(_)                     => f(v)
        case v @ DataTypes.ident(_)                   => f(v)
        case v @ DataTypes.index(_, _)                => f(v)
        case v @ DataTypes.vec2(vs)                   => f(DataTypes.vec2(vs.map(f)))
        case v @ DataTypes.vec3(vs)                   => f(DataTypes.vec3(vs.map(f)))
        case v @ DataTypes.vec4(vs)                   => f(DataTypes.vec4(vs.map(f)))
        case v @ DataTypes.mat2(vs)                   => f(DataTypes.mat2(vs.map(f)))
        case v @ DataTypes.mat3(vs)                   => f(DataTypes.mat3(vs.map(f)))
        case v @ DataTypes.mat4(vs)                   => f(DataTypes.mat4(vs.map(f)))
        case v @ DataTypes.array(_, _)                => f(v)
        case v @ DataTypes.swizzle(_, _, _)           => f(v)

    def typeIdent: Option[ShaderAST.DataTypes.ident] =
      ast match
        case Empty()                    => None
        case Block(_)                   => None
        case UBO(_)                     => None
        case ShaderBlock(_, _, _, _, _) => None
        case Function(_, _, _, rt)      => rt.flatMap(_.typeIdent)
        case CallFunction(_, _, _, rt)  => rt.flatMap(_.typeIdent)
        case FunctionRef(_, rt)         => rt.flatMap(_.typeIdent)
        case Cast(_, as)                => Option(ShaderAST.DataTypes.ident(as))
        case Infix(_, _, _, rt)         => rt.flatMap(_.typeIdent)
        case Assign(_, _)               => None
        case If(_, _, _)                => None
        case While(_, _)                => None
        case For(_, _, _, _)            => None
        case Switch(_, _)               => None
        case Val(id, value, typeOf)     => typeOf.map(t => ShaderAST.DataTypes.ident(t))
        case Annotated(_, _, value)     => value.typeIdent
        case RawLiteral(_)              => None
        case n @ DataTypes.ident(_)     => Option(n)
        case DataTypes.index(_, _)      => None
        case DataTypes.bool(_)          => Option(ShaderAST.DataTypes.ident("bool"))
        case DataTypes.float(_)         => Option(ShaderAST.DataTypes.ident("float"))
        case DataTypes.int(_)           => Option(ShaderAST.DataTypes.ident("int"))
        case DataTypes.vec2(_)          => Option(ShaderAST.DataTypes.ident("vec2"))
        case DataTypes.vec3(_)          => Option(ShaderAST.DataTypes.ident("vec3"))
        case DataTypes.vec4(_)          => Option(ShaderAST.DataTypes.ident("vec4"))
        case DataTypes.mat2(_)          => Option(ShaderAST.DataTypes.ident("mat2"))
        case DataTypes.mat3(_)          => Option(ShaderAST.DataTypes.ident("mat3"))
        case DataTypes.mat4(_)          => Option(ShaderAST.DataTypes.ident("mat4"))
        case DataTypes.array(_, typeOf) => typeOf.map(t => ShaderAST.DataTypes.ident(t + "[]"))
        case DataTypes.swizzle(v, _, _) => v.typeIdent

    def headers: List[ShaderAST] =
      ast match
        case ShaderBlock(_, _, _, headers, _) =>
          headers

        case _ =>
          Nil

    def inType: Option[String] =
      ast match
        case ShaderBlock(in, _, _, _, _) =>
          in

        case _ =>
          None

    def outType: Option[String] =
      ast match
        case ShaderBlock(_, out, _, _, _) =>
          out

        case _ =>
          None
