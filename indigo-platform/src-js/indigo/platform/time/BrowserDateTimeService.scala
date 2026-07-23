package indigo.platform.time

import indigo.core.time.DateTime
import scalajs.js
import indigo.core.time.DateFormat
import indigo.core.time.TimeFormat

final case class BrowserDateTimeService() extends DateTimeService:
  private lazy val formats: (DateFormat, TimeFormat) =
    val frmt = new js.Date(2000, 11, 31, 13, 0, 0).toLocaleString()
    val dateFormat =
      if frmt.indexOf("2000") < frmt.indexOf("12") then DateFormat.YearMonthDay
      else if frmt.indexOf("12") < frmt.indexOf("31") then DateFormat.MonthDayYear
      else DateFormat.DayMonthYear

    val timeFormat = if (frmt.indexOf("13") > -1) TimeFormat.TwentyFourHour else TimeFormat.TwelveHour

    (dateFormat, timeFormat)

  def current: DateTime =
    // TODO: We should replace this with the new Temporal.Now when Scala.JS supports itM
    val dt               = new js.Date()
    val secondsEastOfUtc = -(dt.getTimezoneOffset() * 60).toInt

    DateTime(dt.getTime().toLong + secondsEastOfUtc * 1000L, secondsEastOfUtc)

  def dateformat: DateFormat = formats._1
  def timeformat: TimeFormat = formats._2
