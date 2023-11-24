import scala.sys.process._
import scala.language.postfixOps

import sbtwelcome._
import indigoplugin._

val scala3Version              = "3.3.1"
val indigoVersion              = "0.15.2-SNAPSHOT"
val roguelikeStarterKitVersion = "0.3.3-SNAPSHOT"

Global / onChangedBuildSource                              := ReloadOnSourceChanges
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"
ThisBuild / scalaVersion                                   := scala3Version

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version      := roguelikeStarterKitVersion,
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
        TileCharGen
          .gen(
            "RoguelikeTiles",            // Class/module name.
            "roguelikestarterkit.tiles", // fully qualified package name
            (Compile / sourceManaged).value // Managed sources (output) directory for the generated classes
          )
      }.taskValue
    )

lazy val demoOptions: IndigoOptions =
  IndigoOptions.defaults
    .withTitle("Indigo Roguelike!")
    .withBackgroundColor("black")
    .withAssetDirectory("demo/assets")
    .withWindowSize(800, 600)

lazy val demo =
  (project in file("demo"))
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(commonSettings: _*)
    .settings(
      name          := "roguelike-demo",
      indigoOptions := demoOptions,
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
    .settings(
      Compile / sourceGenerators += Def.task {
        IndigoGenerators("demo")
          .listAssets("Assets", demoOptions.assets)
          .generateConfig("Config", demoOptions)
          .toSourceFiles((Compile / sourceManaged).value)
      }
    )
    .dependsOn(roguelike)

lazy val benchmarks =
  project
    .in(file("benchmarks"))
    .enablePlugins(ScalaJSPlugin, JSDependenciesPlugin)
    .dependsOn(roguelike)
    .settings(
      name         := "benchmarks",
      version      := roguelikeStarterKitVersion,
      organization := "io.indigoengine",
      Test / test  := {},
      libraryDependencies ++= Seq(
        "com.github.japgolly.scalajs-benchmark" %%% "benchmark" % "0.10.0"
      ),
      jsDependencies ++= Seq(
        "org.webjars" % "chartjs" % "1.0.2" / "Chart.js" minified "Chart.min.js"
      ),
      packageJSDependencies / skip := false
    )
    .settings(
      publish / skip      := true,
      publishLocal / skip := true
    )

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
    .aggregate(roguelike, demo, benchmarks)
    .settings(
      logo := rawLogo + "(v" + version.value.toString + ")",
      usefulTasks := Seq(
        UsefulTask("runGame", "Run the game").noAlias,
        UsefulTask("publishLocal", "Local publish").noAlias,
        UsefulTask("code", "Launch VSCode").noAlias
      ),
      logoColor        := scala.Console.YELLOW,
      aliasColor       := scala.Console.BLUE,
      commandColor     := scala.Console.CYAN,
      descriptionColor := scala.Console.WHITE
    )

// To use indigoBuild or indigoRun, first comment out the line above that says: `scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }`
addCommandAlias("runGame", ";demo/compile;demo/fastLinkJS;demo/indigoRun")
addCommandAlias("buildGame", ";demo/compile;demo/fastLinkJS;demo/indigoBuild")

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
