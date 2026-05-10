package tyrian.sdl

import scala.scalanative.unsigned.UInt

import tyrian.GlobalMsg

enum SdlMsg extends GlobalMsg derives CanEqual:
  case Quit
  case Other(rawType: UInt)
