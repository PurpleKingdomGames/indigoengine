package tyrian

import scala.scalanative.unsigned.UInt

enum SDLMsg extends GlobalMsg:
  case Quit
  case Other(rawType: UInt)
