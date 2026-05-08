package indigo.core.events

enum FocusEvent extends GlobalEvent:
  /** The application has received focus
    */
  case ApplicationGainedFocus

  /** The game has received focus
    */
  case CanvasGainedFocus

  /** The application has lost focus
    */
  case ApplicationLostFocus

  /** The game has lost focus
    */
  case CanvasLostFocus
