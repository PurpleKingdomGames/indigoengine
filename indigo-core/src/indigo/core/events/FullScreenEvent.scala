package indigo.core.events

enum FullScreenEvent extends GlobalEvent:
  /** Attempt to enter or exit full screen mode
    */
  case Toggle

  /** Attempt to enter full screen mode
    */
  case Enter

  /** Attempt to exit full screen mode
    */
  case Exit

  /** The game entered full screen mode
    */
  case Entered

  /** A problem occurred trying to enter full screen
    */
  case EnterError

  /** The game exited full screen mode
    */
  case Exited

  /** A problem occurred trying to exit full screen
    */
  case ExitError
