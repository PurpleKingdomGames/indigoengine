package indigo.core.events

enum FocusEvent extends GlobalEvent:
  /** The application has received focus
    */
  case ApplicationGainedFocus

  /** The game canvas has received focus
    */
  case CanvasGainedFocus

  /** The application has lost focus
    */
  case ApplicationLostFocus

  /** The game canvas has lost focus
    */
  case CanvasLostFocus
