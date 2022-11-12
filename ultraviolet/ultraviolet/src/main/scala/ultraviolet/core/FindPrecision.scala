package ultraviolet.core

import scala.quoted.*

object FindPrecision:

  inline def findPrecision[A]: Option[String] = ${ findPrecisionImpl[A] }

  def findPrecisionImpl[A: Type](using Quotes): Expr[Option[String]] = {
    import quotes.reflect.*
    val contents   = TypeRepr.of[A].show
    val precisions = List("highp", "mediump", "lowp")
    val res        = precisions.find(p => contents.contains(p))
    Expr(res)
  }
