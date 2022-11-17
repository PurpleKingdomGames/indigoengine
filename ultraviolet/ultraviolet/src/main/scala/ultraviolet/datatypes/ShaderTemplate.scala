package ultraviolet.datatypes

trait ShaderTemplate:
  def render(headers: String, functions: List[String], body: String): String

object ShaderTemplate:

  given ShaderTemplate with
    def render(headers: String, functions: List[String], body: String): String =
      (List(headers) ++ functions ++ List(body)).mkString("\n").trim
