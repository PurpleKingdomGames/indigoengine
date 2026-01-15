package tyrian

/** Base trait for all messages in tyrian-next applications. All events, user actions, and system messages must extend
  * GlobalMsg to participate in the application's update cycle.
  */
trait GlobalMsg
object GlobalMsg:
  given CanEqual[GlobalMsg, GlobalMsg] = CanEqual.derived
