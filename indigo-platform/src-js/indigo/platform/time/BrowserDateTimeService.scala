package indigo.platform.time

import indigo.core.time.DateFormat
import indigo.core.time.DateTime
import indigo.core.time.TimeFormat
import indigoengine.shared.collections.Batch
import org.scalajs.dom

import scalajs.js

final case class BrowserDateTimeService() extends DateTimeService:
  private lazy val formats: (DateFormat, TimeFormat) =
    val options =
      new DateTimeFormatOptions:
        val year  = "numeric"
        val month = "numeric"
        val day   = "numeric"
        val hour  = "numeric"

    val formatter = BrowserDateTimeFormat(dom.window.navigator.languages, options)
    val parts = Batch.fromJSArray(
      formatter
        .formatToParts(new js.Date(2000, 11, 31, 13, 0, 0))
        .map(_.`type`)
        .filter {
          case "year" | "month" | "day" => true
          case _                        => false
        }
    )

    val dateFormat = parts match
      case Batch("year", "month", "day") => DateFormat.YearMonthDay
      case Batch("day", "month", "year") => DateFormat.DayMonthYear
      case Batch("month", "day", "year") => DateFormat.MonthDayYear
      case _                             => DateFormat.YearMonthDay

    val timeFormat = formatter.resolvedOptions().hourCycle.toOption.getOrElse("h24") match
      case "h11" | "h12" => TimeFormat.TwelveHour
      case "h23" | "h24" => TimeFormat.TwentyFourHour
      case _             => TimeFormat.TwentyFourHour

    (dateFormat, timeFormat)

  def current: DateTime =
    val zdt              = TemporalNow.zonedDateTimeISO()
    val secondsEastOfUtc = (zdt.offsetNanoseconds * 0.000000001).toInt

    DateTime(zdt.epochMilliseconds.toLong + secondsEastOfUtc * 1000L, secondsEastOfUtc)

  def dateformat: DateFormat = formats._1
  def timeformat: TimeFormat = formats._2
