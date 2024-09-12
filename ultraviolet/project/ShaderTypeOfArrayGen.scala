import sbt._
import scala.sys.process._

object ShaderTypeOfArrayGen {

  val tripleQuotes: String = "\"\"\""

  def template(
      name: String,
      contents: String
  ): String =
    s"""package ultraviolet.macros
    |
    |import ultraviolet.syntax.*
    |
    |trait ${name}:
    |
    |$contents
    |""".stripMargin

  def makeArrayInstances(sourceManagedDir: File): Seq[File] = {
    println("Generating Indigo ShaderTypeOf instances for array...")

    val name = "ShaderTypeOfArrayInstances"

    val file: File =
      sourceManagedDir / "ultraviolet" / "macros" / (name + ".scala")

    if (!file.exists()) {
      val newContents: String =
        template(name, makeContents())

      IO.write(file, newContents)

      println("Written: " + file.getCanonicalPath)
    } else {
      println("Found, skipping: " + file.getCanonicalPath)
    }

    Seq(file)
  }

  def makeContents(): String =
    (0 to 4096)
      .map { i =>
        s"""  given array$i[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[$i, A]] with
      |    def typeOf: String = s"$${sto.typeOf}[$i]"
      |"""
      }
      .mkString("\n")

}
