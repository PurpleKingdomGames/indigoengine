package tyrian.platform.runtime

enum RendererState derives CanEqual:
  case Idle
  case Running(lastTriggered: Long)
