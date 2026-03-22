package indigo.internal.services

// import indigo.core.input.AnalogAxis
import indigo.core.input.Gamepad
import indigo.core.input.GamepadInputCapture
// import org.scalajs.dom.window

object NativeGamepadInputService:

//   // PS4 controller vendor/product ids - workaround until we have a more
//   // sound design on controller layouts; `GamepadButtons` is currently
//   // hardcoded to PS-style buttons.
//   private val ps4ControllerVendorProduct = Seq("054c", "05c4")

//   private def usesTouchpad(id: String): Boolean =
//     ps4ControllerVendorProduct.forall(id.contains(_))

//   /* PS4 Layout
//     Axis array:
//     0 left stick X (double 1 is right -1 is left)
//     1 left stick Y (double 1 is down -1 is up)
//     2 right stick X (double 1 is right -1 is left)
//     3 right stick Y (double 1 is down -1 is up)

//     Buttons array:
//     0 X
//     1 O
//     2 Square
//     3 Triangle
//     4 L1
//     5 R1
//     6 L2
//     7 R2
//     8 Share
//     9 Options
//     10 Left Stick Press
//     11 Right Stick Press
//     12 D Up
//     13 D Down
//     14 D Left
//     15 D Right
//     16 PS Button
//     17 Touch pad press
//    */
  def apply(): GamepadInputCapture =
    new GamepadInputCapture {
      def giveGamepadState: Gamepad =
        // No game pads in the PoC. Hope
        Gamepad.default
//         // Some browsers (e.g. Firefox) refresh the gamepad state internally,
//         // others (Webkit-based) do not - so re-poll every frame.
//         window.navigator
//           .getGamepads()
//           .find(Option(_).exists(_.connected)) match {
//           case Some(gp) =>
//             val gameAnalogControls = {
//               val numberOfAxes = gp.axes.length / 2
//               GamepadAnalogControls(
//                 AnalogAxis(gp.axes(0), gp.axes(1), gp.buttons(10).pressed),
//                 if numberOfAxes >= 2 then AnalogAxis(gp.axes(2), gp.axes(3), gp.buttons(11).pressed)
//                 else AnalogAxis.default,
//                 numberOfAxes
//               )
//             }
//             new Gamepad(
//               connected = true,
//               gameAnalogControls,
//               new GamepadDPad(
//                 gp.buttons(12).pressed,
//                 gp.buttons(13).pressed,
//                 gp.buttons(14).pressed,
//                 gp.buttons(15).pressed
//               ),
//               new GamepadButtons(
//                 gp.buttons(0).pressed,
//                 gp.buttons(1).pressed,
//                 gp.buttons(2).pressed,
//                 gp.buttons(3).pressed,
//                 gp.buttons(4).pressed,
//                 gp.buttons(5).pressed,
//                 gp.buttons(6).pressed,
//                 gp.buttons(7).pressed,
//                 gp.buttons(8).pressed,
//                 gp.buttons(9).pressed,
//                 gp.buttons(16).pressed,
//                 usesTouchpad(gp.id) && gp.buttons(17).pressed
//               )
//             )

//           case None =>
//             Gamepad.default
//         }
    }
