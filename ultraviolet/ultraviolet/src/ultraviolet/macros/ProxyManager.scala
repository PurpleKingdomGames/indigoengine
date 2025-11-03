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

  private val proxyLookUp: HashMap[String, Proxy] = new HashMap()

  def lookUp(name: String, fallback: Proxy): Proxy =
    proxyLookUp.get(name).getOrElse(fallback)
  def lookUp(name: String, fallback: String): Proxy =
    lookUp(name, Proxy(fallback))
  def lookUp(name: String): Proxy =
    lookUp(name, name)

  def add(originalName: String, newName: String, arg: List[ShaderAST], returnType: ShaderAST): Unit =
    proxyLookUp += originalName -> Proxy(newName, arg, returnType)
  def add(originalName: String, newName: String): Unit =
    add(originalName, newName, Nil, ShaderAST.unknownType)
  def add(name: String, proxy: Proxy): Unit =
    proxyLookUp += name -> proxy

  private val proxyInlineReplace: HashMap[String, ShaderAST] = new HashMap()

  def addInlineReplace(id: String, value: ShaderAST): Unit =
    proxyInlineReplace += id -> value
  def lookUpInlineReplace(id: String): Option[ShaderAST] =
    proxyInlineReplace.get(id)

final case class Proxy(name: String, argType: List[ShaderAST], returnType: ShaderAST)
object Proxy:
  def apply(name: String): Proxy = Proxy(name, Nil, ShaderAST.unknownType)
