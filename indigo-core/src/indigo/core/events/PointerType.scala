package indigo.core.events

/** The type of pointer that has emitted an input pointer event
  */
enum PointerType derives CanEqual:
  case Mouse, Pen, Touch, Unknown
