import scala.language.postfixOps
import Misc._
import Dependencies._

Global / onChangedBuildSource := ReloadOnSourceChanges

val scala3Version = "3.2.1"

ThisBuild / versionScheme                                  := Some("early-semver")
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
ThisBuild / scalaVersion                                   := scala3Version

lazy val ultravioletVersion = "0.1.0-SNAPSHOT"

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version            := ultravioletVersion,
  crossScalaVersions := Seq(scala3Version),
  organization       := "io.indigoengine",
  libraryDependencies ++= Shared.munit.value,
  Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  scalacOptions ++= Seq("-language:strictEquality"),
  scalafixOnCompile := true,
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
  autoAPIMappings   := true,
  logo              := name.value
)

lazy val neverPublish = Seq(
  publish / skip      := true,
  publishLocal / skip := true
)

lazy val publishSettings = {
  import xerial.sbt.Sonatype._
  Seq(
    publishTo              := sonatypePublishToBundle.value,
    publishMavenStyle      := true,
    sonatypeProfileName    := "io.indigoengine",
    licenses               := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    sonatypeProjectHosting := Some(GitHubHosting("PurpleKingdomGames", "ultraviolet", "indigo@purplekingdomgames.com")),
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

// Root
lazy val ultravioletProject =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin)
    .settings(
      neverPublish,
      commonSettings,
      name        := "UltravioletProject",
      code        := codeTaskDefinition,
      usefulTasks := customTasksAliases,
      presentationSettings(version)
    )
    .aggregate(ultraviolet, sandbox)

// Testing

lazy val sandbox =
  project
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .dependsOn(ultraviolet)
    .settings(
      neverPublish,
      commonSettings,
      name                := "sandbox",
      showCursor          := true,
      title               := "Sandbox",
      gameAssetsDirectory := "assets",
      disableFrameRateLimit := (sys.props("os.name").toLowerCase match {
        case x if x contains "windows" => false
        case _                         => true
      }),
      electronInstall := (sys.props("os.name").toLowerCase match {
        case x if x.contains("windows") || x.contains("linux") =>
          indigoplugin.ElectronInstall.Version("^18.0.0")

        case _ =>
          indigoplugin.ElectronInstall.Global
      }),
      libraryDependencies ++= Shared.indigo.value,
      libraryDependencies ++= Shared.indigoExtras.value,
      libraryDependencies ++= Shared.indigoJson.value
    )

// Shader
lazy val ultraviolet =
  project
    .in(file("ultraviolet"))
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "ultraviolet",
      commonSettings ++ publishSettings,
      Compile / sourceGenerators += shaderDSLGen.taskValue
    )

def shaderDSLGen = Def.task {
  ShaderDSLGen.makeShaderDSL((Compile / sourceManaged).value)
}
