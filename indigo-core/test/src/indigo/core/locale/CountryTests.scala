package indigo.core.locale

class CountryTests extends munit.FunSuite:

  test("looks up a country by its code") {
    assertEquals(Country.fromString("JP"), Some(Country.Japan))
    assertEquals(Country.fromString("GB"), Some(Country.UnitedKingdom))
  }

  test("looking up a country is case insensitive") {
    assertEquals(Country.fromString("jp"), Some(Country.Japan))
    assertEquals(Country.fromString("Gb"), Some(Country.UnitedKingdom))
  }

  test("every country is found by its own code") {
    assert(Country.values.forall(c => Country.fromString(c.code) == Some(c)))
  }

  test("does not look up a country by name, or by an unknown code") {
    assertEquals(Country.fromString("Japan"), None)
    assertEquals(Country.fromString("JPN"), None)
    assertEquals(Country.fromString("XX"), None)
    assertEquals(Country.fromString(""), None)
  }

  test("covers all 249 countries, with unique codes") {
    assertEquals(Country.values.length, 249)
    assertEquals(Country.values.map(_.code).distinct.length, 249)
  }

  test("honours government requested names over the customary form") {
    assertEquals(Country.CaboVerde.name, "Cabo Verde")
    assertEquals(Country.Myanmar.name, "Myanmar")
    assertEquals(Country.Turkiye.name, "Türkiye")
  }
  test("every country has a unique, non-empty name") {
    assert(Country.values.forall(_.name.nonEmpty))
    assertEquals(Country.values.map(_.name).distinct.length, 249)
  }
