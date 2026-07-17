package indigo.core.locale

import indigoengine.shared.collections.Batch

class LocaleTests extends munit.FunSuite:

  test("falls back by dropping one subtag at a time, most specific first") {
    assertEquals(
      Locale(Language.Chinese, Some(Country.Taiwan), Some(Script.Hant)).fallbacks,
      Batch(
        Locale(Language.Chinese, Some(Country.Taiwan), Some(Script.Hant)),
        Locale(Language.Chinese, None, Some(Script.Hant)),
        Locale(Language.Chinese, None, None)
      )
    )
  }

  test("falls back through the script before the bare language") {
    assertEquals(
      Locale(Language.Chinese, None, Some(Script.Hant)).fallbacks,
      Batch(
        Locale(Language.Chinese, None, Some(Script.Hant)),
        Locale(Language.Chinese, None, None)
      )
    )
  }

  test("falls back from a country straight to the bare language") {
    assertEquals(
      Locale(Language.English, Some(Country.UnitedKingdom), None).fallbacks,
      Batch(
        Locale(Language.English, Some(Country.UnitedKingdom), None),
        Locale(Language.English, None, None)
      )
    )
  }

  test("a bare language falls back only to itself") {
    assertEquals(
      Locale(Language.English, None, None).fallbacks,
      Batch(Locale(Language.English, None, None))
    )
  }

  test("negotiates an exact match when there is one") {
    assertEquals(
      Locale.negotiate(
        Batch(Locale(Language.French, None, None)),
        Batch(Locale(Language.English, None, None), Locale(Language.French, None, None))
      ),
      Some(Locale(Language.French, None, None))
    )
  }

  test("negotiates down to a less specific available locale") {
    assertEquals(
      Locale.negotiate(
        Batch(Locale(Language.Chinese, Some(Country.Taiwan), Some(Script.Hant))),
        Batch(Locale(Language.Chinese, None, Some(Script.Hant)), Locale(Language.English, None, None))
      ),
      Some(Locale(Language.Chinese, None, Some(Script.Hant)))
    )
  }

  test("exhausts a preferred language's fallbacks before trying the next preference") {
    assertEquals(
      Locale.negotiate(
        Batch(Locale(Language.French, Some(Country.Canada), None), Locale(Language.English, None, None)),
        Batch(Locale(Language.English, None, None), Locale(Language.French, None, None))
      ),
      Some(Locale(Language.French, None, None))
    )
  }

  test("does not negotiate up to a more specific available locale") {
    assertEquals(
      Locale.negotiate(
        Batch(Locale(Language.Chinese, None, Some(Script.Hant))),
        Batch(Locale(Language.Chinese, Some(Country.Taiwan), Some(Script.Hant)))
      ),
      None
    )
  }

  test("negotiates nothing when no preference is available") {
    assertEquals(
      Locale.negotiate(
        Batch(Locale(Language.Japanese, None, None)),
        Batch(Locale(Language.English, None, None))
      ),
      None
    )

    assertEquals(Locale.negotiate(Batch.empty, Batch(Locale(Language.English, None, None))), None)
  }

  test("parses each combination of subtags") {
    assertEquals(Locale.fromString("en"), Some(Locale(Language.English, None, None)))
    assertEquals(Locale.fromString("en_GB"), Some(Locale(Language.English, Some(Country.UnitedKingdom), None)))
    assertEquals(Locale.fromString("zh_Hant"), Some(Locale(Language.Chinese, None, Some(Script.Hant))))
    assertEquals(
      Locale.fromString("zh_Hant_TW"),
      Some(Locale(Language.Chinese, Some(Country.Taiwan), Some(Script.Hant)))
    )
  }

  test("tells a lone script subtag apart from a lone country subtag") {
    assertEquals(Locale.fromString("zh_TW").flatMap(_.country), Some(Country.Taiwan))
    assertEquals(Locale.fromString("zh_TW").flatMap(_.script), None)

    assertEquals(Locale.fromString("zh_Hant").flatMap(_.script), Some(Script.Hant))
    assertEquals(Locale.fromString("zh_Hant").flatMap(_.country), None)
  }

  test("every locale survives a round trip through its rendered form") {
    val locales =
      Batch(
        Locale(Language.English, None, None),
        Locale(Language.English, Some(Country.UnitedKingdom), None),
        Locale(Language.Chinese, None, Some(Script.Hant)),
        Locale(Language.Chinese, Some(Country.Taiwan), Some(Script.Hant)),
        Locale(Language.Arabic, Some(Country.Egypt), Some(Script.Arab))
      )

    locales.foreach { locale =>
      assertEquals(Locale.fromString(locale.toString), Some(locale))
    }
  }

  test("parsing is case insensitive") {
    assertEquals(Locale.fromString("EN_gb"), Some(Locale(Language.English, Some(Country.UnitedKingdom), None)))
    assertEquals(Locale.fromString("ZH_HANT_tw"), Some(Locale(Language.Chinese, Some(Country.Taiwan), Some(Script.Hant))))
  }

  test("rejects unknown subtags") {
    assertEquals(Locale.fromString(""), None)
    assertEquals(Locale.fromString("xx"), None)
    assertEquals(Locale.fromString("en_XX"), None)
    assertEquals(Locale.fromString("en_Zzzz_XX"), None)
  }

  test("rejects subtags given out of order") {
    assertEquals(Locale.fromString("zh_TW_Hant"), None)
    assertEquals(Locale.fromString("GB_en"), None)
  }

  test("renders as language, then script, then country") {
    assertEquals(Locale(Language.English, None, None).toString, "en")
    assertEquals(Locale(Language.English, Some(Country.UnitedKingdom), None).toString, "en_GB")
    assertEquals(Locale(Language.Chinese, None, Some(Script.Hant)).toString, "zh_Hant")
    assertEquals(Locale(Language.Chinese, Some(Country.Taiwan), Some(Script.Hant)).toString, "zh_Hant_TW")
  }

  test("renders each subtag in its conventional case") {
    assertEquals(Locale(Language.Arabic, Some(Country.Egypt), Some(Script.Arab)).toString, "ar_Arab_EG")
    assertEquals(Locale(Language.SichuanYi, Some(Country.China), Some(Script.Yiii)).toString, "ii_Yiii_CN")
  }

  test("uses the script subtag to decide direction, when there is one") {
    assert(Locale(Language.Punjabi, None, Some(Script.Arab)).isRightToLeft)
    assert(Locale(Language.Kurdish, None, Some(Script.Arab)).isRightToLeft)

    assert(!Locale(Language.Azerbaijani, None, Some(Script.Latn)).isRightToLeft)
    assert(!Locale(Language.Punjabi, None, Some(Script.Guru)).isRightToLeft)
  }

  test("the script subtag wins over the language's default script") {
    assert(!Locale(Language.Arabic, None, Some(Script.Latn)).isRightToLeft)
    assert(Locale(Language.English, None, Some(Script.Hebr)).isRightToLeft)
  }

  test("falls back to the language's default script when there is no script subtag") {
    assert(Locale(Language.Arabic, None, None).isRightToLeft)
    assert(Locale(Language.Hebrew, None, None).isRightToLeft)
    assert(Locale(Language.Persian, None, None).isRightToLeft)
    assert(Locale(Language.Urdu, None, None).isRightToLeft)

    assert(!Locale(Language.English, None, None).isRightToLeft)
    assert(!Locale(Language.Punjabi, None, None).isRightToLeft)
    assert(!Locale(Language.Kurdish, None, None).isRightToLeft)
  }

  test("the country subtag does not affect direction") {
    assert(Locale(Language.Arabic, Some(Country.Egypt), None).isRightToLeft)
    assert(!Locale(Language.English, Some(Country.UnitedKingdom), None).isRightToLeft)
  }
