import scala.sys.process._
import scala.language.postfixOps

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val roguelike =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "roguelike",
      version := "0.0.1",
      scalaVersion := "3.0.0",
      organization := "roguelike",
      libraryDependencies ++= Seq(
        "org.scalameta" %%% "munit" % "0.7.26" % Test
      ),
      testFrameworks += new TestFramework("munit.Framework"),
      Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
      showCursor := true,
      title := "Indigo Roguelike!",
      gameAssetsDirectory := "assets",
      windowStartWidth := 550,
      windowStartHeight := 400,
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo-json-circe" % "0.8.3-SNAPSHOT",
        "io.indigoengine" %%% "indigo"            % "0.8.3-SNAPSHOT",
        "io.indigoengine" %%% "indigo-extras"     % "0.8.3-SNAPSHOT"
      )
      // scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) } // required for parcel, but will break indigoRun & indigoBuild
    )
    .settings(
      Compile / sourceGenerators += Def.task {
        TileCharGen
          .gen(
            "DfTiles", // Class/module name.
            "roguelike", // fully qualified package name
            (Compile / sourceManaged).value, // Managed sources (output) directory for the generated classes
            10, // Character width
            10 // Character height
          )
      }.taskValue
    )
    .settings(
      code := { "code ." ! }
    )

// To use indigoBuild or indigoRun, first comment out the line above that says: `scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }`
addCommandAlias("runGame", ";compile;fastOptJS;indigoRun")
addCommandAlias("buildGame", ";compile;fastOptJS;indigoBuild")

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")
