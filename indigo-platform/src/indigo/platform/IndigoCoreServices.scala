package indigo.platform

import indigo.core.input.GamepadInputCapture

/** IndigoCoreServices is a collection of interfaces to platform managed utilities and services. Indigo needs these
  * things to work, but does not need to know how they function on each platform.
  */
trait IndigoCoreServices:

  def gamepadInputCapture: GamepadInputCapture

  /*
  TODO, missing services:
    - Image service, for copying image data from one image to another
    - Fullscreen service, for entering, toggling, and exiting full screen mode
    - Screen Capture service
   */

object IndigoCoreServices:

  def apply(_gamepadInputCapture: GamepadInputCapture): IndigoCoreServices =
    new IndigoCoreServices {
      def gamepadInputCapture: GamepadInputCapture = _gamepadInputCapture
    }
