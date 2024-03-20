package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.CharSheet
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions
import roguelikestarterkit.ui.datatypes.UiContext

final case class WindowModel[StartupData, CA, A](
    id: WindowId,
    charSheet: CharSheet,
    bounds: Bounds,
    title: Option[String],
    contentModel: A,
    updateContentModel: (UiContext, A) => GlobalEvent => Outcome[A],
    presentContentModel: (UiContext, A) => Outcome[SceneUpdateFragment],
    draggable: Boolean,
    resizable: Boolean,
    closeable: Boolean,
    hasFocus: Boolean,
    static: Boolean,
    minSize: Option[Dimensions],
    maxSize: Option[Dimensions],
    state: WindowState
):

  lazy val minAllowedSize: Dimensions =
    val m = if title.isEmpty then Dimensions(3) else Dimensions(3, 5)
    minSize.fold(m)(_.max(m))

  def withId(value: WindowId): WindowModel[StartupData, CA, A] =
    this.copy(id = value)

  def withBounds(value: Bounds): WindowModel[StartupData, CA, A] =
    this.copy(bounds = value)

  def withPosition(value: Coords): WindowModel[StartupData, CA, A] =
    withBounds(bounds.moveTo(value))
  def moveTo(position: Coords): WindowModel[StartupData, CA, A] =
    withPosition(position)
  def moveTo(x: Int, y: Int): WindowModel[StartupData, CA, A] =
    moveTo(Coords(x, y))
  def moveBy(amount: Coords): WindowModel[StartupData, CA, A] =
    withPosition(bounds.coords + amount)
  def moveBy(x: Int, y: Int): WindowModel[StartupData, CA, A] =
    moveBy(Coords(x, y))

  def withDimensions(value: Dimensions): WindowModel[StartupData, CA, A] =
    val d = value.max(minAllowedSize)
    withBounds(bounds.withDimensions(maxSize.fold(d)(_.min(d))))
  def resizeTo(size: Dimensions): WindowModel[StartupData, CA, A] =
    withDimensions(size)
  def resizeTo(x: Int, y: Int): WindowModel[StartupData, CA, A] =
    resizeTo(Dimensions(x, y))
  def resizeBy(amount: Dimensions): WindowModel[StartupData, CA, A] =
    withDimensions(bounds.dimensions + amount)
  def resizeBy(x: Int, y: Int): WindowModel[StartupData, CA, A] =
    resizeBy(Dimensions(x, y))

  def withTitle(value: String): WindowModel[StartupData, CA, A] =
    this.copy(title = Option(value))

  def withModel(value: A): WindowModel[StartupData, CA, A] =
    this.copy(contentModel = value)

  def updateModel(
      f: (UiContext, A) => GlobalEvent => Outcome[A]
  ): WindowModel[StartupData, CA, A] =
    this.copy(updateContentModel = f)

  def present(
      f: (UiContext, A) => Outcome[SceneUpdateFragment]
  ): WindowModel[StartupData, CA, A] =
    this.copy(presentContentModel = f)

  def withDraggable(value: Boolean): WindowModel[StartupData, CA, A] =
    this.copy(draggable = value)
  def isDraggable: WindowModel[StartupData, CA, A] =
    withDraggable(true)
  def notDraggable: WindowModel[StartupData, CA, A] =
    withDraggable(false)

  def withResizable(value: Boolean): WindowModel[StartupData, CA, A] =
    this.copy(resizable = value)
  def isResizable: WindowModel[StartupData, CA, A] =
    withResizable(true)
  def notResizable: WindowModel[StartupData, CA, A] =
    withResizable(false)

  def withCloseable(value: Boolean): WindowModel[StartupData, CA, A] =
    this.copy(closeable = value)
  def isCloseable: WindowModel[StartupData, CA, A] =
    withCloseable(true)
  def notCloseable: WindowModel[StartupData, CA, A] =
    withCloseable(false)

  def withFocus(value: Boolean): WindowModel[StartupData, CA, A] =
    this.copy(hasFocus = value)
  def focus: WindowModel[StartupData, CA, A] =
    withFocus(true)
  def blur: WindowModel[StartupData, CA, A] =
    withFocus(false)

  def withStatic(value: Boolean): WindowModel[StartupData, CA, A] =
    this.copy(static = value)
  def isStatic: WindowModel[StartupData, CA, A] =
    withStatic(true)
  def notStatic: WindowModel[StartupData, CA, A] =
    withStatic(false)

  def withMinSize(min: Dimensions): WindowModel[StartupData, CA, A] =
    this.copy(minSize = Option(min))
  def withMinSize(width: Int, height: Int): WindowModel[StartupData, CA, A] =
    this.copy(minSize = Option(Dimensions(width, height)))
  def noMinSize: WindowModel[StartupData, CA, A] =
    this.copy(minSize = None)

  def withMaxSize(max: Dimensions): WindowModel[StartupData, CA, A] =
    this.copy(maxSize = Option(max))
  def withMaxSize(width: Int, height: Int): WindowModel[StartupData, CA, A] =
    this.copy(maxSize = Option(Dimensions(width, height)))
  def noMaxSize: WindowModel[StartupData, CA, A] =
    this.copy(maxSize = None)

  def withState(value: WindowState): WindowModel[StartupData, CA, A] =
    this.copy(state = value)
  def open: WindowModel[StartupData, CA, A] =
    withState(WindowState.Open)
  def close: WindowModel[StartupData, CA, A] =
    withState(WindowState.Closed)

  def isOpen: Boolean =
    state == WindowState.Open
  def isClosed: Boolean =
    state == WindowState.Closed

object WindowModel:

  def apply[StartupData, CA, A](
      id: WindowId,
      charSheet: CharSheet,
      content: A
  ): WindowModel[StartupData, CA, A] =
    WindowModel(
      id,
      charSheet,
      Bounds(Coords.zero, Dimensions.zero),
      None,
      contentModel = content,
      updateContentModel = (_, _) => _ => Outcome(content),
      presentContentModel = (_, _) => Outcome(SceneUpdateFragment.empty),
      false,
      false,
      false,
      false,
      false,
      None,
      None,
      WindowState.Closed
    )

  def apply[StartupData, CA](
      id: WindowId,
      charSheet: CharSheet
  ): WindowModel[StartupData, CA, Unit] =
    WindowModel(id, charSheet, ())
