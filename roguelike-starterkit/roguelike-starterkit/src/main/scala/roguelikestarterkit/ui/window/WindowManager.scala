package roguelikestarterkit.ui.window

import indigo.*
import indigo.shared.FrameContext
import roguelikestarterkit.ui.datatypes.CharSheet
import roguelikestarterkit.ui.datatypes.UiContext

final case class WindowManager[StartUpData, Model, RefData](
    id: SubSystemId,
    initialMagnification: Int,
    charSheet: CharSheet,
    extractReference: Model => RefData,
    startUpData: StartUpData,
    layerKey: Option[BindingKey],
    windows: Batch[WindowModel[?, RefData]]
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
      context: SubSystemFrameContext[ReferenceData],
      model: ModelHolder[ReferenceData]
  ): GlobalEvent => Outcome[ModelHolder[ReferenceData]] =
    e =>
      for {
        updatedModel <- WindowManager.updateModel[ReferenceData](
          UiContext(context, charSheet),
          model.model
        )(e)

        updatedViewModel <-
          WindowManager.updateViewModel[ReferenceData](
            UiContext(context, charSheet),
            updatedModel,
            model.viewModel
          )(e)
      } yield ModelHolder(updatedModel, updatedViewModel)

  def present(
      context: SubSystemFrameContext[ReferenceData],
      model: ModelHolder[ReferenceData]
  ): Outcome[SceneUpdateFragment] =
    WindowManager.present(
      layerKey,
      UiContext(context, charSheet),
      model.model,
      model.viewModel
    )

  def register(
      windowModels: WindowModel[?, ReferenceData]*
  ): WindowManager[StartUpData, Model, ReferenceData] =
    register(Batch.fromSeq(windowModels))
  def register(
      windowModels: Batch[WindowModel[?, ReferenceData]]
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
      charSheet,
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
      charSheet: CharSheet,
      extractReference: Model => ReferenceData
  ): WindowManager[Unit, Model, ReferenceData] =
    WindowManager(id, magnification, charSheet, extractReference, (), None, Batch.empty)

  def apply[StartUpData, Model, ReferenceData](
      id: SubSystemId,
      magnification: Int,
      charSheet: CharSheet,
      extractReference: Model => ReferenceData,
      startUpData: StartUpData
  ): WindowManager[StartUpData, Model, ReferenceData] =
    WindowManager(id, magnification, charSheet, extractReference, startUpData, None, Batch.empty)

  def apply[StartUpData, Model, ReferenceData](
      id: SubSystemId,
      magnification: Int,
      charSheet: CharSheet,
      extractReference: Model => ReferenceData,
      startUpData: StartUpData,
      layerKey: BindingKey
  ): WindowManager[StartUpData, Model, ReferenceData] =
    WindowManager(
      id,
      magnification,
      charSheet,
      extractReference,
      startUpData,
      Option(layerKey),
      Batch.empty
    )

  private[window] def updateModel[ReferenceData](
      context: UiContext[ReferenceData],
      model: WindowManagerModel[ReferenceData]
  ): GlobalEvent => Outcome[WindowManagerModel[ReferenceData]] =
    case e: WindowEvent =>
      handleWindowEvents(context, model)(e)

    case e =>
      model.windows
        .map(w => if w.isOpen then Window.updateModel(context, w)(e) else Outcome(w))
        .sequence
        .map(m => model.copy(windows = m))

  private def handleWindowEvents[ReferenceData](
      context: UiContext[ReferenceData],
      model: WindowManagerModel[ReferenceData]
  ): WindowEvent => Outcome[WindowManagerModel[ReferenceData]] =
    case WindowEvent.Refresh(id) =>
      model.refresh(id)

    case WindowEvent.GiveFocusAt(position) =>
      Outcome(model.giveFocusAndSurfaceAt(position))
        .addGlobalEvents(WindowInternalEvent.Redraw)

    case WindowEvent.Open(id) =>
      model.open(id)

    case WindowEvent.OpenAt(id, coords) =>
      model.open(id).map(_.moveTo(id, coords))

    case WindowEvent.Close(id) =>
      model.close(id)

    case WindowEvent.Toggle(id) =>
      model.toggle(id)

    case WindowEvent.Move(id, coords) =>
      Outcome(model.moveTo(id, coords))

    case WindowEvent.Resize(id, dimensions) =>
      Outcome(model.resizeTo(id, dimensions))

    case WindowEvent.Transform(id, bounds) =>
      Outcome(model.transformTo(id, bounds))

    case WindowEvent.Opened(_) =>
      Outcome(model)

    case WindowEvent.Closed(_) =>
      Outcome(model)

    case WindowEvent.Resized(_) =>
      Outcome(model)

    case WindowEvent.MouseOver(_) =>
      Outcome(model)

    case WindowEvent.MouseOut(_) =>
      Outcome(model)

    case WindowEvent.ChangeMagnification(_) =>
      Outcome(model)

  private[window] def updateViewModel[ReferenceData](
      context: UiContext[ReferenceData],
      model: WindowManagerModel[ReferenceData],
      viewModel: WindowManagerViewModel[ReferenceData]
  ): GlobalEvent => Outcome[WindowManagerViewModel[ReferenceData]] =
    case WindowEvent.ChangeMagnification(next) =>
      Outcome(viewModel.changeMagnification(next))

    case e =>
      val updated =
        val prunedVM = viewModel.prune(model)
        model.windows.flatMap { m =>
          if m.isClosed then Batch.empty
          else
            prunedVM.windows.find(_.id == m.id) match
              case None =>
                Batch(Outcome(WindowViewModel.initial(m.id, viewModel.magnification)))

              case Some(vm) =>
                Batch(vm.update(context, m, e))
        }

      updated.sequence.map(vm => viewModel.copy(windows = vm))

  private[window] def present[ReferenceData](
      layerKey: Option[BindingKey],
      context: UiContext[ReferenceData],
      model: WindowManagerModel[ReferenceData],
      viewModel: WindowManagerViewModel[ReferenceData]
  ): Outcome[SceneUpdateFragment] =
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
                Window
                  .present(context, m, vm)
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
      windows: Batch[WindowModel[?, ReferenceData]],
      magnification: Int
  ): ModelHolder[ReferenceData] =
    ModelHolder(
      WindowManagerModel.initial.register(windows),
      WindowManagerViewModel.initial(magnification)
    )
