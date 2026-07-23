package indigo.platform.locale

import indigo.core.locale.Locale
import indigoengine.shared.collections.Batch

/** Deals with retrieving locales that a user has on their system
  */
trait LocaleService:
  /** The current active system locale. None if no locale has been specified
    *
    * @return
    */
  lazy val current: Option[Locale]

  /** A `Batch` of locales available to the game in user preference order (most preferred to least)
    *
    * @return
    */
  lazy val preferred: Batch[Locale]
