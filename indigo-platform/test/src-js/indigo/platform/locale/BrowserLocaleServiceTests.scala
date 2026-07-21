package indigo.platform.locale

import indigo.core.locale.Country
import indigo.core.locale.Language
import indigo.core.locale.Locale
import indigoengine.shared.collections.Batch

import scala.scalajs.js

class BrowserLocaleServiceTests extends munit.FunSuite:

  // Tests run under Node, which has no window. The service reads window.navigator.languages,
  // so we stand one up per test and build the service against it.
  private def serviceReporting(languages: String*): BrowserLocaleService =
    js.Dynamic.global.globalThis.updateDynamic("window")(
      js.Dynamic.literal(
        "navigator" -> js.Dynamic.literal(
          "languages" -> js.Array(languages*)
        )
      )
    )

    BrowserLocaleService()

  test("reads the browser's languages as locales, preserving preference order") {
    assertEquals(
      serviceReporting("en-GB", "fr-FR", "en").preferred,
      Batch(
        Locale(Language.English, Some(Country.UnitedKingdom), None),
        Locale(Language.French, Some(Country.France), None),
        Locale(Language.English, None, None)
      )
    )
  }

  test("reads a bare language tag") {
    assertEquals(
      serviceReporting("fr").preferred,
      Batch(Locale(Language.French, None, None))
    )
  }

  test("drops tags it cannot understand, keeping the rest") {
    assertEquals(
      serviceReporting("zz", "en-GB", "not-a-locale").preferred,
      Batch(Locale(Language.English, Some(Country.UnitedKingdom), None))
    )
  }

  test("the current locale is the most preferred one") {
    assertEquals(
      serviceReporting("fr-FR", "en-GB").current,
      Some(Locale(Language.French, Some(Country.France), None))
    )
  }

  test("no current locale when the browser reports none") {
    val service = serviceReporting()

    assertEquals(service.preferred, Batch.empty[Locale])
    assertEquals(service.current, None)
  }

  test("no current locale when nothing the browser reports can be understood") {
    val service = serviceReporting("zz")

    assertEquals(service.preferred, Batch.empty[Locale])
    assertEquals(service.current, None)
  }
