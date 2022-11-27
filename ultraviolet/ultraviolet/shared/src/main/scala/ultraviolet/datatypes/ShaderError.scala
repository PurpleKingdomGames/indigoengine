package ultraviolet.datatypes

import scala.util.control.NoStackTrace

object ShaderError:

  private def makeMsg(msg: String): String =
    "[ultraviolet] " + msg

  final case class PrintError(msg: String)             extends Exception(makeMsg(msg)) with NoStackTrace
  final case class UBORead(msg: String)                extends Exception(makeMsg(msg)) with NoStackTrace
  final case class UnexpectedConstruction(msg: String) extends Exception(makeMsg(msg)) with NoStackTrace
  final case class Unsupported(msg: String)            extends Exception(makeMsg(msg)) with NoStackTrace
  final case class Validation(msg: String)             extends Exception(makeMsg(msg)) with NoStackTrace
  final case class Metadata(msg: String)               extends Exception(makeMsg(msg)) with NoStackTrace
