package indigo.platform.time

import indigo.core.time.DateFormat
import indigo.core.time.DateTime
import indigo.core.time.TimeFormat
import indigoengine.sdl.facades.sdl.SDL.*
import indigoengine.sdl.facades.sdl.SDLConstants.*

import scala.scalanative.unsafe.*

final case class NativeDateTimeService() extends DateTimeService:
  private lazy val formats: (DateFormat, TimeFormat) = readFormats

  def current: DateTime =
    val ticks = stackalloc[SDL_Time]()

    val nanosSinceEpoch =
      if SDL_GetCurrentTime(ticks) then !ticks else System.currentTimeMillis() * 1_000_000L

    val secondsEastOfUtc = readUtcOffset(nanosSinceEpoch)
    val epochMillis      = Math.floorDiv(nanosSinceEpoch, 1_000_000L)

    DateTime(epochMillis + secondsEastOfUtc * 1000L, secondsEastOfUtc)

  def dateformat: DateFormat = formats._1
  def timeformat: TimeFormat = formats._2

  private def readUtcOffset(nanosSinceEpoch: SDL_Time): Int =
    val parts = stackalloc[SDL_DateTime]()

    // The ninth field of SDL_DateTime is utc_offset, the seconds east of UTC.
    if SDL_TimeToDateTime(nanosSinceEpoch, parts, true) then parts._9 else 0

  private def readFormats: (DateFormat, TimeFormat) =
    val dateFormat = stackalloc[CInt]()
    val timeFormat = stackalloc[CInt]()

    if SDL_GetDateTimeLocalePreferences(dateFormat, timeFormat) then
      (dateFormatFrom(!dateFormat), timeFormatFrom(!timeFormat))
    else (DateFormat.YearMonthDay, TimeFormat.TwentyFourHour)

  private def dateFormatFrom(preference: CInt): DateFormat =
    preference match
      case SDL_DATE_FORMAT_DDMMYYYY => DateFormat.DayMonthYear
      case SDL_DATE_FORMAT_MMDDYYYY => DateFormat.MonthDayYear
      case _                        => DateFormat.YearMonthDay

  private def timeFormatFrom(preference: CInt): TimeFormat =
    preference match
      case SDL_TIME_FORMAT_12HR => TimeFormat.TwelveHour
      case _                    => TimeFormat.TwentyFourHour
