package indigo.core.events

/** Represents in which direction the wheel input was rotated
  */
enum WheelDirection derives CanEqual:
  case Up, Down, Left, Right
