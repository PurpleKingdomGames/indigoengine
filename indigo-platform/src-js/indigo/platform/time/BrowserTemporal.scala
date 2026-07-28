package indigo.platform.time

import scala.scalajs.js.annotation.JSGlobal

import scalajs.js

@js.native
@JSGlobal("Temporal.Now")
object TemporalNow extends js.Object:
  def zonedDateTimeISO(): TemporalZonedDateTime = js.native

@js.native
trait TemporalZonedDateTime extends js.Object:
  val epochMilliseconds: Double = js.native
  val offsetNanoseconds: Double = js.native
