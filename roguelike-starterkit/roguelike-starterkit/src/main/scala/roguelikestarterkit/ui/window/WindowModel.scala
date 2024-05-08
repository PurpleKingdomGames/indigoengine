package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.CharSheet
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions
import roguelikestarterkit.ui.datatypes.UiContext

final case class WindowModel[A, ReferenceData](
    id: WindowId,
    charSheet: CharSheet,
    bounds: Bounds,
    title: Option[String],
    contentModel: A,
    windowContent: WindowContent[A, ReferenceData],
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

  def withId(value: WindowId): WindowModel[A, ReferenceData] =
    this.copy(id = value)

  def withBounds(value: Bounds): WindowModel[A, ReferenceData] =
    val withNewBounds = this.copy(bounds = value)

    withNewBounds.copy(
      contentModel = windowContent.cascade(
        contentModel,
        Window.calculateContentRectangle(value, withNewBounds)
      )
    )

  def withPosition(value: Coords): WindowModel[A, ReferenceData] =
    withBounds(bounds.moveTo(value))
  def moveTo(position: Coords): WindowModel[A, ReferenceData] =
    withPosition(position)
  def moveTo(x: Int, y: Int): WindowModel[A, ReferenceData] =
    moveTo(Coords(x, y))
  def moveBy(amount: Coords): WindowModel[A, ReferenceData] =
    withPosition(bounds.coords + amount)
  def moveBy(x: Int, y: Int): WindowModel[A, ReferenceData] =
    moveBy(Coords(x, y))

  def withDimensions(value: Dimensions): WindowModel[A, ReferenceData] =
    val d = value.max(minAllowedSize)
    withBounds(bounds.withDimensions(maxSize.fold(d)(_.min(d))))
  def resizeTo(size: Dimensions): WindowModel[A, ReferenceData] =
    withDimensions(size)
  def resizeTo(x: Int, y: Int): WindowModel[A, ReferenceData] =
    resizeTo(Dimensions(x, y))
  def resizeBy(amount: Dimensions): WindowModel[A, ReferenceData] =
    withDimensions(bounds.dimensions + amount)
  def resizeBy(x: Int, y: Int): WindowModel[A, ReferenceData] =
    resizeBy(Dimensions(x, y))

  def withTitle(value: String): WindowModel[A, ReferenceData] =
    this.copy(title = Option(value))

  def withModel(value: A): WindowModel[A, ReferenceData] =
    this.copy(contentModel = value)

  def withDraggable(value: Boolean): WindowModel[A, ReferenceData] =
    this.copy(draggable = value)
  def isDraggable: WindowModel[A, ReferenceData] =
    withDraggable(true)
  def notDraggable: WindowModel[A, ReferenceData] =
    withDraggable(false)

  def withResizable(value: Boolean): WindowModel[A, ReferenceData] =
    this.copy(resizable = value)
  def isResizable: WindowModel[A, ReferenceData] =
    withResizable(true)
  def notResizable: WindowModel[A, ReferenceData] =
    withResizable(false)

  def withCloseable(value: Boolean): WindowModel[A, ReferenceData] =
    this.copy(closeable = value)
  def isCloseable: WindowModel[A, ReferenceData] =
    withCloseable(true)
  def notCloseable: WindowModel[A, ReferenceData] =
    withCloseable(false)

  def withFocus(value: Boolean): WindowModel[A, ReferenceData] =
    this.copy(hasFocus = value)
  def focus: WindowModel[A, ReferenceData] =
    withFocus(true)
  def blur: WindowModel[A, ReferenceData] =
    withFocus(false)

  def withStatic(value: Boolean): WindowModel[A, ReferenceData] =
    this.copy(static = value)
  def isStatic: WindowModel[A, ReferenceData] =
    withStatic(true)
  def notStatic: WindowModel[A, ReferenceData] =
    withStatic(false)

  def withMinSize(min: Dimensions): WindowModel[A, ReferenceData] =
    this.copy(minSize = Option(min))
  def withMinSize(width: Int, height: Int): WindowModel[A, ReferenceData] =
    this.copy(minSize = Option(Dimensions(width, height)))
  def noMinSize: WindowModel[A, ReferenceData] =
    this.copy(minSize = None)

  def withMaxSize(max: Dimensions): WindowModel[A, ReferenceData] =
    this.copy(maxSize = Option(max))
  def withMaxSize(width: Int, height: Int): WindowModel[A, ReferenceData] =
    this.copy(maxSize = Option(Dimensions(width, height)))
  def noMaxSize: WindowModel[A, ReferenceData] =
    this.copy(maxSize = None)

  def withState(value: WindowState): WindowModel[A, ReferenceData] =
    this.copy(state = value)
  def open: WindowModel[A, ReferenceData] =
    withState(WindowState.Open)
  def close: WindowModel[A, ReferenceData] =
    withState(WindowState.Closed)

  def isOpen: Boolean =
    state == WindowState.Open
  def isClosed: Boolean =
    state == WindowState.Closed

  def refresh: WindowModel[A, ReferenceData] =
    this.copy(contentModel = windowContent.refresh(contentModel))

object WindowModel:

  def apply[A, ReferenceData](
      id: WindowId,
      charSheet: CharSheet,
      content: A
  )(using c: WindowContent[A, ReferenceData]): WindowModel[A, ReferenceData] =
    WindowModel(
      id,
      charSheet,
      Bounds(Coords.zero, Dimensions.zero),
      None,
      content,
      c,
      false,
      false,
      false,
      false,
      false,
      None,
      None,
      WindowState.Closed
    )
