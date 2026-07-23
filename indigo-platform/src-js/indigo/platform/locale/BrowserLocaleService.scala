package indigo.platform.locale

import indigo.core.locale.Locale
import indigoengine.shared.collections.Batch
import org.scalajs.dom

final case class BrowserLocaleService() extends LocaleService:
  private lazy val locales: Batch[Locale] =
    Batch.fromJSArray(
      dom.window.navigator.languages
        .map(Locale.fromString)
        .collect { case Some(v) => v }
        .distinct
    )

  def preferred: Batch[Locale] = locales

  def current: Option[Locale] = preferred.headOption
