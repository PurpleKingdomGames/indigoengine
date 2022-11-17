package ultraviolet.datatypes

trait ShaderTemplate:
  def print(headers: List[String], functions: List[String], body: List[String]): String

object ShaderTemplate:

  given ShaderTemplate with
    def print(headers: List[String], functions: List[String], body: List[String]): String =
      (headers ++ functions ++ body).mkString("\n").trim
