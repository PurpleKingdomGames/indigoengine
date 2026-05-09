package indigo.platform

import indigo.core.input.GamepadInputCapture
import indigo.platform.audio.AudioService

/** IndigoCoreServices is a collection of interfaces to platform managed utilities and services. Indigo needs these
  * things to work, but does not need to know how they function on each platform.
  */
trait IndigoCoreServices:

  def gamepadInputCapture: GamepadInputCapture

  def audioService: AudioService

  def kill(): Unit

object IndigoCoreServices:

  def apply(_gamepadInputCapture: GamepadInputCapture, _audioService: AudioService): IndigoCoreServices =
    new IndigoCoreServices {
      def gamepadInputCapture: GamepadInputCapture = _gamepadInputCapture
      def audioService: AudioService               = _audioService

      def kill(): Unit =
        _audioService.kill()
    }
