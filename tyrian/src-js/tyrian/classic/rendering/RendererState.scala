package tyrian.classic.rendering

enum RendererState derives CanEqual:
  case Idle
  case Running(lastTriggered: Long)
