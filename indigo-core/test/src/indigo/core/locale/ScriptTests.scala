package indigo.core.locale

class ScriptTests extends munit.FunSuite:

  test("excludes the private use range") {
    assert(!Script.values.exists(s => s.code == "Qaaa" || s.code == "Qabx"))
  }

  test("knows which scripts are written right-to-left") {
    assert(Script.Arab.isRightToLeft)
    assert(Script.Hebr.isRightToLeft)
    assert(Script.Thaa.isRightToLeft)
    assert(Script.Nkoo.isRightToLeft)
    assert(Script.Syrc.isRightToLeft)

    assert(!Script.Latn.isRightToLeft)
    assert(!Script.Guru.isRightToLeft)
    assert(!Script.Cyrl.isRightToLeft)
    assert(!Script.Mong.isRightToLeft)
    assert(!Script.Xpeo.isRightToLeft)
  }

  test("looks up a script by its code") {
    assertEquals(Script.fromString("Latn"), Some(Script.Latn))
    assertEquals(Script.fromString("Hant"), Some(Script.Hant))
  }

  test("looking up a script is case insensitive") {
    assertEquals(Script.fromString("latn"), Some(Script.Latn))
    assertEquals(Script.fromString("HANT"), Some(Script.Hant))
  }

  test("every script is found by its own code") {
    assert(Script.values.forall(s => Script.fromString(s.code) == Some(s)))
  }

  test("does not look up a script by name, or by an unknown code") {
    assertEquals(Script.fromString("Latin"), None)
    assertEquals(Script.fromString("Qaaa"), None)
    assertEquals(Script.fromString(""), None)
  }
