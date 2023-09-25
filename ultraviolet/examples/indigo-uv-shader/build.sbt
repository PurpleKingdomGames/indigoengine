import scala.sys.process._
import scala.language.postfixOps

import sbtwelcome._
import indigoplugin.IndigoOptions

Global / onChangedBuildSource := ReloadOnSourceChanges

Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

lazy val mygame =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings( // Normal SBT settings
      name         := "ultravioletexample",
      version      := "0.0.1",
      scalaVersion := "3.3.1",
      organization := "example",
      libraryDependencies ++= Seq(
        "org.scalameta" %%% "munit" % "0.7.29" % Test
      ),
      testFrameworks += new TestFramework("munit.Framework"),
      scalafixOnCompile := true,
      semanticdbEnabled := true,
      semanticdbVersion := scalafixSemanticdb.revision
    )
    .settings( // Indigo specific settings,
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Ultraviolet Example")
          .withAssetDirectory(os.RelPath.rel / "assets")
          .withWindowSize(400, 400)
          .withBackgroundColor("black"),
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo-json-circe" % "0.15.0",
        "io.indigoengine" %%% "indigo"            % "0.15.0",
        "io.indigoengine" %%% "indigo-extras"     % "0.15.0",
        "io.indigoengine" %%% "ultraviolet"       % "0.1.3"
      )
    )
    .settings(
      code := {
        val command = Seq("code", ".")
        val run = sys.props("os.name").toLowerCase match {
          case x if x contains "windows" => Seq("cmd", "/C") ++ command
          case _                         => command
        }
        run.!
      }
    )
    .settings(
      logo := "Ultraviolet Example (v" + version.value.toString + ")",
      usefulTasks := Seq(
        UsefulTask("runGame", "Run the game").noAlias,
        UsefulTask("buildGame", "Build web version").noAlias,
        UsefulTask("runGameFull", "Run the fully optimised game").noAlias,
        UsefulTask(
          "buildGameFull",
          "Build the fully optimised web version"
        ).noAlias,
        UsefulTask("code", "Launch VSCode").noAlias
      ),
      logoColor        := scala.Console.MAGENTA,
      aliasColor       := scala.Console.YELLOW,
      commandColor     := scala.Console.CYAN,
      descriptionColor := scala.Console.WHITE
    )

addCommandAlias("buildGame", ";compile;fastOptJS;indigoBuild")
addCommandAlias("buildGameFull", ";compile;fullOptJS;indigoBuildFull")
addCommandAlias("runGame", ";compile;fastOptJS;indigoRun")
addCommandAlias("runGameFull", ";compile;fullOptJS;indigoRunFull")

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")
