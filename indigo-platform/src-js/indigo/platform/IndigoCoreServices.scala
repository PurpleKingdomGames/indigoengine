package indigo.platform

import indigo.core.input.GamepadInputCapture
import indigo.platform.audio.AudioService
import indigo.platform.imaging.ImageService

/** IndigoCoreServices is a collection of interfaces to platform managed utilities and services. Indigo needs these
  * things to work, but does not need to know how they function on each platform.
  */
trait IndigoCoreServices:

  def gamepadInputCapture: GamepadInputCapture

  def audioService: AudioService

  def imageService: ImageService

  def kill(): Unit

object IndigoCoreServices:

  def apply(
      _gamepadInputCapture: GamepadInputCapture,
      _audioService: AudioService,
      _imageService: ImageService
  ): IndigoCoreServices =
    new IndigoCoreServices {
      def gamepadInputCapture: GamepadInputCapture = _gamepadInputCapture
      def audioService: AudioService               = _audioService
      def imageService: ImageService               = _imageService

      def kill(): Unit =
        _audioService.kill()
    }
