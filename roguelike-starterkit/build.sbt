import scala.sys.process._
import scala.language.postfixOps

Global / onChangedBuildSource                              := ReloadOnSourceChanges
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

val scala3Version = "3.0.2"

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version      := "0.0.1",
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
        "io.indigoengine" %%% "indigo"        % "0.9.3-SNAPSHOT",
        "io.indigoengine" %%% "indigo-extras" % "0.9.3-SNAPSHOT"
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
              "roguelike",
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
            "RoguelikeTiles",   // Class/module name.
            "roguelike", // fully qualified package name
            (Compile / sourceManaged).value, // Managed sources (output) directory for the generated classes
            Seq((10, 10)) // By default, will generate lots of square sizes.
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
        "io.indigoengine" %%% "indigo-json-circe" % "0.9.3-SNAPSHOT",
        "io.indigoengine" %%% "indigo"            % "0.9.3-SNAPSHOT",
        "io.indigoengine" %%% "indigo-extras"     % "0.9.3-SNAPSHOT"
      )
      // scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) } // required for parcel, but will break indigoRun & indigoBuild
    )
    .settings(
      publish      := {},
      publishLocal := {}
    )
    .dependsOn(roguelike)

lazy val roguelikeProject =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      code := { "code ." ! }
    )
    .aggregate(roguelike, demo)

// To use indigoBuild or indigoRun, first comment out the line above that says: `scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }`
addCommandAlias("runGame", ";demo/compile;demo/fastOptJS;demo/indigoRun")
addCommandAlias("buildGame", ";demo/compile;demo/fastOptJS;demo/indigoBuild")

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")
