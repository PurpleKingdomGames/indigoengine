package indigo.platform

import indigo.core.input.GamepadInputCapture
import indigo.platform.audio.AudioService
import indigo.platform.imaging.ImageService

/** IndigoCoreServices is a collection of interfaces to platform managed utilities and services. Indigo needs these
  * things to work, but does not need to know how they function on each platform.
  */
trait IndigoCoreServices[Image, ImageData]:

  def gamepadInputCapture: GamepadInputCapture

  def audioService: AudioService

  def imageService: ImageService[Image, ImageData]

  def kill(): Unit

object IndigoCoreServices:

  def apply[Image, ImageData](
      _gamepadInputCapture: GamepadInputCapture,
      _audioService: AudioService,
      _imageService: ImageService[Image, ImageData]
  ): IndigoCoreServices[Image, ImageData] =
    new IndigoCoreServices[Image, ImageData] {
      def gamepadInputCapture: GamepadInputCapture     = _gamepadInputCapture
      def audioService: AudioService                   = _audioService
      def imageService: ImageService[Image, ImageData] = _imageService

      def kill(): Unit =
        _audioService.kill()
    }
