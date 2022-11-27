package ultraviolet.macros

import scala.quoted.*

object FindPrecision:

  inline def findPrecision[A]: Option[String] = ${ findPrecisionImpl[A] }

  def findPrecisionImpl[A: Type](using Quotes): Expr[Option[String]] =
    import quotes.reflect.*
    Expr(List("highp", "mediump", "lowp").find(p => TypeRepr.of[A].show.contains(p)))
