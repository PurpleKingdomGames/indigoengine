// TODO: This is overkill at this point. Should be able to simplify this.

lazy val releaseProjects: List[String] =
  List(
    "ultravioletJS",
    "ultravioletJVM"
  )

lazy val coreProjects: List[String] =
  releaseProjects

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
  applyCrossCommand(releaseProjects, "publishLocal")
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
  "crossTestUltravioletOnlyNoClean",
  applyCrossCommand(releaseProjects, "test")
)

addCommandAlias(
  "crossCleanAll",
  applyCrossToAll("clean")
)

// Release

addCommandAlias(
  "crossUltravioletRelease",
  List(
    "crossCleanReleaseable",
    "crossUpdateReleaseable",
    "crossBuildReleaseable",
    "crossUltravioletPublishAllSigned",
    "sonatypeBundleRelease"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "crossCleanReleaseable",
  applyCrossToAllReleaseable("clean")
)
addCommandAlias(
  "crossUpdateReleaseable",
  applyCrossToAllReleaseable("update")
)
addCommandAlias(
  "crossBuildReleaseable",
  applyCrossToAllReleaseable("compile")
)
addCommandAlias(
  "crossUltravioletPublishAllSigned",
  applyCrossToAllReleaseable("publishSigned")
)
