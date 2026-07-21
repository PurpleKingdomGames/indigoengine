package indigo.platform.locale

import indigo.core.locale.Locale

class NativeLocaleServiceTests extends munit.FunSuite:

  // SDL reports whatever the host machine is set to, so these cover the behaviour that must
  // hold for any environment, including one where SDL can tell us nothing.

  test("reads the host's locales without crashing") {
    val service = NativeLocaleService()

    service.preferred.foreach { locale =>
      assertEquals(Locale.fromString(locale.toString), Some(locale))
    }
  }

  test("the current locale is the most preferred one") {
    val service = NativeLocaleService()

    assertEquals(service.current, service.preferred.headOption)
  }

  test("there is no current locale when nothing was reported") {
    val service = NativeLocaleService()

    if service.preferred.isEmpty then assertEquals(service.current, None)
    else assert(service.current.isDefined)
  }

  test("reading repeatedly gives a stable answer") {
    val service = NativeLocaleService()

    assertEquals(service.preferred, service.preferred)
    assertEquals(NativeLocaleService().preferred, service.preferred)
  }
