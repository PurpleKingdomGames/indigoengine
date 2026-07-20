package indigo.core.locale

import indigoengine.shared.collections.Batch

/** A language, optionally narrowed by the country and script it is used in
  *
  * @param privateUse
  *   The BCP 47 private use subtag, e.g. "pirate" in "en_x_pirate". Reserved by the standard for locales of your own
  *   invention, so it can never collide with a real language, country or script
  */
final case class Locale(
    language: Language,
    country: Option[Country],
    script: Option[Script],
    privateUse: Option[String]
) derives CanEqual:

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

  /** This locale and the progressively less specific locales it can be served by, most specific first. A private use
    * locale exhausts its own subtags before degrading to the real locale it extends
    */
  def fallbacks: Batch[Locale] =
    val narrowing =
      Batch(
        Option(this),
        country.map(_ => copy(country = None)),
        Option.when(country.isDefined || script.isDefined)(Locale(language, None, None, privateUse))
      ).collect { case Some(v) => v }

    val real =
      if privateUse.isEmpty then Batch.empty
      else narrowing.map(_.copy(privateUse = None))

    (narrowing ++ real).distinct

  override def toString: String =
    val subtags =
      s"${language.code.toLowerCase}${script
          .map(s => s"_${s.code.toLowerCase.capitalize}")
          .getOrElse("")}${country.map(c => s"_${c.code.toUpperCase}").getOrElse("")}"

    privateUse.map(p => s"${subtags}_x_$p").getOrElse(subtags)

object Locale:

  /** A locale with no private use subtag */
  def apply(language: Language, country: Option[Country], script: Option[Script]): Locale =
    Locale(language, country, script, None)

  def apply(language: Language, privateSubTag: String): Locale =
    Locale(language, None, None, Option(privateSubTag))

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
    val subtags  = locale.split("_")
    val markerAt = subtags.indexWhere(_.equalsIgnoreCase("x"))

    val split = if markerAt >= 0 then subtags.take(markerAt) else subtags

    val privateUse =
      if markerAt >= 0 then Option(subtags.drop(markerAt + 1).mkString("_")).filter(_.nonEmpty)
      else None

    split.headOption
      .flatMap(l => Language.fromString(l))
      .flatMap { languageCode =>
        split.drop(1) match {
          case Array(script, country) =>
            val scriptCode  = Script.fromString(script)
            val countryCode = Country.fromString(country)
            if (scriptCode.isDefined && countryCode.isDefined)
              Option(Locale(languageCode, countryCode, scriptCode, privateUse))
            else
              None

          case Array(countryOrScript) =>
            Country
              .fromString(countryOrScript)
              .map(country => Locale(languageCode, Option(country), None, privateUse))
              .orElse(
                Script.fromString(countryOrScript).map(script => Locale(languageCode, None, Option(script), privateUse))
              )

          case Array() => Some(Locale(languageCode, None, None, privateUse))

          case _ => None
        }
      }
