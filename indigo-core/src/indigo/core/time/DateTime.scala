package indigo.core.time

/** Represents a date and time in a Gregorian calendar
  *
  * @param localSecondsSinceUnixEpoch
  *   The number of seconds since 1st Jan 1970 in the local timezone
  * @param secondsEastOfUtc
  *   The seconds east of UTC in the local timezone
  */
final case class DateTime(
    localSecondsSinceUnixEpoch: Long,
    secondsEastOfUtc: Int
) derives CanEqual:
  lazy val (year, month, day, hour, minute, second, dayOfWeek) = calculateDateParts
  lazy val epochDay                                            = Math.floorDiv(localSecondsSinceUnixEpoch, 86400L)

  val epochSeconds: Long = localSecondsSinceUnixEpoch - secondsEastOfUtc

  override def equals(that: Any): Boolean =
    that match
      case d: DateTime => epochSeconds == d.epochSeconds
      case _           => false

  override def hashCode: Int = epochSeconds.hashCode

  def asUtc: DateTime = DateTime(localSecondsSinceUnixEpoch - secondsEastOfUtc, 0)

  /** Uses Howard Hinnants algorithm for calculating dates using seconds since an epoch
    * https://howardhinnant.github.io/date_algorithms.html
    *
    * @return
    */
  private def calculateDateParts: (Int, Int, Int, Int, Int, Int, Int) =
    val secondsPerDay = 86400L
    val days          = Math.floorDiv(localSecondsSinceUnixEpoch, secondsPerDay) // days since 1970-01-01
    val secOfDay      = Math.floorMod(localSecondsSinceUnixEpoch, secondsPerDay)

    val z   = days + 719468                                         // shift epoch to 0000-03-01
    val era = (if z >= 0 then z else z - 146096) / 146097           // which 400-year block
    val doe = z - era * 146097                                      // day of era   [0, 146096]
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365 // year of era [0, 399]
    val y   = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)               // day of year from Mar 1
    val mp  = (5 * doy + 2) / 153                                   // month, Mar = 0 .. Feb = 11

    val day    = (doy - (153 * mp + 2) / 5 + 1).toInt // day of month [1, 31]
    val month  = (if mp < 10 then mp + 3 else mp - 9).toInt // remap to 1..12
    val year   = (if month <= 2 then y + 1 else y).toInt // Jan/Feb belong to next year
    val hour   = (secOfDay / 3600).toInt
    val minute = ((secOfDay % 3600) / 60).toInt
    val second = (secOfDay  % 60).toInt

    val dayOfWeek = Math.floorMod(days + 4, 7) // 0 = Sunday, 6 = Saturday

    (year, month, day, hour, minute, second, dayOfWeek)

object DateTime:
  given Ordering[DateTime] = Ordering.by(_.epochSeconds)
