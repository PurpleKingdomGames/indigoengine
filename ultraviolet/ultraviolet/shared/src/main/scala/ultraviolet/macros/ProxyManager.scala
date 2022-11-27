package ultraviolet.macros

import ultraviolet.datatypes.ShaderAST

import scala.collection.mutable.HashMap

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
final class ProxyManager:
  private var fnCount: Int = 0
  private def nextFnName: String =
    // using 'def' as it's a Scala keyword - hopefully will minimise user collisions
    val res = "def" + fnCount.toString
    fnCount = fnCount + 1
    res
  def makeDefName: String = nextFnName

  private var varCount: Int = 0
  private def nextVarName: String =
    // using 'val' as it's a Scala keyword - hopefully will minimise user collisions
    val res = "val" + varCount.toString
    varCount = varCount + 1
    res
  def makeVarName: String = nextVarName

  private val proxyLookUp: HashMap[String, (String, Option[ShaderAST])] = new HashMap()

  def lookUp(name: String, fallback: (String, Option[ShaderAST])): (String, Option[ShaderAST]) =
    proxyLookUp.get(name).getOrElse(fallback)
  def lookUp(name: String, fallback: String): (String, Option[ShaderAST]) =
    lookUp(name, fallback -> None)
  def lookUp(name: String): (String, Option[ShaderAST]) =
    lookUp(name, name)

  def add(originalName: String, newName: String, returnType: Option[ShaderAST]): Unit =
    proxyLookUp += originalName -> (newName -> returnType)
  def add(originalName: String, newName: String): Unit =
    add(originalName, newName, None)
