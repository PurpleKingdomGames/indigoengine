package indigo.core.locale

import indigoengine.shared.collections.Batch

final case class Locale(language: Language, country: Option[Country], script: Option[Script]) derives CanEqual:

  /** Whether text in this locale runs right-to-left */
  def isRightToLeft: Boolean =
    script match
      case Some(s) => s.isRightToLeft
      case None =>
        language match
          case Language.Arabic | Language.Divehi | Language.Hebrew | Language.Kashmiri | Language.Pashto |
              Language.Persian | Language.Sindhi | Language.Uighur | Language.Urdu | Language.Yiddish =>
            true
          case _ => false

  /** This locale and the progressively less specific locales it can be served by, most specific first */
  def fallbacks: Batch[Locale] =
    Batch(
      Option(this),
      country.map(_ => copy(country = None)),
      Option.when(country.isDefined || script.isDefined)(Locale(language, None, None))
    ).collect { case Some(v) => v }.distinct

  override def toString: String =
    s"${language.code.toLowerCase}${script
        .map(s => s"_${s.code.toLowerCase.capitalize}")
        .getOrElse("")}${country.map(c => s"_${c.code.toUpperCase}").getOrElse("")}"

object Locale:

  /** Picks the locale to use from those a game provides, given the locales a player prefers, in preference order
    *
    * @param preferred
    *   The list of preferred locales, in order
    * @param available
    *   The locales available to the user
    *
    * @return
    */
  def negotiate(preferred: Batch[Locale], available: Batch[Locale]): Option[Locale] =
    preferred.flatMap(_.fallbacks).find(available.contains)

  /** Takes a string and attempts to convert it to a locale
    * @param locale
    * @return
    */
  def fromString(locale: String): Option[Locale] =
    val split = locale.split("_")
    split.headOption
      .flatMap(l => Language.fromString(l))
      .flatMap { languageCode =>
        split.drop(1) match {
          case Array(script, country) =>
            val scriptCode  = Script.fromString(script)
            val countryCode = Country.fromString(country)
            if (scriptCode.isDefined && countryCode.isDefined)
              Option(Locale(languageCode, countryCode, scriptCode))
            else
              None

          case Array(countryOrScript) =>
            Country
              .fromString(countryOrScript)
              .map(country => Locale(languageCode, Option(country), None))
              .orElse(Script.fromString(countryOrScript).map(script => Locale(languageCode, None, Option(script))))

          case Array() => Some(Locale(languageCode, None, None))

          case _ => None
        }
      }
