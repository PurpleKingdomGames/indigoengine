package indigo.core.events

import indigo.core.input.Gamepad
import indigo.core.input.Keyboard
import indigo.core.input.MouseState
import indigo.core.input.PenState
import indigo.core.input.PointerState
import indigo.core.input.TouchState
import indigo.core.input.Wheel
import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.Millis

import scala.annotation.nowarn

/** Holds a snapshot of the states of the various input types as they were entering this frame.
  *
  * @param mouse
  *   Current state of any mice
  * @param keyboard
  *   Current state of the keyboard
  * @param pen
  *   Current state of any pen inputs
  * @param touch
  *   Current state of and touch inputs
  * @param gamepad
  *   Current state of the gamepad
  * @param pointer
  *   Current state of all pointers, including mouse, pen and touch
  */
final class InputState(
    val mouse: MouseState,
    val keyboard: Keyboard,
    val gamepad: Gamepad,
    val wheel: Wheel,
    val pen: PenState,
    val touch: TouchState,
    val pointer: PointerState
) {

  /** Given some input mappings, produce a guaranteed value A based on the current InputState.
    */
  def mapInputs[A](mappings: InputMapping[A], default: A): A =
    mappings.find(mouse, wheel, keyboard, gamepad).getOrElse(default)

  /** Given some input mappings, produce an optional value A based on the current InputState.
    */
  def mapInputsOption[A](mappings: InputMapping[A]): Option[A] =
    mappings.find(mouse, wheel, keyboard, gamepad)

}

object InputState {
  val default: InputState =
    InputState(
      MouseState.default,
      Keyboard.default,
      Gamepad.default,
      Wheel.default,
      PenState.default,
      TouchState.default,
      PointerState.default
    )

  def calculateNext(
      previous: InputState,
      events: Batch[InputEvent],
      gamepadState: Gamepad
  ): InputState = calculateNext(
    previous,
    events,
    gamepadState,
    Millis.zero
  )
  def calculateNext(
      previous: InputState,
      events: Batch[InputEvent],
      gamepadState: Gamepad,
      time: Millis
  ): InputState =
    @nowarn("msg=deprecated")
    val state = InputState(
      previous.mouse.calculateNext(events.collect { case e: MouseEvent => e }),
      Keyboard.calculateNext(previous.keyboard, events.collect { case e: KeyboardEvent => e }),
      gamepadState,
      Wheel(events.collect { case e: WheelEvent.Move => e }),
      previous.pen.calculateNext(events.collect { case e: PenEvent => e }),
      previous.touch.calculateNext(events.collect { case e: TouchEvent => e }),
      previous.pointer.calculateNext(events.collect { case e: PointerEvent => e }, time)
    )

    state
}
