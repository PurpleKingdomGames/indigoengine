package ultraviolet.datatypes

import scala.util.control.NoStackTrace

sealed abstract class ShaderError(val message: String) extends Exception(message) with NoStackTrace

object ShaderError:

  private def makeMsg(msg: String): String =
    "[ultraviolet] " + msg

  final case class PrintError(msg: String)             extends ShaderError(makeMsg(msg))
  final case class UBORead(msg: String)                extends ShaderError(makeMsg(msg))
  final case class UnexpectedConstruction(msg: String) extends ShaderError(makeMsg(msg))
  final case class Unsupported(msg: String)            extends ShaderError(makeMsg(msg))
  final case class Validation(msg: String)             extends ShaderError(makeMsg(msg))
  final case class Metadata(msg: String)               extends ShaderError(makeMsg(msg))
  final case class OnFileLoad(msg: String)             extends ShaderError(makeMsg(msg))
  final case class GLSLReservedWord(word: String)
      extends Exception(makeMsg(s"'$word' is a reserved word in GLSL, please choose a different name."))
      with NoStackTrace
