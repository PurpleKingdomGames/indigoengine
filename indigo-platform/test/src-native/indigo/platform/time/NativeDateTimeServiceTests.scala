package indigo.platform.time

class NativeDateTimeServiceTests extends munit.FunSuite:

  // The host decides the clock and the formats, so these cover what must hold anywhere.

  test("reads the host's clock") {
    val service = NativeDateTimeService()
    val before  = System.currentTimeMillis()
    val now     = service.current.asUtc.localMillisSinceUnixEpoch
    val after   = System.currentTimeMillis()

    assert(now >= before && now <= after, now)
  }

  test("the local time is the instant shifted by the offset") {
    val now = NativeDateTimeService().current

    assertEquals(
      now.localMillisSinceUnixEpoch - now.secondsEastOfUtc.toLong * 1000L,
      now.asUtc.localMillisSinceUnixEpoch
    )
  }

  test("the offset is a real world one") {
    val offset = NativeDateTimeService().current.secondsEastOfUtc

    assert(offset > -86400 && offset < 86400, offset)
  }

  test("reading the formats repeatedly gives a stable answer") {
    val service = NativeDateTimeService()

    assertEquals(service.dateformat, NativeDateTimeService().dateformat)
    assertEquals(service.timeformat, NativeDateTimeService().timeformat)
  }
