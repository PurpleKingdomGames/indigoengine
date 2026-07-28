package indigo.platform.time

import indigo.core.time.DateTime
import indigo.core.time.DateFormat
import indigo.core.time.TimeFormat

/** Provides access to the system's current date/time and preferred date/time display formats
  */
trait DateTimeService:
  /** The current system date/time (local time) including the current UTC offset
    *
    * @return
    */
  def current: DateTime

  /** The current system local date format enum
    *
    * @return
    */
  def dateformat: DateFormat

  /** The current system time format enum
    *
    * @return
    */
  def timeformat: TimeFormat
