package indigo.platform.locale
import indigo.core.locale.Locale
import indigoengine.sdl.facades.sdl.SDL.*
import indigoengine.shared.collections.Batch

import scala.scalanative.unsafe.*

final case class NativeLocaleService() extends LocaleService:
  private lazy val locales: Batch[Locale] = readTags.map(Locale.fromString).collect { case Some(v) => v }

  def preferred: Batch[Locale] = locales
  def current: Option[Locale]  = locales.headOption

  private def readTags: Batch[String] =
    val countPtr = stackalloc[CInt]()
    val locales  = SDL_GetPreferredLocales(countPtr)

    if locales == null then Batch.empty
    else
      val count = !countPtr

      val tags =
        Batch.fromIndexedSeq(
          (0 until count).map { i =>
            val loc      = locales(i)
            val language = if loc._1 == null then "" else fromCString(loc._1)
            val country  = if loc._2 == null then "" else fromCString(loc._2)

            if country.isEmpty then language else s"$language-$country"
          }
        )

      SDL_free(locales.asInstanceOf[Ptr[Byte]])
      tags.filterNot(_.isEmpty)
