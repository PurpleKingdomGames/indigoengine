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
      false,
      magnification
    )

  def updateViewModel[A, ReferenceData](
      context: UIContext[ReferenceData],
      model: Window[A, ReferenceData],
      viewModel: WindowViewModel[ReferenceData]
  ): GlobalEvent => Outcome[WindowViewModel[ReferenceData]] =
    case FrameTick if model.bounds.hashCode() != viewModel.modelHashCode =>
      Outcome(redraw(context, model, viewModel))

    case WindowInternalEvent.Redraw =>
      Outcome(redraw(context, model, viewModel))

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

  private def calculateDragBy(
      charSize: Size,
      mousePosition: Point,
      windowPosition: Coords
  ): Coords =
    Coords(mousePosition / charSize.toPoint) - windowPosition

  private def redraw[A, ReferenceData](
      context: UIContext[ReferenceData],
      model: Window[A, ReferenceData],
      viewModel: WindowViewModel[ReferenceData]
  ): WindowViewModel[ReferenceData] =
    viewModel.copy(
      modelHashCode = model.bounds.hashCode()
    )
