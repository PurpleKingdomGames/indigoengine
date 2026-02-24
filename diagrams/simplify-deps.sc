#!/usr/bin/env -S scala-cli shebang

//> using scala 3
//> using toolkit default

/*
Visualises the repos modules.
Takes the result of running ./mill visualise __.compile and simplifies it.

Remeber, the diagram is compilation order, not dependency order, so sometimes links my look a little unexpected.

Useage: scala-cli run simplify-deps.sc -- module-deps.dot > simplified.dot
 */

val dotFile = args(0)
val path =
  if dotFile.startsWith("/") then os.Path(dotFile)
  else os.pwd / os.RelPath(dotFile)

val lines = os.read.lines(path)

// Regex patterns for parsing dot file
val nodeRegex = """"([^"]+)"\s*\[""".r
val edgeRegex = """"([^"]+)"\s*->\s*"([^"]+)"""".r

// Extract all node names
val allNodes = lines.flatMap { line =>
  nodeRegex.findFirstMatchIn(line).map(_.group(1))
}.toSet

// Filter out test nodes (containing ".test.")
val nonTestNodes = allNodes.filterNot(_.contains(".test."))

// Categorize by platform suffix
val jsNodes     = nonTestNodes.filter(_.endsWith(".js.compile"))
val jvmNodes    = nonTestNodes.filter(_.endsWith(".jvm.compile"))
val nativeNodes = nonTestNodes.filter(_.endsWith(".native.compile"))
val plainNodes  = nonTestNodes.filter { n =>
  n.endsWith(".compile") &&
  !n.endsWith(".js.compile") &&
  !n.endsWith(".jvm.compile") &&
  !n.endsWith(".native.compile")
}

// Compute base names for each platform
val jsBaseNames     = jsNodes.map(_.stripSuffix(".js.compile"))
val jvmBaseNames    = jvmNodes.map(_.stripSuffix(".jvm.compile"))
val nativeBaseNames = nativeNodes.map(_.stripSuffix(".native.compile"))

// For each base name with platform variants, build the sorted platform list
val allPlatformBases = (jsBaseNames ++ jvmBaseNames ++ nativeBaseNames).toSet
val platformsForBase: Map[String, List[String]] =
  allPlatformBases.map { base =>
    val platforms = List(
      if jsBaseNames.contains(base)     then Some("js")     else None,
      if jvmBaseNames.contains(base)    then Some("jvm")    else None,
      if nativeBaseNames.contains(base) then Some("native") else None
    ).flatten
    base -> platforms
  }.toMap

def displayName(base: String): String =
  platformsForBase.get(base).fold(base)(ps => s"$base (${ps.mkString(", ")})")

// Build rename map: original name -> display name
val renameMap: Map[String, String] =
  val jsRenames     = jsNodes.map { n => n -> displayName(n.stripSuffix(".js.compile")) }
  val jvmRenames    = jvmNodes.map { n => n -> displayName(n.stripSuffix(".jvm.compile")) }
  val nativeRenames = nativeNodes.map { n => n -> displayName(n.stripSuffix(".native.compile")) }
  val plainRenames  = plainNodes.map { n => n -> n.stripSuffix(".compile") }
  (jsRenames ++ jvmRenames ++ nativeRenames ++ plainRenames).toMap

// Process edges: remove test refs, apply renames, dedup, drop self-loops
val edges = lines
  .flatMap { line =>
    edgeRegex.findFirstMatchIn(line).map(m => (m.group(1), m.group(2)))
  }
  .filterNot((a, b) => a.contains(".test.") || b.contains(".test."))
  .flatMap { (a, b) =>
    for
      na <- renameMap.get(a)
      nb <- renameMap.get(b)
      if na != nb
    yield (na, nb)
  }
  .distinct
  .sortBy((a, b) => (a, b))

// Sorted unique node names
val nodes = renameMap.values.toList.distinct.sorted

// Output simplified dot graph
println("""digraph "module-deps" {""")
println("""  graph ["rankdir"="LR"]""")
nodes.foreach { n =>
  println(s"""  "$n" ["style"="solid","shape"="box"]""")
}
edges.foreach { (a, b) =>
  println(s"""  "$a" -> "$b"""")
}
println("}")
