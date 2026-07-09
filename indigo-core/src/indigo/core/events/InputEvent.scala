package indigo.core.events

import indigo.core.constants.Key
import indigo.core.datatypes.Point
import indigo.core.events.MouseEvent.Move

/** Tags events for input devices like mice and keyboards. `InputEvent`s work in partnership with `InputState`. Events
  * represent a one time thing that happened since the last frame, while the state represents the _ongoing_ state of an
  * input.
  *
  * For example there is a mouse Move event i.e. "The mouse was moved" and there is also the mouse position on the
  * `InputState` i.e. "Where is the mouse now?"
  */
sealed trait InputEvent extends GlobalEvent with Product with Serializable

/** Represents a wheel event, such as a mouse wheel or touchpad scroll
  */
sealed trait WheelEvent extends InputEvent
object WheelEvent:
  enum DeltaMode derives CanEqual:
    /** The delta values are in pixels */
    case Pixel

    /** The delta values are in lines */
    case Line

    /** The delta values are in pages */
    case Page

  /** Represents a wheel event that has moved in one or more directions. This event is always fired when the wheel is
    * moved, along with it's helper counterparts (Vertical, Horizontal, Depth)
    *
    * @param deltaX
    * @param deltaY
    * @param deltaZ
    * @param deltaMode
    */
  final case class Move(deltaX: Double, deltaY: Double, deltaZ: Double, deltaMode: DeltaMode) extends WheelEvent

  object Move:
    def apply(deltaX: Double, deltaY: Double, deltaZ: Double): Move =
      Move(deltaX, deltaY, deltaZ, DeltaMode.Pixel)

  /** Represents a wheel event that has moved vertically, i.e. up or down
    *
    * @param deltaY
    * @param deltaMode
    */
  final case class Vertical(deltaY: Double, deltaMode: DeltaMode) extends WheelEvent {
    val direction =
      if deltaY < 0 then WheelDirection.Up
      else WheelDirection.Down
  }

  object Vertical:
    def unapply(e: Vertical): Option[Double] =
      Option(e.deltaY)

  /** Represents a wheel event that has moved horizontally, i.e. left or right
    *
    * @param deltaX
    * @param deltaMode
    */
  final case class Horizontal(deltaX: Double, deltaMode: DeltaMode) extends WheelEvent {
    val direction =
      if deltaX < 0 then WheelDirection.Left
      else WheelDirection.Right
  }

  object Horizontal:
    def unapply(e: Horizontal): Option[Double] =
      Option(e.deltaX)

  /** Represents a wheel event that has moved in the Z axis, i.e. depth
    *
    * @param deltaZ
    * @param deltaMode
    */
  final case class Depth(deltaZ: Double, deltaMode: DeltaMode) extends WheelEvent
  object Depth:
    def unapply(e: Depth): Option[Double] =
      Option(e.deltaZ)

trait PositionalInputEvent extends InputEvent:
  /** Unique pointer identifier
    */
  def pointerId: PointerId

  /** Coordinates relative to the screen
    */
  def position: Point

  /** The X position relative to the screen
    */
  def x: Int = position.x

  /** The Y position relative to the screen
    */
  def y: Int = position.y

  /** The delta position between this event and the last event relative to the screen
    */
  def movementPosition: Point

  /** The delta X position between this event and the last event relative to the screen
    */
  def movementX: Int = movementPosition.x

  /** The delta Y position between this event and the last event relative to the screen
    */
  def movementY: Int = movementPosition.y

/** Represents all mouse events
  */
sealed trait MouseEvent extends PositionalInputEvent:
  /** Unique pointer identifier
    */
  def pointerId: PointerId

  /** Coordinates relative to the screen
    */
  def position: Point

  /** The delta position between this event and the last event relative to the screen
    */
  def movementPosition: Point

object MouseEvent:

  /** The mouse button was clicked
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the mouse pointer relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param button
    *   The button that was clicked
    */
  final case class Click(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      button: MouseButton
  ) extends MouseEvent
  object Click:
    def apply(x: Int, y: Int): Click =
      Click(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )

    def apply(position: Point): Click =
      Click(
        PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    def unapply(e: Click): Option[Point] =
      Option(e.position)

  /** The mouse button was released
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the mouse pointer relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param button
    *   The button that was released
    */
  final case class Up(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      button: MouseButton
  ) extends MouseEvent
  object Up:
    def apply(position: Point): Up =
      Up(
        PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    def apply(x: Int, y: Int): Up =
      Up(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    def apply(x: Int, y: Int, button: MouseButton): Up =
      Up(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        button = button
      )
    def unapply(e: Up): Option[Point] =
      Option(e.position)

  /** The mouse button was pressed down
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the mouse pointer relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param button
    *   The button that was pressed down
    */
  final case class Down(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      button: MouseButton
  ) extends MouseEvent
  object Down:
    def apply(position: Point): Down =
      Down(
        PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    def apply(x: Int, y: Int): Down =
      Down(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    def apply(x: Int, y: Int, button: MouseButton): Down =
      Down(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        button = button
      )
    def unapply(e: Down): Option[Point] =
      Option(e.position)

  /** The mouse was moved to a new position
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the mouse pointer relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    */
  final case class Move(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point
  ) extends MouseEvent
  object Move:
    def apply(x: Int, y: Int): Move =
      Move(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero
      )
    def unapply(e: Move): Option[Point] =
      Option(e.position)

  /** Mouse has moved into game boundaries. It's counterpart is [[Leave]]
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the mouse pointer relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    */
  final case class Enter(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point
  ) extends MouseEvent
  object Enter:
    def unapply(e: Enter): Option[Point] =
      Option(e.position)

  /** Mouse has left game boundaries. It's counterpart is [[Enter]].
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the mouse pointer relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    */
  final case class Leave(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point
  ) extends MouseEvent
  object Leave:
    def unapply(e: Leave): Option[Point] =
      Option(e.position)

  /** The ongoing interactions was cancelled, which may occur when:
    *   - the mouse is disconnected
    *   - the device orientation changes
    *   - applications are switched
    *
    * @param pointerId
    * @param position
    * @param movementPosition
    */
  final case class Cancel(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point
  ) extends MouseEvent
  object Cancel:
    def unapply(e: Cancel): Option[Point] =
      Option(e.position)

end MouseEvent

/** Represents all touch events
  */
sealed trait TouchEvent extends PositionalInputEvent:
  /** Unique pointer identifier
    */
  def pointerId: PointerId

  /** The identifier of the finger that triggered the event */
  def fingerId: FingerId

  /** Coordinates relative to the screen
    */
  def position: Point

  /** The delta position between this event and the last event relative to the screen
    */
  def movementPosition: Point

  /** The normalised pressure of the touch (between 0 and 1) */
  def pressure: Double

object TouchEvent:

  /** Represents a tap of the finger on the screen
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param fingerId
    *   The unique identifier for the finger input
    * @param position
    *   The position of the tap relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param pressure
    *   The normalised pressure of the tap (between 0 and 1)
    */
  final case class Tap(
      pointerId: PointerId,
      fingerId: FingerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends TouchEvent
  object Tap:
    def apply(x: Int, y: Int): Tap =
      Tap(
        PointerId.unknown,
        FingerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1
      )
    def apply(position: Point): Tap =
      Tap(
        PointerId.unknown,
        FingerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pressure = 1
      )
    def unapply(e: Tap): Option[Point] =
      Option(e.position)

  /** The finger was released fromm the screen
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param fingerId
    *   The unique identifier for the finger input
    * @param position
    *   The position of the finger release relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param pressure
    *   The normalised pressure of the touch (between 0 and 1)
    */
  final case class Up(
      pointerId: PointerId,
      fingerId: FingerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends TouchEvent
  object Up:

    def apply(position: Point): Up =
      Up(
        PointerId.unknown,
        FingerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pressure = 0
      )

    def apply(x: Int, y: Int): Up =
      Up(
        PointerId.unknown,
        FingerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 0
      )

    def unapply(e: Up): Option[Point] =
      Option(e.position)

  /** The finger was pressed down on the screen
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param fingerId
    *   The unique identifier for the finger input
    * @param position
    *   The position of the finger press relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param pressure
    *   The normalised pressure of the touch (between 0 and 1)
    */
  final case class Down(
      pointerId: PointerId,
      fingerId: FingerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends TouchEvent
  object Down:

    def apply(position: Point): Down =
      Down(
        PointerId.unknown,
        FingerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pressure = 1
      )

    def apply(x: Int, y: Int): Down =
      Down(
        PointerId.unknown,
        FingerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1
      )

    def unapply(e: Down): Option[Point] =
      Option(e.position)

  /** The finger was moved on the screen, i.e. dragged
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param fingerId
    *   The unique identifier for the finger input
    * @param position
    *   The position of the finger relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param pressure
    *   The normalised pressure of the touch (between 0 and 1)
    */
  final case class Move(
      pointerId: PointerId,
      fingerId: FingerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends TouchEvent
  object Move:
    def apply(x: Int, y: Int): Move =
      Move(
        PointerId.unknown,
        FingerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1
      )
    def unapply(e: Move): Option[Point] =
      Option(e.position)

  /** A finger has entered the game boundaries. It's counterpart is [[Leave]].
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param fingerId
    *   The unique identifier for the finger input
    * @param position
    *   The position of the finger relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param pressure
    *   The normalised pressure of the touch (between 0 and 1)
    */
  final case class Enter(
      pointerId: PointerId,
      fingerId: FingerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends TouchEvent
  object Enter:
    def unapply(e: Enter): Option[Point] =
      Option(e.position)

  /** A finger has left the game boundaries. It's counterpart is [[Enter]]
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param fingerId
    *   The unique identifier for the finger input
    * @param position
    *   The position of the finger relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param pressure
    *   The normalised pressure of the touch (between 0 and 1)
    */
  final case class Leave(
      pointerId: PointerId,
      fingerId: FingerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends TouchEvent
  object Leave:
    def unapply(e: Leave): Option[Point] =
      Option(e.position)

  /** The ongoing interactions was cancelled, which may occur when:
    *   - the touch device is disconnected
    *   - the device orientation changes
    *   - a palm rejection is detected
    *   - applications are switched
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param fingerId
    *   The unique identifier for the finger input
    * @param position
    *   The position of the finger relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param pressure
    *   The normalised pressure of the touch (between 0 and 1)
    */
  final case class Cancel(
      pointerId: PointerId,
      fingerId: FingerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends TouchEvent
  object Cancel:
    def unapply(e: Cancel): Option[Point] =
      Option(e.position)

end TouchEvent

sealed trait PenEvent extends PositionalInputEvent:
  /** Unique pointer identifier
    */
  def pointerId: PointerId

  /** Coordinates relative to the screen
    */
  def position: Point

  /** The normalised pressure of the pen */
  def pressure: Double

object PenEvent:

  /** The pen has been pressed and released or a button on the pen has been pressed and released. Where a button is
    * provided, it indicates which button was pressed on the pen. If a button is not provided, it indicates that the pen
    * was pressed down on the pad
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the pen relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param pressure
    *   The normalised pressure of the pen (between 0 and 1)
    * @param button
    *   The button that was pressed, if any
    */
  final case class Click(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pressure: Double,
      button: Option[MouseButton]
  ) extends PenEvent
  object Click:
    def apply(x: Int, y: Int): Click =
      Click(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1,
        button = Option.empty
      )
    def apply(x: Int, y: Int, button: MouseButton): Click =
      Click(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1,
        button = Option(button)
      )
    def apply(position: Point): Click =
      Click(
        PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pressure = 1,
        button = Option.empty
      )
    def apply(position: Point, button: MouseButton): Click =
      Click(
        PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pressure = 1,
        button = Option(button)
      )
    def unapply(e: Click): Option[Point] =
      Option(e.position)

  /** The pen has been released or a button on the pen has been released. Where a button is provided, it indicates which
    * button was released on the pen. If a button is not provided, it indicates that the pen was released from the pad
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the pen relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param pressure
    *   The normalised pressure of the pen (between 0 and 1)
    * @param button
    *   The button that was released, if any
    */
  final case class Up(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pressure: Double,
      button: Option[MouseButton]
  ) extends PenEvent
  object Up:
    def apply(position: Point): Up =
      Up(
        PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pressure = 1,
        button = Option.empty
      )
    def apply(x: Int, y: Int): Up =
      Up(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1,
        button = Option.empty
      )
    def apply(x: Int, y: Int, button: MouseButton): Up =
      Up(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1,
        button = Option(button)
      )
    def unapply(e: Up): Option[Point] =
      Option(e.position)

  /** The pen was pressed down on the pad or a button on the pen was pressed down. Where a button is provided, it
    * indicates which button was pressed on the pen. If a button is not provided, it indicates that the pen was pressed
    * down on the pad
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the pen relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param pressure
    *   The normalised pressure of the pen (between 0 and 1)
    * @param button
    *   The button that was pressed, if any
    */
  final case class Down(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pressure: Double,
      button: Option[MouseButton]
  ) extends PenEvent
  object Down:
    def apply(position: Point): Down =
      Down(
        PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pressure = 1,
        button = Option.empty
      )
    def apply(x: Int, y: Int): Down =
      Down(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1,
        button = Option.empty
      )
    def apply(position: Point, button: MouseButton): Down =
      Down(
        PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pressure = 1,
        button = Option(button)
      )
    def apply(x: Int, y: Int, button: MouseButton): Down =
      Down(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1,
        button = Option(button)
      )
    def unapply(e: Down): Option[Point] =
      Option(e.position)

  /** The pen was moved on the pad, i.e. dragged
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the pen relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param pressure
    *   The normalised pressure of the pen (between 0 and 1)
    */
  final case class Move(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends PenEvent
  object Move:
    def apply(x: Int, y: Int): Move =
      Move(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1
      )
    def unapply(e: Move): Option[Point] =
      Option(e.position)

  /** Pen has entered the game boundaries. It's counterpart is [[Leave]]
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the pen relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param pressure
    *   The normalised pressure of the pen (between 0 and 1)
    */
  final case class Enter(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends PenEvent
  object Enter:
    def unapply(e: Enter): Option[Point] =
      Option(e.position)

  /** Pen has left the game boundaries. It's counterpart is [[Enter]]
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the pen relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param pressure
    *   The normalised pressure of the pen (between 0 and 1)
    */
  final case class Leave(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends PenEvent
  object Leave:
    def unapply(e: Leave): Option[Point] =
      Option(e.position)

  /** The ongoing interactions was cancelled, which may occur when:
    *   - the pen is disconnected
    *   - the device orientation changes
    *   - applications are switched
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the pen relative to the screen
    * @param movementPosition
    *   The delta position between this event and the last event relative to the screen
    * @param pressure
    *   The normalised pressure of the pen (between 0 and 1)
    */
  final case class Cancel(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends PenEvent
  object Cancel:
    def unapply(e: Cancel): Option[Point] =
      Option(e.position)

end PenEvent

/** Represents all mouse, pen and touch events
  */
sealed trait PointerEvent extends PositionalInputEvent:

  /** Indicates the device type that caused the event (mouse, pen, touch, etc.)
    */
  def pointerType: PointerType

object PointerEvent:
  /** Pointing device is moved into canvas hit test boundaries. It's counterpart is [[Leave]].
    */
  final case class Enter(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pointerType: PointerType
  ) extends PointerEvent
  object Enter:
    def apply(pointerId: PointerId, position: Point, movementPosition: Point, pointerType: PointerType): Enter =
      Enter(
        pointerId = pointerId,
        position = position,
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = movementPosition,
        width = 0,
        height = 0,
        pressure = 0,
        tangentialPressure = 0,
        tiltX = Radians.zero,
        tiltY = Radians.zero,
        twist = Radians.zero,
        pointerType = pointerType,
        isPrimary = true
      )

    def unapply(e: Enter): Option[Point] =
      Option(e.position)

  /** Pointing device left canvas hit test boundaries. It's counterpart is [[Enter]].
    */
  final case class Leave(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pointerType: PointerType
  ) extends PointerEvent
  object Leave:
    def apply(pointerId: PointerId, position: Point, movementPosition: Point, pointerType: PointerType): Leave =
      Leave(
        pointerId = pointerId,
        position = position,
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = movementPosition,
        width = 0,
        height = 0,
        pressure = 0,
        tangentialPressure = 0,
        tiltX = Radians.zero,
        tiltY = Radians.zero,
        twist = Radians.zero,
        pointerType = pointerType,
        isPrimary = true
      )

    def unapply(e: Leave): Option[Point] =
      Option(e.position)

  /** Pointing device is in active buttons state.
    */
  final case class Down(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pointerType: PointerType,
      button: Option[MouseButton]
  ) extends PointerEvent
  object Down:
    def apply(position: Point): Down =
      Down(position, MouseButton.LeftMouseButton, PointerType.Mouse)
    def apply(x: Int, y: Int): Down =
      Down(Point(x, y), MouseButton.LeftMouseButton, PointerType.Mouse)
    def apply(position: Point, pointerType: PointerType): Down =
      Down(position, MouseButton.LeftMouseButton, pointerType)
    def apply(x: Int, y: Int, pointerType: PointerType): Down =
      Down(Point(x, y), MouseButton.LeftMouseButton, pointerType)
    def apply(position: Point, button: MouseButton): Down =
      Down(position, button, PointerType.Mouse)
    def apply(x: Int, y: Int, button: MouseButton): Down =
      Down(Point(x, y), button, PointerType.Mouse)
    def apply(x: Int, y: Int, button: MouseButton, pointerType: PointerType): Down =
      Down(Point(x, y), button, pointerType)

    def apply(position: Point, button: MouseButton, pointerType: PointerType): Down =
      Down(
        pointerId = PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        button = Some(button),
        pointerType = pointerType
      )

    def unapply(e: Down): Option[Point] =
      Option(e.position)

  /** Pointing device is no longer in active buttons state.
    */
  final case class Up(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pointerType: PointerType,
      button: Option[MouseButton]
  ) extends PointerEvent
  object Up:
    def apply(position: Point): Up =
      Up(position, MouseButton.LeftMouseButton, PointerType.Mouse)
    def apply(x: Int, y: Int): Up =
      Up(Point(x, y), MouseButton.LeftMouseButton, PointerType.Mouse)
    def apply(position: Point, pointerType: PointerType): Up =
      Up(position, MouseButton.LeftMouseButton, pointerType)
    def apply(x: Int, y: Int, pointerType: PointerType): Up =
      Up(Point(x, y), MouseButton.LeftMouseButton, pointerType)
    def apply(position: Point, button: MouseButton): Up =
      Up(position, button, PointerType.Mouse)
    def apply(x: Int, y: Int, button: MouseButton): Up =
      Up(Point(x, y), button, PointerType.Mouse)
    def apply(x: Int, y: Int, button: MouseButton, pointerType: PointerType): Up =
      Up(Point(x, y), button, pointerType)

    def apply(position: Point, button: MouseButton, pointerType: PointerType): Up =
      Up(
        pointerId = PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        button = Some(button),
        pointerType = pointerType
      )

    def unapply(e: Up): Option[Point] =
      Option(e.position)

  /** Pointing device button has been clicked */
  final case class Click(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pointerType: PointerType,
      button: Option[MouseButton]
  ) extends PointerEvent
  object Click:
    def apply(position: Point): Click =
      Click(position, MouseButton.LeftMouseButton, PointerType.Mouse)
    def apply(x: Int, y: Int): Click =
      Click(Point(x, y), MouseButton.LeftMouseButton, PointerType.Mouse)
    def apply(position: Point, pointerType: PointerType): Click =
      Click(position, MouseButton.LeftMouseButton, pointerType)
    def apply(x: Int, y: Int, pointerType: PointerType): Click =
      Click(Point(x, y), MouseButton.LeftMouseButton, pointerType)
    def apply(position: Point, button: MouseButton): Click =
      Click(position, button, PointerType.Mouse)
    def apply(x: Int, y: Int, button: MouseButton): Click =
      Click(Point(x, y), button, PointerType.Mouse)
    def apply(x: Int, y: Int, button: MouseButton, pointerType: PointerType): Click =
      Click(Point(x, y), button, pointerType)

    def apply(position: Point, button: MouseButton, pointerType: PointerType): Click =
      Click(
        pointerId = PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        button = Some(button),
        pointerType = pointerType
      )
    def unapply(e: Click): Option[Point] =
      Option(e.position)

  /** Pointing device changed coordinates.
    */
  final case class Move(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pointerType: PointerType
  ) extends PointerEvent
  object Move:
    def apply(position: Point): Move =
      Move(position, PointerType.Mouse)
    def apply(x: Int, y: Int): Move =
      Move(Point(x, y), PointerType.Mouse)
    def apply(x: Int, y: Int, pointerType: PointerType): Move =
      Move(Point(x, y), pointerType)

    def apply(position: Point, pointerType: PointerType): Move =
      Move(
        pointerId = PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pointerType = pointerType
      )

    def unapply(e: Move): Option[Point] =
      Option(e.position)

  /** The ongoing interactions was cancelled due to:
    *   - the pointer device being disconnected
    *   - device orientation change
    *   - palm rejection
    *   - switching applications
    */
  final case class Cancel(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pointerType: PointerType
  ) extends PointerEvent
  object Cancel:
    def unapply(e: Cancel): Option[Point] =
      Option(e.position)

/** Represents all keyboard events
  */
sealed trait KeyboardEvent extends InputEvent {
  val key: Key
  val isRepeat: Boolean
  val isAltKeyDown: Boolean
  val isCtrlKeyDown: Boolean
  val isMetaKeyDown: Boolean
  val isShiftKeyDown: Boolean
}
object KeyboardEvent {

  /** A key was released during the last frame
    *
    * @param key
    *   A `Key` instance representing the key that was released
    * @param isRepeat
    *   Whether the key was pressed repeatedly since the last frame
    * @param isAltKeyDown
    *   Whether the `alt` key was pressed when the event was fired
    * @param isCtrlKeyDown
    *   Whether the `ctrl` key was pressed when the event was fired
    * @param isMetaKeyDown
    *   Whether the meta button (Windows key, or Cmd Key) key was pressed when the event was fired
    * @param isShiftKeyDown
    *   Whether the `shift` key was pressed when the event was fired
    */
  final case class KeyUp(
      key: Key,
      isRepeat: Boolean,
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean
  ) extends KeyboardEvent
  object KeyUp:
    def apply(key: Key): KeyUp =
      KeyUp(
        key,
        isRepeat = false,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false
      )
    def unapply(e: KeyUp): Option[Key] =
      Option(e.key)

  /** A key was pressed down during the last frame
    *
    * @param key
    *   A `Key` instance representing the key that was pressed
    * @param isRepeat
    *   Whether the key was pressed repeatedly since the last frame
    * @param isAltKeyDown
    *   Whether the `alt` key was pressed when the event was fired
    * @param isCtrlKeyDown
    *   Whether the `ctrl` key was pressed when the event was fired
    * @param isMetaKeyDown
    *   Whether the meta button (Windows key, or Cmd Key) key was pressed when the event was fired
    * @param isShiftKeyDown
    *   Whether the `shift` key was pressed when the event was fired
    */
  final case class KeyDown(
      key: Key,
      isRepeat: Boolean,
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean
  ) extends KeyboardEvent
  object KeyDown:
    def apply(key: Key): KeyDown =
      KeyDown(
        key,
        isRepeat = false,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false
      )
    def unapply(e: KeyDown): Option[Key] =
      Option(e.key)
}
