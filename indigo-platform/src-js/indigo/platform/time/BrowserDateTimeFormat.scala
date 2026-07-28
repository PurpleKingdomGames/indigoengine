package indigo.platform.time

import scala.annotation.nowarn
import scala.scalajs.js.annotation.JSGlobal

import scalajs.js

@js.native
@JSGlobal("Intl.DateTimeFormat")
@nowarn("msg=unused")
class BrowserDateTimeFormat(locales: js.UndefOr[js.Array[String]], options: js.UndefOr[js.Object]) extends js.Object:
  def formatToParts(date: js.Date): js.Array[DateTimeFormatPart] = js.native
  def resolvedOptions(): ResolvedOptions                         = js.native

@js.native
trait DateTimeFormatPart extends js.Object:
  val `type`: String = js.native
  val value: String  = js.native

@js.native
trait ResolvedOptions extends js.Object:
  val hourCycle: js.UndefOr[String] = js.native
  val hour12: js.UndefOr[Boolean]   = js.native

trait DateTimeFormatOptions extends js.Object:
  val year: String
  val month: String
  val day: String
  val hour: String
