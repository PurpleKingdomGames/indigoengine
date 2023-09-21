import scala.language.postfixOps
import Misc._
import Dependencies._
import org.typelevel.scalacoptions.ScalacOptions

Global / onChangedBuildSource := ReloadOnSourceChanges

val scala3Version = "3.3.1"

ThisBuild / versionScheme                                  := Some("early-semver")
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"
ThisBuild / scalaVersion                                   := scala3Version

lazy val ultravioletVersion = "0.1.3-SNAPSHOT"

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version            := ultravioletVersion,
  crossScalaVersions := Seq(scala3Version),
  organization       := "io.indigoengine",
  libraryDependencies ++= Shared.munit.value,
  scalacOptions ++= Seq("-language:strictEquality"),
  scalafixOnCompile := true,
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
  autoAPIMappings   := true,
  logo              := name.value,
  Test / tpolecatExcludeOptions ++= Set(
    ScalacOptions.warnValueDiscard,
    ScalacOptions.warnUnusedImports,
    ScalacOptions.warnUnusedLocals
  )
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
    .settings(
      neverPublish,
      commonSettings,
      name        := "UltravioletProject",
      code        := codeTaskDefinition,
      usefulTasks := customTasksAliases,
      presentationSettings(version)
    )
    .aggregate(ultraviolet.js, ultraviolet.jvm, sandbox)

// Testing

lazy val sandbox =
  project
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .dependsOn(ultraviolet.js)
    .settings(
      neverPublish,
      commonSettings,
      name                  := "sandbox",
      showCursor            := true,
      title                 := "Sandbox",
      gameAssetsDirectory   := "assets",
      disableFrameRateLimit := false,
      electronInstall       := indigoplugin.ElectronInstall.Latest,
      libraryDependencies ++= Shared.indigo.value,
      libraryDependencies ++= Shared.indigoExtras.value,
      libraryDependencies ++= Shared.indigoJson.value
    )

// Shader
lazy val ultraviolet =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full)
    .in(file("ultraviolet"))
    .settings(
      name := "ultraviolet",
      commonSettings ++ publishSettings,
      Compile / sourceGenerators += shaderDSLGen.taskValue,
      Compile / sourceGenerators += shaderTypeOfArrayGen.taskValue
    )

def shaderDSLGen = Def.task {
  ShaderDSLGen.makeShaderDSL((Compile / sourceManaged).value)
}

def shaderTypeOfArrayGen = Def.task {
  ShaderTypeOfArrayGen.makeArrayInstances((Compile / sourceManaged).value)
}
