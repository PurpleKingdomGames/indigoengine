package indigo.core.time

/** Represents a date and time in a Gregorian calendar.
  *
  * The instant is stored as a single millisecond count so that identity is structural: two `DateTime`s are equal when
  * they describe the same local millis at the same offset. Compare instants across offsets with [[asUtc]] or
  * [[epochMillis]].
  *
  * @param localMillisSinceUnixEpoch
  *   The number of milliseconds since 1st Jan 1970 in the local timezone
  * @param secondsEastOfUtc
  *   The seconds east of UTC in the local timezone
  */
final case class DateTime(
    localMillisSinceUnixEpoch: Long,
    secondsEastOfUtc: Int
) derives CanEqual:
  lazy val (year, month, day, hour, minute, second, millisecond, dayOfWeek) = calculateDateParts

  lazy private val epochMillis: Long = localMillisSinceUnixEpoch - secondsEastOfUtc.toLong * 1000L

  /** Get the date as it would be in UTC
    *
    * @return
    */
  def asUtc: DateTime = DateTime(epochMillis, 0)

  /** Uses Howard Hinnants algorithm for calculating dates using milliseconds since an epoch
    * https://howardhinnant.github.io/date_algorithms.html
    *
    * @return
    */
  private def calculateDateParts: (Int, Int, Int, Int, Int, Int, Int, Int) =
    import DateTime.Constants.*

    val days     = Math.floorDiv(localMillisSinceUnixEpoch, millisPerDay) // Days since 1970-01-01
    val msOfDay  = Math.floorMod(localMillisSinceUnixEpoch, millisPerDay) // The millisecond part of the day
    val secOfDay = msOfDay / 1000L                                        // The seconds part of the day

    val z   = days + 719468                               // Shift epoch to 0000-03-01 (3 months ahead of 0000-01-01)
    val era = (if z >= 0 then z else z - 146096) / 146097 // Which 400-year block
    val doe = z - era * 146097                            // Day of era   [0, 146096]
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365 // Year of era [0, 399]
    val y   = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)               // Day of year from Mar 1
    val mp  = (5 * doy + 2) / 153                                   // Month, Mar = 0 .. Feb = 11

    val day   = (doy - (153 * mp + 2) / 5 + 1).toInt       // Day of month [1, 31]
    val month = (if mp < 10 then mp + 3 else mp - 9).toInt // Remap to January start 1..12
    val year  = (if month <= 2 then y + 1 else y).toInt    // Jan/Feb belong to next year (due oto previous March start)

    val hour        = (secOfDay / 3600).toInt // Calculate the hour of the day
    val minute      = ((secOfDay % 3600) / 60).toInt // Calculate the minute of the day
    val second      = (secOfDay  % 60).toInt         // Calculate the second of the day
    val millisecond = (msOfDay   % 1000).toInt       // Calculate the millisecond of the day

    val dayOfWeek = Math.floorMod(days + 4, 7).toInt // 0 = Sunday, 6 = Saturday

    (year, month, day, hour, minute, second, millisecond, dayOfWeek)

object DateTime:
  given Ordering[DateTime] = Ordering.by(_.epochMillis)

  object Constants:
    val millisPerDay: Long = 86_400_000L
