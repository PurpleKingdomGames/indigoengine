package tyrian.classic.runtime

enum RendererState derives CanEqual:
  case Idle
  case Running(lastTriggered: Long)
