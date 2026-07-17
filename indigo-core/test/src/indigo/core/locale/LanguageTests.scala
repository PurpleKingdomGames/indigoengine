package indigo.core.locale

class LanguageTests extends munit.FunSuite:
  test("looks up a language by its code") {
    assertEquals(Language.fromString("en"), Some(Language.English))
    assertEquals(Language.fromString("cy"), Some(Language.Welsh))
  }

  test("looking up a language is case insensitive") {
    assertEquals(Language.fromString("EN"), Some(Language.English))
    assertEquals(Language.fromString("Cy"), Some(Language.Welsh))
  }

  test("every language is found by its own code") {
    assert(Language.values.forall(l => Language.fromString(l.code) == Some(l)))
  }

  test("does not look up a language by name, or by an unknown code") {
    assertEquals(Language.fromString("English"), None)
    assertEquals(Language.fromString("eng"), None)
    assertEquals(Language.fromString("xx"), None)
    assertEquals(Language.fromString(""), None)
  }

  test("covers all 183 set 1 codes, uniquely") {
    assertEquals(Language.values.length, 183)
    assertEquals(Language.values.map(_.code).distinct.length, 183)
  }
