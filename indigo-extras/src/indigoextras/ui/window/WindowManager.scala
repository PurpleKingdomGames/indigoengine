package indigoextras.ui.window

import indigo.*
import indigoextras.ui.datatypes.UIContext
import indigoextras.ui.datatypes.UIState

final case class WindowManager[StartUpData, Model, RefData](
    id: SubSystemId,
    initialMagnification: Magnification,
    snapGrid: Size,
    extractReference: Model => RefData,
    startUpData: StartUpData,
    layerKey: LayerKey,
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

  /** Registers a window with the WindowManager. All Window's must be registered before the scene starts.
    */
  def register(
      newWindows: Window[?, ReferenceData]*
  ): WindowManager[StartUpData, Model, ReferenceData] =
    register(Batch.fromSeq(newWindows))
  def register(
      newWindows: Batch[Window[?, ReferenceData]]
  ): WindowManager[StartUpData, Model, ReferenceData] =
    this.copy(windows = windows ++ newWindows)

  /** Sets which windows are initially open. Once the scene is running, opening and closing is managed by the
    * WindowManagerModel via events.
    */
  def open(ids: WindowId*): WindowManager[StartUpData, Model, ReferenceData] =
    open(Batch.fromSeq(ids))
  def open(ids: Batch[WindowId]): WindowManager[StartUpData, Model, ReferenceData] =
    this.copy(windows = windows.map(w => if ids.exists(_ == w.id) then w.open else w))

  /** Sets which window is initially focused. Once the scene is running, focusing is managed by the WindowManagerModel
    * via events.
    */
  def focus(id: WindowId): WindowManager[StartUpData, Model, ReferenceData] =
    val reordered =
      windows.find(w => w.isOpen && w.id == id) match
        case None =>
          windows

        case Some(w) =>
          windows.filterNot(_.id == w.id).map(_.blur) :+ w.focus

    this.copy(windows = reordered)

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

  /** Allows you to set the layer key that the WindowManager will use to present the windows.
    */
  def withLayerKey(newLayerKey: LayerKey): WindowManager[StartUpData, Model, ReferenceData] =
    this.copy(layerKey = newLayerKey)

object WindowManager:

  /** Creates a WindowManager instance with no snap grid, that respects the magnification specified.
    */
  def apply[Model](id: SubSystemId, layerKey: LayerKey): WindowManager[Unit, Model, Unit] =
    WindowManager(id, Magnification.x1, Size(1), _ => (), (), layerKey, Batch.empty)

  /** Creates a WindowManager instance with no snap grid, that respects the magnification specified.
    */
  def apply[Model](
      id: SubSystemId,
      layerKey: LayerKey,
      magnification: Magnification
  ): WindowManager[Unit, Model, Unit] =
    WindowManager(id, magnification, Size(1), _ => (), (), layerKey, Batch.empty)

  /** Creates a WindowManager instance with no snap grid, that respects the magnification specified.
    */
  def apply[Model](
      id: SubSystemId,
      layerKey: LayerKey,
      magnification: Magnification,
      snapGrid: Size
  ): WindowManager[Unit, Model, Unit] =
    WindowManager(id, magnification, snapGrid, _ => (), (), layerKey, Batch.empty)

  def apply[Model, ReferenceData](
      id: SubSystemId,
      layerKey: LayerKey,
      magnification: Magnification,
      snapGrid: Size,
      extractReference: Model => ReferenceData
  ): WindowManager[Unit, Model, ReferenceData] =
    WindowManager(id, magnification, snapGrid, extractReference, (), layerKey, Batch.empty)

  def apply[StartUpData, Model, ReferenceData](
      id: SubSystemId,
      layerKey: LayerKey,
      magnification: Magnification,
      snapGrid: Size,
      extractReference: Model => ReferenceData,
      startUpData: StartUpData
  ): WindowManager[StartUpData, Model, ReferenceData] =
    WindowManager(
      id,
      magnification,
      snapGrid,
      extractReference,
      startUpData,
      layerKey,
      Batch.empty
    )

  private def modalWindowOpen[ReferenceData](
      model: WindowManagerModel[ReferenceData]
  ): Option[WindowId] =
    model.windows.find(w => w.isOpen && w.mode == WindowMode.Modal).map(_.id)

  private[window] def updateModel[ReferenceData](
      context: UIContext[ReferenceData],
      model: WindowManagerModel[ReferenceData]
  ): GlobalEvent => Outcome[WindowManagerModel[ReferenceData]] = e =>
    val outcome = e match
      case e: WindowEvent =>
        modalWindowOpen(model) match
          case None =>
            handleWindowEvents(context, model)(e)

          case modelId =>
            if e.isNotification || modelId == e.windowId then handleWindowEvents(context, model)(e)
            else Outcome(model)

      case e: PointerEvent.Down =>
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

    attachWindowEvents(outcome, model)

  private def updateWindows[ReferenceData](
      context: UIContext[ReferenceData],
      model: WindowManagerModel[ReferenceData],
      modalWindow: Option[WindowId]
  ): GlobalEvent => Outcome[WindowManagerModel[ReferenceData]] =
    e =>
      val windowUnderPointer =
        model.windowAt(context.pointerCoords, context.frame.viewport, context.magnification)

      model.windows
        .map { w =>
          val windowActive = w.activeCheck(context)

          val ctx =
            context.withState(calculateUIState(w, modalWindow, windowUnderPointer))

          val windowUpdateFocus =
            if w.hasFocus && windowActive.isInActive then w.blur
            else w

          Window.updateModel(ctx, windowUpdateFocus)(e)
        }
        .sequence
        .map(m => model.copy(windows = m))

  private def handleWindowEvents[ReferenceData](
      context: UIContext[ReferenceData],
      model: WindowManagerModel[ReferenceData]
  ): WindowEvent => Outcome[WindowManagerModel[ReferenceData]] =
    case WindowEvent.Refresh(id) =>
      model.refresh(context, id)

    case WindowEvent.Focus(id) =>
      Outcome(model.focusOn(id))

    case WindowEvent.GiveFocusAt(position) =>
      Outcome(model.focusAt(position, context))
        .addGlobalEvents(WindowInternalEvent.Redraw)

    case WindowEvent.Open(id) =>
      Outcome(model.open(id).focusOn(id))

    case WindowEvent.OpenAt(id, coords) =>
      Outcome(
        model
          .open(id)
          .moveTo(id, coords, Space.Screen, context.frame.viewport, context.magnification)
          .focusOn(id)
      )

    case WindowEvent.Close(id) =>
      Outcome(model.close(id))

    case WindowEvent.Toggle(id) =>
      Outcome(model.toggle(id))

    case WindowEvent.Move(id, coords, space) =>
      Outcome(model.moveTo(id, coords, space, context.frame.viewport, context.magnification))

    case WindowEvent.Anchor(id, anchor) =>
      Outcome(model.anchor(id, anchor))

    case WindowEvent.Resize(id, dimensions, space) =>
      model.resizeTo(id, dimensions, space, context.frame.viewport, context.magnification).refresh(context, id)

    case WindowEvent.Transform(id, bounds, space) =>
      model.transformTo(id, bounds, space, context.frame.viewport, context.magnification).refresh(context, id)

    case WindowEvent.CloseFocused =>
      model.focused match
        case None =>
          Outcome(model)

        case Some(window) =>
          Outcome(model.close(window.id))

    case WindowEvent.ChangeMagnification(_) =>
      Outcome(model)

    // Pass through notification type events so that windows can deal with them
    case e: (WindowEvent.Opened | WindowEvent.Closed | WindowEvent.Focused | WindowEvent.Blurred | WindowEvent.Resized |
          WindowEvent.PointerOver | WindowEvent.PointerOut | WindowEvent.MagnificationChanged) =>
      updateWindows(context, model, modalWindowOpen(model))(e)

  private[window] def updateViewModel[ReferenceData](
      context: UIContext[ReferenceData],
      model: WindowManagerModel[ReferenceData],
      viewModel: WindowManagerViewModel[ReferenceData]
  ): GlobalEvent => Outcome[WindowManagerViewModel[ReferenceData]] =
    case WindowEvent.ChangeMagnification(next) =>
      Outcome(viewModel.changeMagnification(next))
        .addGlobalEvents(
          if next.toInt != viewModel.magnification.toInt then Batch(WindowEvent.MagnificationChanged(next))
          else Batch.empty
        )

    case e =>
      val modalWindow = modalWindowOpen(model)
      val windowUnderPointer =
        model.windowAt(context.pointerCoords, context.frame.viewport, context.magnification)

      val updated =
        val prunedVM = viewModel.prune(model)
        model.windows.flatMap { m =>
          if m.isClosed then Batch.empty
          else
            prunedVM.windows.find(_.id == m.id) match
              case None =>
                Batch(Outcome(WindowViewModel.initial(m.id)))

              case Some(vm) =>
                Batch(
                  vm.update(
                    context.withState(calculateUIState(m, modalWindow, windowUnderPointer)),
                    m,
                    e
                  )
                )
        }

      updated.sequence.map(vm => viewModel.copy(windows = vm))

  private[window] def present[ReferenceData](
      layerKey: LayerKey,
      context: UIContext[ReferenceData],
      model: WindowManagerModel[ReferenceData],
      viewModel: WindowManagerViewModel[ReferenceData]
  ): Outcome[SceneUpdateFragment] =
    val modalWindow        = modalWindowOpen(model)
    val windowUnderPointer = model.windowAt(context.pointerCoords, context.frame.viewport, context.magnification)

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
                    context.withState(calculateUIState(m, modalWindow, windowUnderPointer)),
                    m,
                    vm
                  )
              )
        }
        .sequence

    windowLayers.map { layers =>
      SceneUpdateFragment(
        LayerEntry(layerKey, Layer.Stack(layers))
          .withMagnification(context.magnification)
      )
    }

  private def calculateUIState[ReferenceData](
      window: Window[?, ReferenceData],
      modalWindowId: Option[WindowId],
      windowUnderPointerId: Option[WindowId]
  ): UIState =
    // Closed windows are always inactive
    if window.isClosed then UIState.InActive
    else
      modalWindowId match
        case Some(id) if id == window.id =>
          if window.hasFocus then UIState.Focused else UIState.Active

        case Some(_) =>
          UIState.InActive

        case None =>
          if window.hasFocus then UIState.Focused
          else if windowUnderPointerId.exists(_ == window.id) then UIState.Active
          else UIState.InActive

  private def attachWindowEvents[ReferenceData](
      outcome: Outcome[WindowManagerModel[ReferenceData]],
      prevModel: WindowManagerModel[ReferenceData]
  ): Outcome[WindowManagerModel[ReferenceData]] =

    outcome match
      case Outcome.Error(_, _) =>
        outcome

      case Outcome.Result(model, globalEvents) =>
        Outcome.Result(model, windowEvents(prevModel, model) ++ globalEvents)

  private def windowEvents[ReferenceData](
      prevModel: WindowManagerModel[ReferenceData],
      model: WindowManagerModel[ReferenceData]
  ): Batch[WindowEvent] =
    val prevOpenIds    = prevModel.windows.filter(_.isOpen).map(_.id).toSet
    val currentOpenIds = model.windows.filter(_.isOpen).map(_.id).toSet

    val prevFocusIds    = prevModel.windows.filter(_.hasFocus).map(_.id).toSet
    val currentFocusIds = model.windows.filter(_.hasFocus).map(_.id).toSet

    val opened =
      Batch.fromSet(
        currentOpenIds
          .filterNot(prevOpenIds.contains)
          .map(WindowEvent.Opened.apply)
      )

    val focusChanged =
      Batch.fromSet(
        prevFocusIds
          .filterNot(currentFocusIds.contains)
          .map(w => WindowEvent.Blurred(w))
      ) ++
        Batch.fromSet(
          currentFocusIds
            .filterNot(prevFocusIds.contains)
            .map(w => WindowEvent.Focused(w))
        )

    val closed =
      Batch.fromSet(
        prevOpenIds
          .filterNot(currentOpenIds.contains)
          .map(WindowEvent.Closed.apply)
      )

    opened ++ focusChanged ++ closed

final case class ModelHolder[ReferenceData](
    model: WindowManagerModel[ReferenceData],
    viewModel: WindowManagerViewModel[ReferenceData]
)
object ModelHolder:
  def initial[ReferenceData](
      windows: Batch[Window[?, ReferenceData]],
      magnification: Magnification
  ): ModelHolder[ReferenceData] =
    ModelHolder(
      WindowManagerModel.initial.register(windows),
      WindowManagerViewModel.initial(magnification)
    )
