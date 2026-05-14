package tyrian

import indigoengine.sdl.facades.sdl.SDLEvents

import scala.scalanative.unsigned.UInt

enum SDLMsg derives CanEqual:
  case Quit
  // TODO: Frame isn't _really_ an SDLMsg. We just want a watcher that hooks into the loop.
  case Frame(runningTime: Seconds)
  case Other(rawType: UInt)

object SDLMsg:

  def fromSDLEvent(event: UInt): SDLMsg =
    event match
      case SDLEvents.SDL_EVENT_QUIT =>
        SDLMsg.Quit

      case raw =>
        SDLMsg.Other(event)
