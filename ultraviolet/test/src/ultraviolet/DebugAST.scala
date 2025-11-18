package ultraviolet

import ultraviolet.datatypes.Shader

import scala.quoted.*

object DebugAST:

  inline def toAST[In, Out](inline expr: Shader[In, Out]): String = ${ toASTImpl('{ expr }) }

  private def toASTImpl[In, Out: Type](expr: Expr[Shader[In, Out]])(using Quotes): Expr[String] = {

    import quotes.reflect.*

    println(">>> AST (Shader[In, Out]):")
    println(Printer.TreeStructure.show(expr.asTerm))
    println("<<<")

    Expr("Done.")
  }

  inline def any[In, Out](inline expr: Any): String = ${ anyToASTImpl('{ expr }) }

  private def anyToASTImpl[In, Out: Type](expr: Expr[Any])(using Quotes): Expr[String] = {

    import quotes.reflect.*

    println(">>> AST (Any):")
    println(Printer.TreeStructure.show(expr.asTerm))
    println("<<<")

    Expr("Done.")
  }
