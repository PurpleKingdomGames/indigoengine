package ultraviolet.datatypes

final case class ShaderPrinterConfig(headers: List[PrinterHeader]):
  def withHeaders(newHeaders: List[PrinterHeader]): ShaderPrinterConfig =
    this.copy(headers = newHeaders)
  def addHeaders(newHeaders: List[PrinterHeader]): ShaderPrinterConfig =
    this.copy(headers = headers ++ newHeaders)

object ShaderPrinterConfig:
  val noHeaders: ShaderPrinterConfig =
    ShaderPrinterConfig(Nil)

  val default: ShaderPrinterConfig =
    noHeaders

final case class PrinterHeader(header: String)
object PrinterHeader:
  val Version300ES: PrinterHeader          = PrinterHeader("#version 300 es")
  val PrecisionHighPFloat: PrinterHeader   = PrinterHeader("precision highp float;")
  val PrecisionMediumPFloat: PrinterHeader = PrinterHeader("precision mediump float;")
  val PrecisionLowPFloat: PrinterHeader    = PrinterHeader("precision lowp float;")
