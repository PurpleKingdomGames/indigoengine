package indigo.scenegraph.registers

import indigo.core.datatypes.FontInfo
import indigo.core.datatypes.FontKey
import indigo.core.utils.CacheKey
import indigo.core.utils.QuickCache

import scala.annotation.nowarn

final class FontRegister {

  implicit private val cache: QuickCache[FontInfo] = QuickCache.empty

  def kill(): Unit =
    clearRegister()

  @nowarn("msg=unused")
  def register(fontInfo: FontInfo): Unit = {
    QuickCache(fontInfo.fontKey.toString)(fontInfo)
    ()
  }

  def findByFontKey(fontKey: FontKey): Option[FontInfo] =
    cache.fetch(CacheKey(fontKey.toString))

  @nowarn("msg=unused")
  def clearRegister(): Unit = {
    cache.purgeAll()
    ()
  }

}
