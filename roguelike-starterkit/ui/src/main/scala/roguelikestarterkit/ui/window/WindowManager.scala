package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.UIContext
import roguelikestarterkit.ui.datatypes.UIState

final case class WindowManager[StartUpData, Model, RefData](
    id: SubSystemId,
    initialMagnification: Int,
    snapGrid: Size,
    extractReference: Model => RefData,
    startUpData: StartUpData,
    layerKey: Option[BindingKey],
    windows: Batch[Window[?, RefData]]
) extends SubSystem[Model]:
  type EventType      = GlobalEvent
  type ReferenceData  = RefData
  type SubSystemModel = ModelHolder[ReferenceData]

  def eventFilter: GlobalEvent => Option[GlobalEvent] =
    e => Some(e)

  def reference(model: Model): ReferenceData =
    extractReference(model)

  def initialModel: Outcome[ModelHolder[ReferenceData]] =
    Outcome(
      ModelHolder.initial(windows, initialMagnification)
    )

  def update(
      context: SubSystemContext[ReferenceData],
      model: ModelHolder[ReferenceData]
  ): GlobalEvent => Outcome[ModelHolder[ReferenceData]] =
    e =>
      for {
        updatedModel <- WindowManager.updateModel[ReferenceData](
          UIContext(context, snapGrid, model.viewModel.magnification),
          model.model
        )(e)

        updatedViewModel <-
          WindowManager.updateViewModel[ReferenceData](
            UIContext(context, snapGrid, model.viewModel.magnification),
            updatedModel,
            model.viewModel
          )(e)
      } yield ModelHolder(updatedModel, updatedViewModel)

  def present(
      context: SubSystemContext[ReferenceData],
      model: ModelHolder[ReferenceData]
  ): Outcome[SceneUpdateFragment] =
    WindowManager.present(
      layerKey,
      UIContext(context, snapGrid, model.viewModel.magnification),
      model.model,
      model.viewModel
    )

  def register(
      windowModels: Window[?, ReferenceData]*
  ): WindowManager[StartUpData, Model, ReferenceData] =
    register(Batch.fromSeq(windowModels))
  def register(
      windowModels: Batch[Window[?, ReferenceData]]
  ): WindowManager[StartUpData, Model, ReferenceData] =
    this.copy(windows = windows ++ windowModels)

  def open(ids: WindowId*): WindowManager[StartUpData, Model, ReferenceData] =
    open(Batch.fromSeq(ids))
  def open(ids: Batch[WindowId]): WindowManager[StartUpData, Model, ReferenceData] =
    this.copy(windows = windows.map(w => if ids.exists(_ == w.id) then w.open else w))

  def withStartupData[A](newStartupData: A): WindowManager[A, Model, ReferenceData] =
    WindowManager(
      id,
      initialMagnification,
      snapGrid,
      extractReference,
      newStartupData,
      layerKey,
      windows
    )

  def withLayerKey(newLayerKey: BindingKey): WindowManager[StartUpData, Model, ReferenceData] =
    this.copy(layerKey = Option(newLayerKey))

object WindowManager:

  def apply[Model, ReferenceData](
      id: SubSystemId,
      magnification: Int,
      snapGrid: Size,
      extractReference: Model => ReferenceData
  ): WindowManager[Unit, Model, ReferenceData] =
    WindowManager(id, magnification, snapGrid, extractReference, (), None, Batch.empty)

  def apply[StartUpData, Model, ReferenceData](
      id: SubSystemId,
      magnification: Int,
      snapGrid: Size,
      extractReference: Model => ReferenceData,
      startUpData: StartUpData
  ): WindowManager[StartUpData, Model, ReferenceData] =
    WindowManager(id, magnification, snapGrid, extractReference, startUpData, None, Batch.empty)

  def apply[StartUpData, Model, ReferenceData](
      id: SubSystemId,
      magnification: Int,
      snapGrid: Size,
      extractReference: Model => ReferenceData,
      startUpData: StartUpData,
      layerKey: BindingKey
  ): WindowManager[StartUpData, Model, ReferenceData] =
    WindowManager(
      id,
      magnification,
      snapGrid,
      extractReference,
      startUpData,
      Option(layerKey),
      Batch.empty
    )

  private def modalWindowOpen[ReferenceData](
      model: WindowManagerModel[ReferenceData]
  ): Option[WindowId] =
    model.windows.find(w => w.isOpen && w.mode == WindowMode.Modal).map(_.id)

  private[window] def updateModel[ReferenceData](
      context: UIContext[ReferenceData],
      model: WindowManagerModel[ReferenceData]
  ): GlobalEvent => Outcome[WindowManagerModel[ReferenceData]] =
    case e: WindowEvent =>
      modalWindowOpen(model) match
        case None =>
          handleWindowEvents(context, model)(e)

        case modelId =>
          if modelId == e.windowId then handleWindowEvents(context, model)(e)
          else Outcome(model)

    case e: PointerEvent.PointerClick =>
      updateWindows(context, model, modalWindowOpen(model))(e)
        .addGlobalEvents(WindowEvent.GiveFocusAt(context.pointerCoords))

    case FrameTick =>
      modalWindowOpen(model) match
        case None =>
          updateWindows(context, model, None)(FrameTick)

        case _id @ Some(id) =>
          updateWindows(context, model, _id)(FrameTick).map(_.focusOn(id))

    case e =>
      updateWindows(context, model, modalWindowOpen(model))(e)

  private def updateWindows[ReferenceData](
      context: UIContext[ReferenceData],
      model: WindowManagerModel[ReferenceData],
      modalWindow: Option[WindowId]
  ): GlobalEvent => Outcome[WindowManagerModel[ReferenceData]] =
    e =>
      val windowUnderPointer = model.windowAt(context.pointerCoords)

      model.windows
        .map { w =>
          Window.updateModel(
            context.copy(state = modalWindow match
              case Some(id) if id == w.id =>
                UIState.Active

              case Some(_) =>
                UIState.InActive

              case None =>
                if w.hasFocus || windowUnderPointer.exists(_ == w.id) then UIState.Active
                else UIState.InActive),
            w
          )(e)
        }
        .sequence
        .map(m => model.copy(windows = m))

  private def handleWindowEvents[ReferenceData](
      context: UIContext[ReferenceData],
      model: WindowManagerModel[ReferenceData]
  ): WindowEvent => Outcome[WindowManagerModel[ReferenceData]] =
    case WindowEvent.Refresh(id) =>
      model.refresh(id, context.reference)

    case WindowEvent.Focus(id) =>
      Outcome(model.focusOn(id))

    case WindowEvent.GiveFocusAt(position) =>
      Outcome(model.focusAt(position))
        .addGlobalEvents(WindowInternalEvent.Redraw)

    case WindowEvent.Open(id) =>
      model.open(id).addGlobalEvents(WindowEvent.Focus(id))

    case WindowEvent.OpenAt(id, coords) =>
      model
        .open(id)
        .map(_.moveTo(id, coords, Space.Screen))
        .addGlobalEvents(WindowEvent.Focus(id))

    case WindowEvent.Close(id) =>
      model.close(id)

    case WindowEvent.Toggle(id) =>
      model.toggle(id)

    case WindowEvent.Move(id, coords, space) =>
      Outcome(model.moveTo(id, coords, space))

    case WindowEvent.Resize(id, dimensions, space) =>
      model.resizeTo(id, dimensions, space).refresh(id, context.reference)

    case WindowEvent.Transform(id, bounds, space) =>
      model.transformTo(id, bounds, space).refresh(id, context.reference)

    case WindowEvent.Opened(_) =>
      Outcome(model)

    case WindowEvent.Closed(_) =>
      Outcome(model)

    case WindowEvent.Resized(_) =>
      Outcome(model)

    case WindowEvent.PointerOver(_) =>
      Outcome(model)

    case WindowEvent.PointerOut(_) =>
      Outcome(model)

    case WindowEvent.ChangeMagnification(_) =>
      Outcome(model)

  private[window] def updateViewModel[ReferenceData](
      context: UIContext[ReferenceData],
      model: WindowManagerModel[ReferenceData],
      viewModel: WindowManagerViewModel[ReferenceData]
  ): GlobalEvent => Outcome[WindowManagerViewModel[ReferenceData]] =
    case WindowEvent.ChangeMagnification(next) =>
      Outcome(viewModel.changeMagnification(next))

    case e =>
      val windowUnderPointer = model.windowAt(context.pointerCoords)

      val updated =
        val prunedVM = viewModel.prune(model)
        model.windows.flatMap { m =>
          if m.isClosed then Batch.empty
          else
            prunedVM.windows.find(_.id == m.id) match
              case None =>
                Batch(Outcome(WindowViewModel.initial(m.id, viewModel.magnification)))

              case Some(vm) =>
                Batch(
                  vm.update(
                    context.copy(state =
                      if m.hasFocus || windowUnderPointer.exists(_ == m.id) then UIState.Active
                      else UIState.InActive
                    ),
                    m,
                    e
                  )
                )
        }

      updated.sequence.map(vm => viewModel.copy(windows = vm))

  private[window] def present[ReferenceData](
      layerKey: Option[BindingKey],
      context: UIContext[ReferenceData],
      model: WindowManagerModel[ReferenceData],
      viewModel: WindowManagerViewModel[ReferenceData]
  ): Outcome[SceneUpdateFragment] =
    val windowUnderPointer = model.windowAt(context.pointerCoords)

    val windowLayers: Outcome[Batch[Layer]] =
      model.windows
        .filter(_.isOpen)
        .flatMap { m =>
          viewModel.windows.find(_.id == m.id) match
            case None =>
              // Shouldn't get here.
              Batch.empty

            case Some(vm) =>
              Batch(
                WindowView
                  .present(
                    context.copy(state =
                      if m.hasFocus || windowUnderPointer.exists(_ == m.id) then UIState.Active
                      else UIState.InActive
                    ),
                    m,
                    vm
                  )
              )
        }
        .sequence

    windowLayers.map { layers =>
      layerKey match
        case None =>
          SceneUpdateFragment(
            LayerEntry(Layer.Stack(layers))
          )

        case Some(key) =>
          SceneUpdateFragment(
            LayerEntry(key -> Layer.Stack(layers))
          )
    }

final case class ModelHolder[ReferenceData](
    model: WindowManagerModel[ReferenceData],
    viewModel: WindowManagerViewModel[ReferenceData]
)
object ModelHolder:
  def initial[ReferenceData](
      windows: Batch[Window[?, ReferenceData]],
      magnification: Int
  ): ModelHolder[ReferenceData] =
    ModelHolder(
      WindowManagerModel.initial.register(windows),
      WindowManagerViewModel.initial(magnification)
    )
