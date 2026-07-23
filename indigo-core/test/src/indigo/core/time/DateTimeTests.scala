package indigo.core.time

class DateTimeTests extends munit.FunSuite {

  test("DateTime should decompose the unix epoch") {
    val dt = DateTime(0L, 0)

    assertEquals((dt.year, dt.month, dt.day), (1970, 1, 1))
    assertEquals((dt.hour, dt.minute, dt.second), (0, 0, 0))
  }

  test("DateTime should decompose a time of day") {
    val dt = DateTime(1709214330L, 0) // 2024-02-29 13:45:30

    assertEquals((dt.year, dt.month, dt.day), (2024, 2, 29))
    assertEquals((dt.hour, dt.minute, dt.second), (13, 45, 30))
  }

  test("DateTime should handle the last second of a day") {
    val dt = DateTime(86399L, 0)

    assertEquals((dt.year, dt.month, dt.day), (1970, 1, 1))
    assertEquals((dt.hour, dt.minute, dt.second), (23, 59, 59))
  }

  test("DateTime should roll over into the next day") {
    val dt = DateTime(86400L, 0)

    assertEquals((dt.year, dt.month, dt.day), (1970, 1, 2))
    assertEquals((dt.hour, dt.minute, dt.second), (0, 0, 0))
  }

  test("DateTime should decompose dates before the epoch") {
    val dt = DateTime(-1L, 0)

    assertEquals((dt.year, dt.month, dt.day), (1969, 12, 31))
    assertEquals((dt.hour, dt.minute, dt.second), (23, 59, 59))
  }

  test("DateTime should decompose dates well before the epoch") {
    val dt = DateTime(-2208988800L, 0)

    assertEquals((dt.year, dt.month, dt.day), (1900, 1, 1))
    assertEquals((dt.hour, dt.minute, dt.second), (0, 0, 0))
  }

  // The 4/100/400 year leap rules are where naive implementations break

  test("DateTime should handle a leap day in a year divisible by 4") {
    val dt = DateTime(1709164800L, 0)

    assertEquals((dt.year, dt.month, dt.day), (2024, 2, 29))
  }

  test("DateTime should handle a leap day in a year divisible by 400") {
    val dt = DateTime(951782400L, 0)

    assertEquals((dt.year, dt.month, dt.day), (2000, 2, 29))
  }

  test("DateTime should treat a year divisible by 100 but not 400 as a common year") {
    val dt = DateTime(4107542400L, 0)

    assertEquals((dt.year, dt.month, dt.day), (2100, 3, 1))
  }

  test("DateTime should calculate the day of the week") {
    assertEquals(DateTime(0L, 0).dayOfWeek, 4)          // Thursday 1970-01-01
    assertEquals(DateTime(86400L, 0).dayOfWeek, 5)      // Friday
    assertEquals(DateTime(-1L, 0).dayOfWeek, 3)         // Wednesday 1969-12-31
    assertEquals(DateTime(951782400L, 0).dayOfWeek, 2)  // Tuesday 2000-02-29
    assertEquals(DateTime(4107542400L, 0).dayOfWeek, 1) // Monday 2100-03-01
  }

  test("DateTime fields describe local time, and ignore the offset") {
    val utc   = DateTime(0L, 0)
    val plus1 = DateTime(0L, 3600)

    assertEquals((plus1.year, plus1.month, plus1.day), (utc.year, utc.month, utc.day))
    assertEquals((plus1.hour, plus1.minute, plus1.second), (utc.hour, utc.minute, utc.second))
  }

  test("asUtc should subtract the offset to recover the instant") {
    val dt = DateTime(0L, 3600).asUtc

    assertEquals(dt.secondsEastOfUtc, 0)
    assertEquals((dt.year, dt.month, dt.day), (1969, 12, 31))
    assertEquals((dt.hour, dt.minute, dt.second), (23, 0, 0))
  }

  test("asUtc should handle a negative offset") {
    val dt = DateTime(0L, -3600).asUtc

    assertEquals(dt.secondsEastOfUtc, 0)
    assertEquals((dt.year, dt.month, dt.day), (1970, 1, 1))
    assertEquals((dt.hour, dt.minute, dt.second), (1, 0, 0))
  }

  test("asUtc should be idempotent") {
    val once  = DateTime(1709214330L, 3600).asUtc
    val twice = once.asUtc

    assertEquals(once, twice)
  }

  test("asUtc should be a no-op when the offset is already zero") {
    val dt = DateTime(1709214330L, 0)

    assertEquals(dt.asUtc, dt)
  }

  test("equality is field-wise, so the same instant at different offsets is not equal") {
    assert(DateTime(0L, 0) != DateTime(3600L, 3600))
    assertEquals(DateTime(0L, 0).asUtc, DateTime(3600L, 3600).asUtc)
  }

}
