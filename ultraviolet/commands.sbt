lazy val releaseProjects: List[String] =
  List(
    "ultravioletJS",
    "ultravioletJVM"
  )

lazy val coreProjects: List[String] =
  releaseProjects ++ List(
    "sandbox"
  )

val allProjects = List("ultravioletProject") // the aggregate

def applyCommand(projects: List[String], command: String): String =
  projects.map(p => p + "/" + command).mkString(";", ";", "")

def applyCrossCommand(projects: List[String], command: String): String =
  projects.map(p => "+" + p + "/" + command).mkString(";", ";", "")

def applyToAll(command: String): String =
  List(
    applyCommand(allProjects, command)
  ).mkString

def applyCrossToAll(command: String): String =
  List(
    applyCrossCommand(allProjects, command)
  ).mkString

def applyToAllReleaseable(command: String): String =
  List(
    applyCommand(releaseProjects, command)
  ).mkString

def applyCrossToAllReleaseable(command: String): String =
  List(
    applyCrossCommand(releaseProjects, command)
  ).mkString

addCommandAlias(
  "cleanAll",
  applyToAll("clean")
)

addCommandAlias(
  "buildAllNoClean",
  applyToAll("compile")
)
addCommandAlias(
  "buildAll",
  List(
    "cleanAll",
    "buildAllNoClean"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "testUltraviolet",
  applyCommand(coreProjects, "test")
)
addCommandAlias(
  "testAllNoClean",
  List(
    "testUltraviolet"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "testAll",
  List(
    "cleanAll",
    "testAllNoClean"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "testCompileAllNoClean",
  applyToAll("test:compile")
)
addCommandAlias(
  "testCompileAll",
  List(
    "cleanAll",
    "testCompileAllNoClean"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "buildUltraviolet",
  applyCommand(coreProjects, "compile")
)
addCommandAlias(
  "localPublishUltraviolet",
  applyToAll("publishLocal")
)

addCommandAlias(
  "localPublish",
  List(
    "cleanAll",
    "buildUltraviolet",
    "localPublishUltraviolet"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "localPublishNoClean",
  List(
    "buildUltraviolet",
    "localPublishUltraviolet"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "sandboxBuild",
  List(
    "buildAllNoClean",
    "sandbox/fastOptJS",
    "sandbox/indigoBuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "sandboxBuildFull",
  List(
    "buildAllNoClean",
    "sandbox/fullOptJS",
    "sandbox/indigoBuildFull"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "sandboxRun",
  List(
    "buildAllNoClean",
    "sandbox/fastOptJS",
    "sandbox/indigoRun"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "sandboxRunFull",
  List(
    "buildAllNoClean",
    "sandbox/fullOptJS",
    "sandbox/indigoRunFull"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "ultravioletPublishAllSigned",
  applyToAllReleaseable("publishSigned")
)

addCommandAlias(
  "ultravioletRelease",
  List(
    "cleanAll",
    "buildAllNoClean",
    "testAllNoClean",
    "ultravioletPublishAllSigned",
    "sonatypeBundleRelease"
  ).mkString(";", ";", "")
)

// -- cross building --

addCommandAlias(
  "crossBuildUltraviolet",
  applyCrossCommand(allProjects, "compile")
)
addCommandAlias(
  "crossLocalPublishUltraviolet",
  applyCrossCommand(allProjects, "publishLocal")
)
addCommandAlias(
  "crossLocalPublishNoClean",
  List("crossLocalPublishUltraviolet").mkString(";", ";", "")
)

addCommandAlias(
  "crossTestUltraviolet",
  applyCrossCommand(allProjects, "test")
)
addCommandAlias(
  "crossTestAllNoClean",
  List(
    "crossTestUltraviolet"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "crossCleanAll",
  applyCrossToAll("clean")
)

addCommandAlias(
  "crossUltravioletRelease",
  List(
    "crossCleanAll",
    "crossBuildUltraviolet",
    "crossUltravioletPublishAllSigned",
    "sonatypeBundleRelease"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "crossUltravioletPublishAllSigned",
  applyCrossToAllReleaseable("publishSigned")
)
