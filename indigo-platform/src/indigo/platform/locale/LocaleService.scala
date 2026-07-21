package indigo.platform.locale

import indigo.core.locale.Locale
import indigoengine.shared.collections.Batch

trait LocaleService:
  def current: Option[Locale]
  def preferred: Batch[Locale]
