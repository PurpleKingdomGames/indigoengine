package indigo.platform.time

import indigo.core.time.DateTime
import indigo.core.time.DateFormat
import indigo.core.time.TimeFormat

/** Deals with retrieving locales that a user has on their system
  */
trait DateTimeService:
  /** The current active system locale. None if no locale has been specified
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
