package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.terminal.TerminalMaterial
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Bounds.dimensions
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions
import roguelikestarterkit.ui.datatypes.UiContext
import roguelikestarterkit.ui.shaders.LayerMask

object Window:

  private val graphic: Graphic[TerminalMaterial] =
    Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  def updateModel[A, ReferenceData](
      context: UiContext[ReferenceData],
      model: WindowModel[A, ReferenceData]
  ): GlobalEvent => Outcome[WindowModel[A, ReferenceData]] =
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
        model.withDimensions(model.bounds.dimensions + (dragData.by - dragData.offset).toDimensions)
      ).addGlobalEvents(WindowEvent.Resized(id))

    case e =>
      val contentRectangle = calculateContentRectangle(model.bounds, model)

      model.windowContent
        .updateModel(context.copy(bounds = contentRectangle), model.contentModel)(e)
        .map(model.withModel)

  def calculateDragBy(charSize: Int, mousePosition: Point, windowPosition: Coords): Coords =
    Coords(mousePosition / charSize) - windowPosition

  def calculateContentRectangle[A, ReferenceData](
      workingBounds: Bounds,
      model: WindowModel[A, ReferenceData]
  ): Bounds =
    if model.title.isDefined then
      workingBounds
        .resize((workingBounds.dimensions - Dimensions(2, 4)).max(Dimensions.zero))
        .moveTo(workingBounds.coords + Coords(1, 3))
    else
      workingBounds
        .resize((workingBounds.dimensions - Dimensions(2, 2)).max(Dimensions.zero))
        .moveTo(workingBounds.coords + Coords(1, 1))

  def redraw[A, ReferenceData](
      context: UiContext[ReferenceData],
      model: WindowModel[A, ReferenceData],
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

    val vm = viewModel.resize(tempModel)
    val clones =
      vm.terminal.toCloneTiles(
        CloneId("window_tile"),
        tempModel.bounds.coords.toScreenSpace(model.charSheet.size),
        model.charSheet.charCrops
      ) { case (fg, bg) =>
        graphic.withMaterial(TerminalMaterial(model.charSheet.assetName, fg, bg))
      }

    val contentRectangle =
      calculateContentRectangle(tempModel.bounds, model)

    vm.copy(
      terminalClones = clones,
      modelHashCode = model.bounds.hashCode(),
      contentRectangle = contentRectangle
    )

  def updateViewModel[A, ReferenceData](
      context: UiContext[ReferenceData],
      model: WindowModel[A, ReferenceData],
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
      val focus =
        if actionsAllowed && !model.static && model.bounds
            .resize(model.bounds.dimensions - 1)
            .contains(gridPos)
        then Batch(WindowEvent.GiveFocusAt(gridPos))
        else Batch.empty

      Outcome(viewModel)
        .addGlobalEvents(close ++ focus)

    case e: MouseEvent.MouseDown
        if model.draggable &&
          viewModel.dragData.isEmpty &&
          model.bounds.withDimensions(model.bounds.width, 3).contains(context.mouseCoords) &&
          context.mouseCoords != model.bounds.topRight + Coords(-1, 0) =>
      val d = calculateDragBy(model.charSheet.charSize, e.position, model.bounds.coords)

      Outcome(viewModel.copy(dragData = Option(DragData(d, d))))
        .addGlobalEvents(WindowEvent.GiveFocusAt(context.mouseCoords))

    case e: MouseEvent.MouseDown
        if model.resizable &&
          viewModel.resizeData.isEmpty &&
          model.bounds.bottomRight - Coords(1) == (context.mouseCoords) =>
      val d = calculateDragBy(model.charSheet.charSize, e.position, model.bounds.coords)

      Outcome(viewModel.copy(resizeData = Option(DragData(d, d))))
        .addGlobalEvents(WindowEvent.GiveFocusAt(context.mouseCoords))

    case e: MouseEvent.MouseUp if viewModel.dragData.isDefined =>
      Outcome(viewModel)
        .addGlobalEvents(
          WindowInternalEvent.MoveBy(
            model.id,
            viewModel.dragData
              .map(
                _.copy(by =
                  calculateDragBy(model.charSheet.charSize, e.position, model.bounds.coords)
                )
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
                _.copy(by =
                  calculateDragBy(model.charSheet.charSize, e.position, model.bounds.coords)
                )
              )
              .getOrElse(DragData.zero)
          ),
          WindowInternalEvent.ClearData
        )

    case e: MouseEvent.Move if viewModel.dragData.isDefined =>
      Outcome(
        viewModel.copy(
          dragData = viewModel.dragData.map(
            _.copy(by = calculateDragBy(model.charSheet.charSize, e.position, model.bounds.coords))
          )
        )
      )

    case e: MouseEvent.Move if viewModel.resizeData.isDefined =>
      Outcome(
        viewModel.copy(
          resizeData = viewModel.resizeData.map(
            _.copy(by = calculateDragBy(model.charSheet.charSize, e.position, model.bounds.coords))
          )
        )
      )

    case MouseEvent.Move(pt)
        if viewModel.mouseIsOver && !model.bounds
          .toScreenSpace(context.charSheet.size)
          .contains(pt) =>
      Outcome(viewModel.copy(mouseIsOver = false))
        .addGlobalEvents(WindowEvent.MouseOut(model.id))

    case MouseEvent.Move(pt)
        if !viewModel.mouseIsOver && model.bounds
          .toScreenSpace(context.charSheet.size)
          .contains(pt) =>
      Outcome(viewModel.copy(mouseIsOver = true))
        .addGlobalEvents(WindowEvent.MouseOver(model.id))

    case _ =>
      Outcome(viewModel)

  def present[A, ReferenceData](
      context: UiContext[ReferenceData],
      model: WindowModel[A, ReferenceData],
      viewModel: WindowViewModel[ReferenceData]
  ): Outcome[Layer] =
    model.windowContent
      .present(
        context.copy(bounds = viewModel.contentRectangle),
        model.contentModel
      )
      .map {
        case l: Layer.Content =>
          Layer.Stack(
            Layer
              .Content(viewModel.terminalClones.clones)
              .addCloneBlanks(viewModel.terminalClones.blanks),
            l.withBlendMaterial(
              LayerMask(
                viewModel.contentRectangle
                  .toScreenSpace(context.charSheet.size * viewModel.magnification)
              )
            )
          )

        case l: Layer.Stack =>
          val masked =
            l.layers.map {
              case l: Layer.Content =>
                l.withBlendMaterial(
                  LayerMask(
                    viewModel.contentRectangle
                      .toScreenSpace(context.charSheet.size * viewModel.magnification)
                  )
                )

              case l =>
                l
            }

          Layer.Stack(
            Layer
              .Content(viewModel.terminalClones.clones)
              .addCloneBlanks(viewModel.terminalClones.blanks) :: masked
          )
      }
