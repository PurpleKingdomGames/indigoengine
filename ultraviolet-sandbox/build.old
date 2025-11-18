import scala.sys.process._
import scala.language.postfixOps

import sbtwelcome._
import indigoplugin._

Global / onChangedBuildSource := ReloadOnSourceChanges

Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }

lazy val gameOptions: IndigoOptions =
  IndigoOptions.defaults
    .withTitle("UV Sandbox")
    .withWindowSize(550, 400)
    .withBackgroundColor("black")
    .withAssetDirectory("assets")
    .excludeAssets {
      case p if p.endsWith(os.RelPath.rel / ".gitkeep") => true
      case _                                            => false
    }
    .useElectronExecutable("npx electron")

lazy val uvsandbox =
  (project in file("uvsandbox"))
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings( // Normal SBT settings
      name         := "uvsandbox",
      version      := "0.0.1",
      scalaVersion := "3.7.1",
      organization := "io.indigoengine",
      libraryDependencies ++= Seq(
        "org.scalameta" %%% "munit" % "1.2.1" % Test
      ),
      testFrameworks += new TestFramework("munit.Framework"),
      scalafixOnCompile := true,
      semanticdbEnabled := true,
      semanticdbVersion := scalafixSemanticdb.revision
    )
    .settings( // Indigo specific settings
      indigoOptions := gameOptions,
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo-json-circe" % "0.21.0",
        "io.indigoengine" %%% "indigo"            % "0.21.0",
        "io.indigoengine" %%% "indigo-extras"     % "0.21.0"
      ).map(
        _ excludeAll (
          ExclusionRule(organization = "io.indigoengine", name = "ultraviolet")
        )
      )
    )
    .dependsOn(ultraviolet)

lazy val sandbox =
  (project in file("."))
    .settings(
      logo := "UV Sandbox (v" + version.value.toString + ")",
      usefulTasks := Seq(
        UsefulTask("runGame", "Run the game").noAlias,
        UsefulTask("buildGame", "Build web version").noAlias,
        UsefulTask("runGameFull", "Run the fully optimised game").noAlias,
        UsefulTask(
          "buildGameFull",
          "Build the fully optimised web version"
        ).noAlias
      ),
      logoColor        := scala.Console.MAGENTA,
      aliasColor       := scala.Console.YELLOW,
      commandColor     := scala.Console.CYAN,
      descriptionColor := scala.Console.WHITE
    )
    .aggregate(uvsandbox)

lazy val ultraviolet = ProjectRef(file(".."), "ultravioletJS")

addCommandAlias(
  "buildGame",
  List(
    "uvsandbox/compile",
    "uvsandbox/fastLinkJS",
    "uvsandbox/indigoBuild"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "buildGameFull",
  List(
    "uvsandbox/compile",
    "uvsandbox/fullLinkJS",
    "uvsandbox/indigoBuildFull"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "runGame",
  List(
    "uvsandbox/compile",
    "uvsandbox/fastLinkJS",
    "uvsandbox/indigoRun"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "runGameFull",
  List(
    "uvsandbox/compile",
    "uvsandbox/fullLinkJS",
    "uvsandbox/indigoRunFull"
  ).mkString(";", ";", "")
)
