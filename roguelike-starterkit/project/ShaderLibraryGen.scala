import sbt._
import scala.sys.process._

object ShaderLibraryGen {

  val extensions: List[String] =
    List(".vert", ".frag")

  val fileFilter: String => Boolean =
    name => extensions.exists(e => name.endsWith(e))

  def extractDetails(remaining: Seq[String], name: String, file: File): Option[ShaderDetails] =
    remaining match {
      case Nil =>
        None

      case ext :: exts if name.endsWith(ext) =>
        Some(ShaderDetails(name.substring(0, name.indexOf(ext)).capitalize, name, ext, IO.read(file)))

      case _ :: exts =>
        extractDetails(exts, name, file)
    }

  val tripleQuotes: String = "\"\"\""

  def template(moduleName: String, fullyQualifiedPath: String, contents: String): String =
    s"""package $fullyQualifiedPath
    |
    |object $moduleName {
    |
    |$contents
    |
    |}
    """.stripMargin

  def extractShaderCode(text: String, tag: String, assetName: String, newName: String): Seq[ShaderSnippet] =
    s"""//<indigo-$tag>\n((.|\n|\r)*)//</indigo-$tag>""".r
      .findAllIn(text)
      .toSeq
      .map(_.toString)
      .map(_.split('\n').drop(1).dropRight(1).mkString("\n"))
      .map { program =>
        val lines =
          program.split('\n')
            .toList
            .map { ln =>
              if(ln.startsWith("#define")) {
                val arg = ln.split(" ")(1)
                ShaderLine.Define(arg, arg.toLowerCase)
              } else ShaderLine.Normal(ln)
            }

        ShaderSnippet(newName + tag.split("-").map(_.capitalize).mkString, lines)
      }

  def makeShaderLibrary(moduleName: String, fullyQualifiedPath: String, files: Set[File], sourceManagedDir: File): Seq[File] = {
    println("Generating Indigo RawShaderCode Library...")

    val shaderFiles: Seq[File] =
      files.filter(f => fileFilter(f.name)).toSeq

    val glslValidatorExitCode = "glslangValidator -v" !

    println("***************")
    println("GLSL Validation")
    println("***************")

    if (glslValidatorExitCode == 0)
      shaderFiles.foreach { f =>
        val exit = ("glslangValidator " + f.getCanonicalPath) !

        if (exit != 0)
          throw new Exception("GLSL Validation Error in: " + f.getName)
        else
          println(f.getName + " [valid]")
      }
    else
      println("**WARNING**: GLSL Validator not installed, shader code not checked.")

    val shaderDetails: Seq[ShaderDetails] =
      shaderFiles
        .map(f => extractDetails(extensions, f.name, f))
        .collect { case Some(s) => s }

    val contents: String =
      shaderDetails
        .flatMap { d =>
          extractShaderCode(d.shaderCode, "vertex", d.originalName + d.ext, d.newName) ++
            extractShaderCode(d.shaderCode, "fragment", d.originalName + d.ext, d.newName) ++
            extractShaderCode(d.shaderCode, "prepare", d.originalName + d.ext, d.newName) ++
            extractShaderCode(d.shaderCode, "light", d.originalName + d.ext, d.newName) ++
            extractShaderCode(d.shaderCode, "composite", d.originalName + d.ext, d.newName)
        }
        .map { snippet =>
          if(snippet.containsDefineStatements) {
            val defines: List[ShaderLine.Define] =
              snippet.lines.collect {
                case d: ShaderLine.Define => d
              }

            def program: String =
              defines.map(d => s"#define ${d.originalName} $${${d.argName}}").mkString("\n") + "\n" +
              snippet.lines.flatMap {
                case ShaderLine.Normal(c) => List(c)
                case _ => Nil
              }.mkString("\n")
  
            val args: String = defines.map(d => d.argName + ": String").mkString(", ")

            s"""  def ${snippet.variableName}(${args}): String =
               |    s${tripleQuotes}${program}${tripleQuotes}
               |
            """.stripMargin
          } else {
            val program: String = snippet.lines.flatMap {
              case ShaderLine.Normal(c) => List(c)
              case _ => Nil
            }.mkString("\n")
  
            s"""  val ${snippet.variableName}: String =
               |    ${tripleQuotes}${program}${tripleQuotes}
               |
            """.stripMargin
          }
        }
        .mkString("\n")

    val file: File =
      sourceManagedDir / (moduleName + ".scala")

    val newContents: String =
      template(moduleName, fullyQualifiedPath, contents)

    IO.write(file, newContents)

    println("Written: " + file.getCanonicalPath)

    Seq(file)
  }

  case class ShaderDetails(newName: String, originalName: String, ext: String, shaderCode: String)
  case class ShaderSnippet(variableName: String, lines: List[ShaderLine]) {
    val containsDefineStatements: Boolean =
      lines.collect { case l: ShaderLine.Define => l}.nonEmpty
  }
}

sealed trait ShaderLine extends Product with Serializable
object ShaderLine {
  final case class Normal(code: String) extends ShaderLine
  final case class Define(originalName: String, argName: String) extends ShaderLine
}
