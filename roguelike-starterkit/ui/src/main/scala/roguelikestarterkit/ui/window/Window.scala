package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions
import roguelikestarterkit.ui.datatypes.UIContext

final case class Window[A, ReferenceData](
    id: WindowId,
    snapGrid: Size,
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
    state: WindowState,
    present: (UIContext[ReferenceData], Window[A, ReferenceData]) => Outcome[Layer]
):

  lazy val minAllowedSize: Dimensions =
    val m = if title.isEmpty then Dimensions(3) else Dimensions(3, 5)
    minSize.fold(m)(_.max(m))

  def withId(value: WindowId): Window[A, ReferenceData] =
    this.copy(id = value)

  def withBounds(value: Bounds): Window[A, ReferenceData] =
    this.copy(bounds = value)

  def withPosition(value: Coords): Window[A, ReferenceData] =
    withBounds(bounds.moveTo(value))
  def moveTo(position: Coords): Window[A, ReferenceData] =
    withPosition(position)
  def moveTo(x: Int, y: Int): Window[A, ReferenceData] =
    moveTo(Coords(x, y))
  def moveBy(amount: Coords): Window[A, ReferenceData] =
    withPosition(bounds.coords + amount)
  def moveBy(x: Int, y: Int): Window[A, ReferenceData] =
    moveBy(Coords(x, y))

  def withDimensions(value: Dimensions): Window[A, ReferenceData] =
    val d = value.max(minAllowedSize)
    withBounds(bounds.withDimensions(maxSize.fold(d)(_.min(d))))
  def resizeTo(size: Dimensions): Window[A, ReferenceData] =
    withDimensions(size)
  def resizeTo(x: Int, y: Int): Window[A, ReferenceData] =
    resizeTo(Dimensions(x, y))
  def resizeBy(amount: Dimensions): Window[A, ReferenceData] =
    withDimensions(bounds.dimensions + amount)
  def resizeBy(x: Int, y: Int): Window[A, ReferenceData] =
    resizeBy(Dimensions(x, y))

  def withTitle(value: String): Window[A, ReferenceData] =
    this.copy(title = Option(value))

  def withModel(value: A): Window[A, ReferenceData] =
    this.copy(contentModel = value)

  def withDraggable(value: Boolean): Window[A, ReferenceData] =
    this.copy(draggable = value)
  def isDraggable: Window[A, ReferenceData] =
    withDraggable(true)
  def notDraggable: Window[A, ReferenceData] =
    withDraggable(false)

  def withResizable(value: Boolean): Window[A, ReferenceData] =
    this.copy(resizable = value)
  def isResizable: Window[A, ReferenceData] =
    withResizable(true)
  def notResizable: Window[A, ReferenceData] =
    withResizable(false)

  def withCloseable(value: Boolean): Window[A, ReferenceData] =
    this.copy(closeable = value)
  def isCloseable: Window[A, ReferenceData] =
    withCloseable(true)
  def notCloseable: Window[A, ReferenceData] =
    withCloseable(false)

  def withFocus(value: Boolean): Window[A, ReferenceData] =
    this.copy(hasFocus = value)
  def focus: Window[A, ReferenceData] =
    withFocus(true)
  def blur: Window[A, ReferenceData] =
    withFocus(false)

  def withStatic(value: Boolean): Window[A, ReferenceData] =
    this.copy(static = value)
  def isStatic: Window[A, ReferenceData] =
    withStatic(true)
  def notStatic: Window[A, ReferenceData] =
    withStatic(false)

  def withMinSize(min: Dimensions): Window[A, ReferenceData] =
    this.copy(minSize = Option(min))
  def withMinSize(width: Int, height: Int): Window[A, ReferenceData] =
    this.copy(minSize = Option(Dimensions(width, height)))
  def noMinSize: Window[A, ReferenceData] =
    this.copy(minSize = None)

  def withMaxSize(max: Dimensions): Window[A, ReferenceData] =
    this.copy(maxSize = Option(max))
  def withMaxSize(width: Int, height: Int): Window[A, ReferenceData] =
    this.copy(maxSize = Option(Dimensions(width, height)))
  def noMaxSize: Window[A, ReferenceData] =
    this.copy(maxSize = None)

  def withState(value: WindowState): Window[A, ReferenceData] =
    this.copy(state = value)
  def open: Window[A, ReferenceData] =
    withState(WindowState.Open)
  def close: Window[A, ReferenceData] =
    withState(WindowState.Closed)

  def isOpen: Boolean =
    state == WindowState.Open
  def isClosed: Boolean =
    state == WindowState.Closed

  def refresh(reference: ReferenceData): Window[A, ReferenceData] =
    this.copy(contentModel =
      windowContent.refresh(
        reference,
        contentModel,
        WindowView.calculateContentRectangle(bounds, this).dimensions
      )
    )

object Window:

  def apply[A, ReferenceData](
      id: WindowId,
      snapGrid: Size,
      content: A
  )(
      present: (UIContext[ReferenceData], Window[A, ReferenceData]) => Outcome[Layer]
  )(using c: WindowContent[A, ReferenceData]): Window[A, ReferenceData] =
    Window(
      id,
      snapGrid,
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
      WindowState.Closed,
      present
    )

  def updateModel[A, ReferenceData](
      context: UIContext[ReferenceData],
      model: Window[A, ReferenceData]
  ): GlobalEvent => Outcome[Window[A, ReferenceData]] =
    case WindowInternalEvent.MoveBy(id, dragData) if model.id == id =>
      Outcome(
        model.copy(
          bounds = model.bounds.moveBy(dragData.by - dragData.offset)
        )
      )

    case WindowInternalEvent.MoveTo(id, position) if model.id == id =>
      Outcome(
        model.copy(
          bounds = model.bounds.moveTo(position)
        )
      )

    case WindowInternalEvent.ResizeBy(id, dragData) if model.id == id =>
      Outcome(
        model
          .withDimensions(model.bounds.dimensions + (dragData.by - dragData.offset).toDimensions)
          .refresh(context.reference)
      ).addGlobalEvents(WindowEvent.Resized(id))

    case e =>
      val contentRectangle = WindowView.calculateContentRectangle(model.bounds, model)

      model.windowContent
        .updateModel(context.copy(bounds = contentRectangle), model.contentModel)(e)
        .map(model.withModel)
