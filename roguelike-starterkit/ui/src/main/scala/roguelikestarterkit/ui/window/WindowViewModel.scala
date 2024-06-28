package roguelikestarterkit.ui.window

import indigo.*
import indigo.syntax.*
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions
import roguelikestarterkit.ui.datatypes.UIContext

final case class WindowViewModel[ReferenceData](
    id: WindowId,
    modelHashCode: Int,
    contentRectangle: Bounds,
    dragData: Option[DragData],
    resizeData: Option[DragData],
    mouseIsOver: Boolean,
    magnification: Int
):

  def update[A](
      context: UIContext[ReferenceData],
      model: Window[A, ReferenceData],
      event: GlobalEvent
  ): Outcome[WindowViewModel[ReferenceData]] =
    WindowViewModel.updateViewModel(context, model, this)(event)

object WindowViewModel:

  def initial[ReferenceData](id: WindowId, magnification: Int): WindowViewModel[ReferenceData] =
    WindowViewModel(
      id,
      0,
      Bounds.zero,
      None,
      None,
      false,
      magnification
    )

  def updateViewModel[A, ReferenceData](
      context: UIContext[ReferenceData],
      model: Window[A, ReferenceData],
      viewModel: WindowViewModel[ReferenceData]
  ): GlobalEvent => Outcome[WindowViewModel[ReferenceData]] =
    case FrameTick
        if model.bounds.hashCode() != viewModel.modelHashCode ||
          viewModel.dragData.isDefined ||
          viewModel.resizeData.isDefined =>
      Outcome(redraw(context, model, viewModel))

    case WindowInternalEvent.Redraw =>
      Outcome(redraw(context, model, viewModel))

    case WindowInternalEvent.ClearData =>
      Outcome(viewModel.copy(resizeData = None, dragData = None))

    case e: MouseEvent.Click =>
      val gridPos = context.mouseCoords

      val actionsAllowed = viewModel.dragData.isEmpty && viewModel.resizeData.isEmpty

      val close =
        if actionsAllowed && model.closeable && gridPos == model.bounds.topRight + Coords(-1, 0)
        then Batch(WindowEvent.Close(model.id))
        else Batch.empty

      Outcome(viewModel)
        .addGlobalEvents(close)

    case e: MouseEvent.MouseDown
        if context.isActive && model.draggable &&
          viewModel.dragData.isEmpty &&
          model.bounds.withDimensions(model.bounds.width, 3).contains(context.mouseCoords) &&
          context.mouseCoords != model.bounds.topRight + Coords(-1, 0) =>
      val d = calculateDragBy(model.snapGrid, e.position, model.bounds.coords)

      Outcome(viewModel.copy(dragData = Option(DragData(d, d))))

    case e: MouseEvent.MouseDown
        if context.isActive && model.resizable &&
          viewModel.resizeData.isEmpty &&
          model.bounds.bottomRight - Coords(1) == (context.mouseCoords) =>
      val d = calculateDragBy(model.snapGrid, e.position, model.bounds.coords)

      Outcome(viewModel.copy(resizeData = Option(DragData(d, d))))

    case e: MouseEvent.MouseUp if viewModel.dragData.isDefined =>
      Outcome(viewModel)
        .addGlobalEvents(
          WindowInternalEvent.MoveBy(
            model.id,
            viewModel.dragData
              .map(
                _.copy(by = calculateDragBy(model.snapGrid, e.position, model.bounds.coords))
              )
              .getOrElse(DragData.zero)
          ),
          WindowInternalEvent.ClearData
        )

    case e: MouseEvent.MouseUp if viewModel.resizeData.isDefined =>
      Outcome(viewModel)
        .addGlobalEvents(
          WindowInternalEvent.ResizeBy(
            model.id,
            viewModel.resizeData
              .map(
                _.copy(by = calculateDragBy(model.snapGrid, e.position, model.bounds.coords))
              )
              .getOrElse(DragData.zero)
          ),
          WindowInternalEvent.ClearData
        )

    case e: MouseEvent.Move if viewModel.dragData.isDefined =>
      Outcome(
        viewModel.copy(
          dragData = viewModel.dragData.map(
            _.copy(by = calculateDragBy(model.snapGrid, e.position, model.bounds.coords))
          )
        )
      )

    case e: MouseEvent.Move if viewModel.resizeData.isDefined =>
      Outcome(
        viewModel.copy(
          resizeData = viewModel.resizeData.map(
            _.copy(by = calculateDragBy(model.snapGrid, e.position, model.bounds.coords))
          )
        )
      )

    case MouseEvent.Move(pt)
        if viewModel.mouseIsOver && !model.bounds
          .toScreenSpace(context.snapGrid)
          .contains(pt) =>
      Outcome(viewModel.copy(mouseIsOver = false))
        .addGlobalEvents(WindowEvent.MouseOut(model.id))

    case MouseEvent.Move(pt)
        if !viewModel.mouseIsOver && model.bounds
          .toScreenSpace(context.snapGrid)
          .contains(pt) =>
      Outcome(viewModel.copy(mouseIsOver = true))
        .addGlobalEvents(WindowEvent.MouseOver(model.id))

    case _ =>
      Outcome(viewModel)

  def calculateDragBy(charSize: Size, mousePosition: Point, windowPosition: Coords): Coords =
    Coords(mousePosition / charSize.toPoint) - windowPosition

  def redraw[A, ReferenceData](
      context: UIContext[ReferenceData],
      model: Window[A, ReferenceData],
      viewModel: WindowViewModel[ReferenceData]
  ): WindowViewModel[ReferenceData] =
    val tempModel =
      model
        .withDimensions(
          model.bounds.dimensions + viewModel.resizeData
            .map(d => d.by - d.offset)
            .getOrElse(Coords.zero)
            .toDimensions
        )
        .moveBy(
          viewModel.dragData
            .map(d => d.by - d.offset)
            .getOrElse(Coords.zero)
        )

    val contentRectangle =
      WindowView.calculateContentRectangle(tempModel.bounds, model)

    viewModel.copy(
      modelHashCode = model.bounds.hashCode(),
      contentRectangle = contentRectangle
    )
