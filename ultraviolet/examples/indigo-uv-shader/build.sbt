import scala.sys.process._
import scala.language.postfixOps

import sbtwelcome._

Global / onChangedBuildSource := ReloadOnSourceChanges

Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

lazy val mygame =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings( // Normal SBT settings
      name         := "ultravioletexample",
      version      := "0.0.1",
      scalaVersion := "3.2.1",
      organization := "example",
      libraryDependencies ++= Seq(
        "org.scalameta" %%% "munit" % "0.7.29" % Test
      ),
      testFrameworks += new TestFramework("munit.Framework"),
      scalafixOnCompile := true,
      semanticdbEnabled := true,
      semanticdbVersion := scalafixSemanticdb.revision
    )
    .settings( // Indigo specific settings
      showCursor            := true,
      title                 := "Ultraviolet Example",
      gameAssetsDirectory   := "assets",
      windowStartWidth      := 400,
      windowStartHeight     := 400,
      disableFrameRateLimit := false,
      electronInstall       := indigoplugin.ElectronInstall.Latest,
      backgroundColor       := "black",
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo-json-circe" % "0.14.1-SNAPSHOT",
        "io.indigoengine" %%% "indigo"            % "0.14.1-SNAPSHOT",
        "io.indigoengine" %%% "indigo-extras"     % "0.14.1-SNAPSHOT",
        "io.indigoengine" %%% "ultraviolet"       % "0.1.0"
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
        UsefulTask("a", "runGame", "Run the game"),
        UsefulTask("b", "buildGame", "Build web version"),
        UsefulTask("c", "runGameFull", "Run the fully optimised game"),
        UsefulTask("d", "buildGameFull", "Build the fully optimised web version"),
        UsefulTask("e", "code", "Launch VSCode")
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
