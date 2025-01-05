package ultraviolet.datatypes

import scala.annotation.tailrec
import scala.quoted.*

sealed trait ShaderAST derives CanEqual
object ShaderAST:

  given ToExpr[ShaderAST] with {
    def apply(x: ShaderAST)(using Quotes): Expr[ShaderAST] =
      x match
        case v: Empty                => Expr(v)
        case v: Block                => Expr(v)
        case v: Neg                  => Expr(v)
        case v: Not                  => Expr(v)
        case v: UBO                  => Expr(v)
        case v: Struct               => Expr(v)
        case v: New                  => Expr(v)
        case v: ShaderBlock          => Expr(v)
        case v: Function             => Expr(v)
        case v: CallFunction         => Expr(v)
        case v: CallExternalFunction => Expr(v)
        case v: FunctionRef          => Expr(v)
        case v: Cast                 => Expr(v)
        case v: Infix                => Expr(v)
        case v: Assign               => Expr(v)
        case v: MultiStatements      => Expr(v)
        case v: If                   => Expr(v)
        case v: While                => Expr(v)
        case v: For                  => Expr(v)
        case v: Switch               => Expr(v)
        case v: DataTypes            => Expr(v)
        case v: Val                  => Expr(v)
        case v: Annotated            => Expr(v)
        case v: RawLiteral           => Expr(v)
        case v: Field                => Expr(v)
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

  final case class Neg(value: ShaderAST) extends ShaderAST
  object Neg:
    given ToExpr[Neg] with {
      def apply(x: Neg)(using Quotes): Expr[Neg] =
        '{ Neg(${ Expr(x.value) }) }
    }

  final case class Not(value: ShaderAST) extends ShaderAST
  object Not:
    given ToExpr[Not] with {
      def apply(x: Not)(using Quotes): Expr[Not] =
        '{ Not(${ Expr(x.value) }) }
    }

  final case class UBO(uboDef: UBODef) extends ShaderAST
  object UBO:
    given ToExpr[UBO] with {
      def apply(x: UBO)(using Quotes): Expr[UBO] =
        '{ UBO(${ Expr(x.uboDef) }) }
    }

  final case class Struct(name: String, members: List[ShaderAST]) extends ShaderAST
  object Struct:
    given ToExpr[Struct] with {
      def apply(x: Struct)(using Quotes): Expr[Struct] =
        '{ Struct(${ Expr(x.name) }, ${ Expr(x.members) }) }
    }

  final case class New(name: String, args: List[ShaderAST]) extends ShaderAST
  object New:
    given ToExpr[New] with {
      def apply(x: New)(using Quotes): Expr[New] =
        '{ New(${ Expr(x.name) }, ${ Expr(x.args) }) }
    }

  // Specifically handles our 'Shader' type
  final case class ShaderBlock(
      inType: Option[String],
      outType: Option[String],
      envVarName: Option[String],
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
            ${ Expr(x.statements) }
          )
        }
    }

  final case class Function(id: String, args: List[(ShaderAST, String)], body: ShaderAST, returnType: ShaderAST)
      extends ShaderAST
  object Function:
    given ToExpr[Function] with {
      def apply(x: Function)(using Quotes): Expr[Function] =
        '{ Function(${ Expr(x.id) }, ${ Expr(x.args) }, ${ Expr(x.body) }, ${ Expr(x.returnType) }) }
    }

  final case class CallFunction(
      id: String,
      args: List[ShaderAST],
      returnType: ShaderAST
  ) extends ShaderAST
  object CallFunction:
    given ToExpr[CallFunction] with {
      def apply(x: CallFunction)(using Quotes): Expr[CallFunction] =
        '{ CallFunction(${ Expr(x.id) }, ${ Expr(x.args) }, ${ Expr(x.returnType) }) }
    }

  final case class CallExternalFunction(
      id: String,
      args: List[ShaderAST],
      returnType: ShaderAST
  ) extends ShaderAST
  object CallExternalFunction:
    given ToExpr[CallExternalFunction] with {
      def apply(x: CallExternalFunction)(using Quotes): Expr[CallExternalFunction] =
        '{ CallExternalFunction(${ Expr(x.id) }, ${ Expr(x.args) }, ${ Expr(x.returnType) }) }
    }

  // Allows things high up the tree to gain a reference to created functions.
  final case class FunctionRef(id: String, arg: List[ShaderAST], returnType: ShaderAST) extends ShaderAST
  object FunctionRef:
    given ToExpr[FunctionRef] with {
      def apply(x: FunctionRef)(using Quotes): Expr[FunctionRef] =
        '{ FunctionRef(${ Expr(x.id) }, ${ Expr(x.arg) }, ${ Expr(x.returnType) }) }
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
      returnType: ShaderAST
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

  final case class Val(id: String, value: ShaderAST, typeOf: ShaderAST) extends ShaderAST
  object Val:
    given ToExpr[Val] with {
      def apply(x: Val)(using Quotes): Expr[Val] =
        '{ Val(${ Expr(x.id) }, ${ Expr(x.value) }, ${ Expr(x.typeOf) }) }
    }

  final case class MultiStatements(
      leading: ShaderAST,
      statements: List[ShaderAST]
  ) extends ShaderAST
  object MultiStatements:
    given ToExpr[MultiStatements] with {
      def apply(m: MultiStatements)(using Quotes): Expr[MultiStatements] =
        '{ MultiStatements(${ Expr(m.leading) }, ${ Expr(m.statements) }) }
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

  final case class Field(term: ShaderAST, field: ShaderAST) extends ShaderAST
  object Field:
    given ToExpr[Field] with {
      def apply(x: Field)(using Quotes): Expr[Field] =
        '{ Field(${ Expr(x.term) }, ${ Expr(x.field) }) }
    }

  enum DataTypes extends ShaderAST:
    case ident(id: String)
    case external(id: String)
    case index(id: String, at: ShaderAST)
    case externalIndex(id: String, at: ShaderAST)
    case bool(b: Boolean)
    case float(v: Float)
    case int(v: Int)
    case vec2(args: List[ShaderAST])
    case vec3(args: List[ShaderAST])
    case vec4(args: List[ShaderAST])
    case bvec2(args: List[ShaderAST])
    case bvec3(args: List[ShaderAST])
    case bvec4(args: List[ShaderAST])
    case ivec2(args: List[ShaderAST])
    case ivec3(args: List[ShaderAST])
    case ivec4(args: List[ShaderAST])
    case mat2(args: List[ShaderAST])
    case mat3(args: List[ShaderAST])
    case mat4(args: List[ShaderAST])
    case array(size: Int, args: List[ShaderAST], typeOf: ShaderAST)
    case swizzle(genType: ShaderAST, swizzle: String, returnType: ShaderAST)

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
          case v: DataTypes.ident         => Expr(v)
          case v: DataTypes.external      => Expr(v)
          case v: DataTypes.index         => Expr(v)
          case v: DataTypes.externalIndex => Expr(v)
          case v: DataTypes.bool          => Expr(v)
          case v: DataTypes.float         => Expr(v)
          case v: DataTypes.int           => Expr(v)
          case v: DataTypes.vec2          => Expr(v)
          case v: DataTypes.vec3          => Expr(v)
          case v: DataTypes.vec4          => Expr(v)
          case v: DataTypes.bvec2         => Expr(v)
          case v: DataTypes.bvec3         => Expr(v)
          case v: DataTypes.bvec4         => Expr(v)
          case v: DataTypes.ivec2         => Expr(v)
          case v: DataTypes.ivec3         => Expr(v)
          case v: DataTypes.ivec4         => Expr(v)
          case v: DataTypes.mat2          => Expr(v)
          case v: DataTypes.mat3          => Expr(v)
          case v: DataTypes.mat4          => Expr(v)
          case v: DataTypes.array         => Expr(v)
          case v: DataTypes.swizzle       => Expr(v)
    }
    given ToExpr[ident] with {
      def apply(x: ident)(using Quotes): Expr[ident] =
        '{ ident(${ Expr(x.id) }) }
    }
    given ToExpr[external] with {
      def apply(x: external)(using Quotes): Expr[external] =
        '{ external(${ Expr(x.id) }) }
    }
    given ToExpr[index] with {
      def apply(x: index)(using Quotes): Expr[index] =
        '{ index(${ Expr(x.id) }, ${ Expr(x.at) }) }
    }
    given ToExpr[externalIndex] with {
      def apply(x: externalIndex)(using Quotes): Expr[externalIndex] =
        '{ externalIndex(${ Expr(x.id) }, ${ Expr(x.at) }) }
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
    given ToExpr[bvec2] with {
      def apply(x: bvec2)(using Quotes): Expr[bvec2] =
        '{ bvec2(${ Expr(x.args) }) }
    }
    given ToExpr[bvec3] with {
      def apply(x: bvec3)(using Quotes): Expr[bvec3] =
        '{ bvec3(${ Expr(x.args) }) }
    }
    given ToExpr[bvec4] with {
      def apply(x: bvec4)(using Quotes): Expr[bvec4] =
        '{ bvec4(${ Expr(x.args) }) }
    }
    given ToExpr[ivec2] with {
      def apply(x: ivec2)(using Quotes): Expr[ivec2] =
        '{ ivec2(${ Expr(x.args) }) }
    }
    given ToExpr[ivec3] with {
      def apply(x: ivec3)(using Quotes): Expr[ivec3] =
        '{ ivec3(${ Expr(x.args) }) }
    }
    given ToExpr[ivec4] with {
      def apply(x: ivec4)(using Quotes): Expr[ivec4] =
        '{ ivec4(${ Expr(x.args) }) }
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
        '{ array(${ Expr(x.size) }, ${ Expr(x.args) }, ${ Expr(x.typeOf) }) }
    }
    given ToExpr[swizzle] with {
      def apply(x: swizzle)(using Quotes): Expr[swizzle] =
        '{ swizzle(${ Expr(x.genType) }, ${ Expr(x.swizzle) }, ${ Expr(x.returnType) }) }
    }

  val unknownType: ShaderAST.DataTypes.ident = ShaderAST.DataTypes.ident("void")

  extension (ast: ShaderAST)
    def isEmpty: Boolean =
      ast match
        case Empty() => true
        case _       => false

    def exists(p: ShaderAST): Boolean =
      find(_ == p).isDefined

    def find(p: ShaderAST => Boolean): Option[ShaderAST] =
      findAll(p).headOption

    def findAll(p: ShaderAST => Boolean): List[ShaderAST] =
      @tailrec
      def rec(remaining: List[ShaderAST], acc: List[ShaderAST]): List[ShaderAST] =
        remaining match
          case Nil => acc.reverse
          case x :: xs =>
            x match
              case v if p(v)                     => rec(xs, v :: acc)
              case Empty()                       => rec(xs, acc)
              case Block(s)                      => rec(s ++ xs, acc)
              case Neg(s)                        => rec(s :: xs, acc)
              case Not(s)                        => rec(s :: xs, acc)
              case UBO(_)                        => rec(xs, acc)
              case Struct(_, _)                  => rec(xs, acc)
              case New(_, _)                     => rec(xs, acc)
              case ShaderBlock(_, _, _, s)       => rec(s ++ xs, acc)
              case Function(_, _, body, _)       => rec(body :: xs, acc)
              case CallFunction(_, _, _)         => rec(xs, acc)
              case CallExternalFunction(_, _, _) => rec(xs, acc)
              case FunctionRef(_, _, _)          => rec(xs, acc)
              case Cast(v, _)                    => rec(v :: xs, acc)
              case Infix(_, l, r, _)             => rec(l :: r :: xs, acc)
              case Assign(l, r)                  => rec(l :: r :: xs, acc)
              case If(_, t, e)                   => rec(t :: (e.toList ++ xs), acc)
              case While(_, b)                   => rec(b :: xs, acc)
              case For(_, _, _, b)               => rec(b :: xs, acc)
              case Switch(_, cs)                 => rec(cs.map(_._2) ++ xs, acc)
              case Val(_, body, _)               => rec(body :: xs, acc)
              case MultiStatements(_, as)        => rec(as ++ xs, acc)
              case Annotated(_, _, body)         => rec(body :: xs, acc)
              case RawLiteral(_)                 => rec(xs, acc)
              case Field(t, _)                   => rec(t :: xs, acc)
              case v: DataTypes.ident            => rec(xs, acc)
              case v: DataTypes.external         => rec(xs, acc)
              case v: DataTypes.index            => rec(xs, acc)
              case v: DataTypes.externalIndex    => rec(xs, acc)
              case v: DataTypes.bool             => rec(xs, acc)
              case v: DataTypes.float            => rec(xs, acc)
              case v: DataTypes.int              => rec(xs, acc)
              case v: DataTypes.vec2             => rec(v.args ++ xs, acc)
              case v: DataTypes.vec3             => rec(v.args ++ xs, acc)
              case v: DataTypes.vec4             => rec(v.args ++ xs, acc)
              case v: DataTypes.bvec2            => rec(v.args ++ xs, acc)
              case v: DataTypes.bvec3            => rec(v.args ++ xs, acc)
              case v: DataTypes.bvec4            => rec(v.args ++ xs, acc)
              case v: DataTypes.ivec2            => rec(v.args ++ xs, acc)
              case v: DataTypes.ivec3            => rec(v.args ++ xs, acc)
              case v: DataTypes.ivec4            => rec(v.args ++ xs, acc)
              case v: DataTypes.mat2             => rec(v.args ++ xs, acc)
              case v: DataTypes.mat3             => rec(v.args ++ xs, acc)
              case v: DataTypes.mat4             => rec(v.args ++ xs, acc)
              case v: DataTypes.array            => rec(xs, acc)
              case v: DataTypes.swizzle          => rec(v.genType :: xs, acc)

      rec(List(ast), Nil)

    def prune: ShaderAST =
      def crush(statements: ShaderAST): ShaderAST =
        statements match
          case b: Block => b.copy(statements = b.statements.filterNot(_.isEmpty).map(crush))
          case other    => other

      crush(ast)

    def traverse(modify: PartialFunction[ShaderAST, ShaderAST]): ShaderAST =
      val f = modify.orElse(s => s)
      ast match
        case v @ Empty()                             => f(v)
        case Block(s)                                => f(Block(s.map(_.traverse(f))))
        case Neg(s)                                  => f(Neg(s.traverse(f)))
        case Not(s)                                  => f(Not(s.traverse(f)))
        case v @ UBO(_)                              => f(v)
        case Struct(name, members)                   => f(Struct(name, members.map(_.traverse(f))))
        case New(name, args)                         => f(New(name, args.map(_.traverse(f))))
        case ShaderBlock(in, out, n, s)              => f(ShaderBlock(in, out, n, s.map(_.traverse(f))))
        case Function(id, args, body, returnType)    => f(Function(id, args, body.traverse(f), returnType))
        case CallFunction(id, args, rt)              => f(CallFunction(id, args, rt))
        case CallExternalFunction(id, args, rt)      => f(CallFunction(id, args, rt))
        case FunctionRef(id, arg, rt)                => f(FunctionRef(id, arg, rt))
        case Cast(value, as)                         => f(Cast(value.traverse(f), as))
        case Infix(op, l, r, returnType)             => f(Infix(op, l.traverse(f), r.traverse(f), returnType))
        case Assign(l, r)                            => f(Assign(l.traverse(f), r.traverse(f)))
        case If(c, t, e)                             => f(If(c, t.traverse(f), e.map(_.traverse(f))))
        case While(c, b)                             => f(While(c, b.traverse(f)))
        case For(i, c, n, b)                         => f(For(i, c, n, b.traverse(f)))
        case Switch(c, cs)                           => f(Switch(c, cs.map(p => p._1 -> p._2.traverse(f))))
        case Val(id, value, typeOf)                  => f(Val(id, value.traverse(f), typeOf))
        case MultiStatements(typeOf, vs)             => f(MultiStatements(typeOf, vs.map(f)))
        case Annotated(id, param, value)             => f(Annotated(id, param, value.traverse(f)))
        case v @ RawLiteral(_)                       => f(v)
        case Field(t, n)                             => f(Field(t.traverse(f), n))
        case v @ DataTypes.bool(_)                   => f(v)
        case v @ DataTypes.float(_)                  => f(v)
        case v @ DataTypes.int(_)                    => f(v)
        case v @ DataTypes.ident(_)                  => f(v)
        case v @ DataTypes.external(_)               => f(v)
        case DataTypes.index(id, at)                 => f(DataTypes.index(id, at.traverse(f)))
        case DataTypes.externalIndex(id, at)         => f(DataTypes.externalIndex(id, at.traverse(f)))
        case DataTypes.vec2(vs)                      => f(DataTypes.vec2(vs.map(_.traverse(f))))
        case DataTypes.vec3(vs)                      => f(DataTypes.vec3(vs.map(_.traverse(f))))
        case DataTypes.vec4(vs)                      => f(DataTypes.vec4(vs.map(_.traverse(f))))
        case DataTypes.bvec2(vs)                     => f(DataTypes.bvec2(vs.map(_.traverse(f))))
        case DataTypes.bvec3(vs)                     => f(DataTypes.bvec3(vs.map(_.traverse(f))))
        case DataTypes.bvec4(vs)                     => f(DataTypes.bvec4(vs.map(_.traverse(f))))
        case DataTypes.ivec2(vs)                     => f(DataTypes.ivec2(vs.map(_.traverse(f))))
        case DataTypes.ivec3(vs)                     => f(DataTypes.ivec3(vs.map(_.traverse(f))))
        case DataTypes.ivec4(vs)                     => f(DataTypes.ivec4(vs.map(_.traverse(f))))
        case DataTypes.mat2(vs)                      => f(DataTypes.mat2(vs.map(_.traverse(f))))
        case DataTypes.mat3(vs)                      => f(DataTypes.mat3(vs.map(_.traverse(f))))
        case DataTypes.mat4(vs)                      => f(DataTypes.mat4(vs.map(_.traverse(f))))
        case DataTypes.array(s, vs, t)               => f(DataTypes.array(s, vs.map(_.traverse(f)), t))
        case DataTypes.swizzle(genType, swizzle, rt) => f(DataTypes.swizzle(genType.traverse(f), swizzle, rt))

    def typeIdent: ShaderAST.DataTypes.ident =
      ast match
        case Empty()                        => unknownType
        case Block(_)                       => unknownType
        case Neg(v)                         => v.typeIdent
        case Not(v)                         => v.typeIdent
        case UBO(_)                         => unknownType
        case Struct(name, _)                => ShaderAST.DataTypes.ident(name)
        case New(name, _)                   => ShaderAST.DataTypes.ident(name)
        case ShaderBlock(_, _, _, _)        => unknownType
        case Function(_, _, _, rt)          => rt.typeIdent
        case CallFunction(_, _, rt)         => rt.typeIdent
        case CallExternalFunction(_, _, rt) => rt.typeIdent
        case FunctionRef(_, _, rt)          => rt.typeIdent
        case Cast(_, as)                    => ShaderAST.DataTypes.ident(as)
        case Infix(_, _, _, rt)             => rt.typeIdent
        case Assign(_, _)                   => unknownType
        case If(_, _, _)                    => unknownType
        case While(_, _)                    => unknownType
        case For(_, _, _, _)                => unknownType
        case Switch(_, _)                   => unknownType
        case Val(id, value, typeOf)         => typeOf.typeIdent
        case MultiStatements(typeOf, _)     => typeOf.typeIdent
        case Annotated(_, _, value)         => value.typeIdent
        case RawLiteral(_)                  => unknownType
        case Field(t, n)                    => unknownType
        case n @ DataTypes.ident(_)         => n
        case n @ DataTypes.external(_)      => ShaderAST.DataTypes.ident(n.id)
        case DataTypes.index(_, _)          => unknownType
        case DataTypes.externalIndex(_, _)  => unknownType
        case DataTypes.bool(_)              => ShaderAST.DataTypes.ident("bool")
        case DataTypes.float(_)             => ShaderAST.DataTypes.ident("float")
        case DataTypes.int(_)               => ShaderAST.DataTypes.ident("int")
        case DataTypes.vec2(_)              => ShaderAST.DataTypes.ident("vec2")
        case DataTypes.vec3(_)              => ShaderAST.DataTypes.ident("vec3")
        case DataTypes.vec4(_)              => ShaderAST.DataTypes.ident("vec4")
        case DataTypes.bvec2(_)             => ShaderAST.DataTypes.ident("bvec2")
        case DataTypes.bvec3(_)             => ShaderAST.DataTypes.ident("bvec3")
        case DataTypes.bvec4(_)             => ShaderAST.DataTypes.ident("bvec4")
        case DataTypes.ivec2(_)             => ShaderAST.DataTypes.ident("ivec2")
        case DataTypes.ivec3(_)             => ShaderAST.DataTypes.ident("ivec3")
        case DataTypes.ivec4(_)             => ShaderAST.DataTypes.ident("ivec4")
        case DataTypes.mat2(_)              => ShaderAST.DataTypes.ident("mat2")
        case DataTypes.mat3(_)              => ShaderAST.DataTypes.ident("mat3")
        case DataTypes.mat4(_)              => ShaderAST.DataTypes.ident("mat4")
        case DataTypes.array(_, _, typeOf)  => ShaderAST.DataTypes.ident(typeOf.typeIdent.id + "[]")
        case DataTypes.swizzle(_, _, rt)    => rt.typeIdent

    def inType: Option[String] =
      ast match
        case ShaderBlock(in, _, _, _) =>
          in

        case _ =>
          None

    def outType: Option[String] =
      ast match
        case ShaderBlock(_, out, _, _) =>
          out

        case _ =>
          None
