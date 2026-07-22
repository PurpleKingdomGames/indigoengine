package indigo.platform.locale

import indigo.core.locale.Locale
import indigoengine.shared.collections.Batch
import org.scalajs.dom

final case class BrowserLocaleService() extends LocaleService:
  lazy val preferred: Batch[Locale] =
    Batch.fromJSArray(
      dom.window.navigator.languages
        .map(Locale.fromString)
        .collect { case Some(v) => v }
        .distinct
    )

  lazy val current: Option[Locale] = preferred.headOption
