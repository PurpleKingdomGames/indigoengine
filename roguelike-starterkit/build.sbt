import scala.sys.process._
import scala.language.postfixOps

import sbtwelcome._

Global / onChangedBuildSource                              := ReloadOnSourceChanges
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

val scala3Version = "3.2.0"
val indigoVersion = "0.13.0"

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version      := "0.1.1-SNAPSHOT",
  scalaVersion := scala3Version,
  organization := "io.indigoengine",
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % "0.7.29" % Test
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  scalafixOnCompile := true,
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision
)

lazy val publishSettings = {
  import xerial.sbt.Sonatype._
  Seq(
    publishTo           := sonatypePublishToBundle.value,
    publishMavenStyle   := true,
    sonatypeProfileName := "io.indigoengine",
    licenses            := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    sonatypeProjectHosting := Some(
      GitHubHosting("PurpleKingdomGames", "roguelike-starterkit", "indigo@purplekingdomgames.com")
    ),
    developers := List(
      Developer(
        id = "davesmith00000",
        name = "David Smith",
        email = "indigo@purplekingdomgames.com",
        url = url("https://github.com/davesmith00000")
      )
    )
  )
}

lazy val roguelike =
  (project in file("roguelike-starterkit"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .settings(
      name := "roguelike-starterkit",
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo"        % indigoVersion,
        "io.indigoengine" %%% "indigo-extras" % indigoVersion
      )
    )
    .settings(
      Compile / sourceGenerators += Def.task {
        val cachedFun = FileFunction.cached(
          streams.value.cacheDirectory / "shaders"
        ) { (files: Set[File]) =>
          ShaderLibraryGen
            .makeShaderLibrary(
              "TerminalShaders",
              "io.indigoengine.roguelike.starterkit.tiles",
              files,
              (Compile / sourceManaged).value
            )
            .toSet
        }

        cachedFun(IO.listFiles((baseDirectory.value / "shaders")).toSet).toSeq
      }.taskValue
    )
    .settings(
      Compile / sourceGenerators += Def.task {
        TileCharGen
          .gen(
            "RoguelikeTiles",                             // Class/module name.
            "io.indigoengine.roguelike.starterkit.tiles", // fully qualified package name
            (Compile / sourceManaged).value // Managed sources (output) directory for the generated classes
          )
      }.taskValue
    )

lazy val demo =
  (project in file("demo"))
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(commonSettings: _*)
    .settings(
      name                := "roguelike-demo",
      showCursor          := true,
      title               := "Indigo Roguelike!",
      gameAssetsDirectory := "assets",
      windowStartWidth    := 550,
      windowStartHeight   := 400,
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo-json-circe" % indigoVersion,
        "io.indigoengine" %%% "indigo"            % indigoVersion,
        "io.indigoengine" %%% "indigo-extras"     % indigoVersion
      )
      // scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) } // required for parcel, but will break indigoRun & indigoBuild
    )
    .settings(
      publish / skip      := true,
      publishLocal / skip := true
    )
    .dependsOn(roguelike)

lazy val roguelikeStarterKit =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      code := { "code ." ! }
    )
    .settings(
      publish / skip      := true,
      publishLocal / skip := true
    )
    .aggregate(roguelike, demo)
    .settings(
      logo := rawLogo + "(v" + version.value.toString + ")",
      usefulTasks := Seq(
        UsefulTask("r", "runGame", "Run the game (requires Electron)"),
        UsefulTask("p", "publishLocal", "Local publish"),
        UsefulTask("c", "code", "Launch VSCode")
      ),
      logoColor        := scala.Console.YELLOW,
      aliasColor       := scala.Console.BLUE,
      commandColor     := scala.Console.CYAN,
      descriptionColor := scala.Console.WHITE
    )

// To use indigoBuild or indigoRun, first comment out the line above that says: `scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }`
addCommandAlias("runGame", ";demo/compile;demo/fastOptJS;demo/indigoRun")
addCommandAlias("buildGame", ";demo/compile;demo/fastOptJS;demo/indigoBuild")

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")

// format: off
lazy val rawLogo: String =
"""
                          ___ __               
  _______  ___ ___ _____ / (_) /_____          
 / __/ _ \/ _ `/ // / -_) / /  '_/ -_)         
/_/  \___/\_, /\_,_/\__/_/_/_/\_\\__/_    _ __ 
  ___ / //___/_____/ /____ ________/ /__ (_) /_
 (_-</ __/ _ `/ __/ __/ -_) __/___/  '_// / __/
/___/\__/\_,_/_/  \__/\__/_/     /_/\_\/_/\__/ 
                                               
"""
