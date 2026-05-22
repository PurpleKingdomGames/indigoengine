package indigo.internal.services

import indigo.core.input.Gamepad
import indigo.core.input.GamepadInputCapture

object NativeGamepadInputService:

  def apply(): GamepadInputCapture =
    new GamepadInputCapture {
      def giveGamepadState: Gamepad =
        Gamepad.default
    }
