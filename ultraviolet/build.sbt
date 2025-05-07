import scala.language.postfixOps
import Misc._
import Dependencies._
import org.typelevel.scalacoptions.ScalacOptions

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / semanticdbEnabled    := true

val scala3Version = "3.6.4"

ThisBuild / versionScheme := Some("early-semver")
ThisBuild / scalaVersion  := scala3Version

lazy val ultravioletVersion = "0.6.0"

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version            := ultravioletVersion,
  crossScalaVersions := Seq(scala3Version),
  organization       := "io.indigoengine",
  libraryDependencies ++= Shared.munit.value,
  scalacOptions ++= Seq("-language:strictEquality"),
  // scalafixOnCompile := true, // Plays havoc with the sandbox, checked in CI.
  // semanticdbEnabled := true, // Plays havoc with the sandbox, checked in CI.
  autoAPIMappings := true,
  logo            := name.value,
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
    ),
    sonatypeCredentialHost := "oss.sonatype.org",
    sonatypeRepository     := "https://oss.sonatype.org/service/local"
  )
}

// Root
lazy val ultravioletProject =
  (project in file("."))
    .settings(
      neverPublish,
      commonSettings,
      name        := "UltravioletProject",
      usefulTasks := customTasksAliases,
      presentationSettings(version)
    )
    .aggregate(ultraviolet.js, ultraviolet.jvm)

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
