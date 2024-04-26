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

object WindowManager:

  def apply[Model, ReferenceData](
      id: SubSystemId,
      magnification: Int,
      charSheet: CharSheet,
      extractReference: Model => ReferenceData
  ): WindowManager[Unit, Model, ReferenceData] =
    WindowManager(id, magnification, charSheet, extractReference, (), Batch.empty)

  def apply[StartUpData, Model, ReferenceData](
      id: SubSystemId,
      magnification: Int,
      charSheet: CharSheet,
      extractReference: Model => ReferenceData,
      startUpData: StartUpData
  ): WindowManager[StartUpData, Model, ReferenceData] =
    WindowManager(id, magnification, charSheet, extractReference, startUpData, Batch.empty)

  def updateModel[ReferenceData](
      context: UiContext[ReferenceData],
      model: WindowManagerModel[ReferenceData]
  ): GlobalEvent => Outcome[WindowManagerModel[ReferenceData]] =
    case WindowManagerEvent.Close(id) =>
      Outcome(model.close(id))

    case WindowManagerEvent.GiveFocusAt(position) =>
      Outcome(model.giveFocusAndSurfaceAt(position))
        .addGlobalEvents(WindowEvent.Redraw)

    case WindowManagerEvent.Open(id) =>
      Outcome(model.open(id))

    case WindowManagerEvent.OpenAt(id, coords) =>
      Outcome(model.open(id).moveTo(id, coords))

    case WindowManagerEvent.Move(id, coords) =>
      Outcome(model.moveTo(id, coords))

    case WindowManagerEvent.Resize(id, dimensions) =>
      Outcome(model.resizeTo(id, dimensions))

    case WindowManagerEvent.Transform(id, bounds) =>
      Outcome(model.transformTo(id, bounds))

    case e =>
      model.windows
        .map(w => if w.isOpen then Window.updateModel(context, w)(e) else Outcome(w))
        .sequence
        .map(m => model.copy(windows = m))

  def updateViewModel[ReferenceData](
      context: UiContext[ReferenceData],
      model: WindowManagerModel[ReferenceData],
      viewModel: WindowManagerViewModel[ReferenceData]
  ): GlobalEvent => Outcome[WindowManagerViewModel[ReferenceData]] =
    case WindowManagerEvent.ChangeMagnification(next) =>
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

  def present[ReferenceData](
      context: UiContext[ReferenceData],
      model: WindowManagerModel[ReferenceData],
      viewModel: WindowManagerViewModel[ReferenceData]
  ): Outcome[SceneUpdateFragment] =
    model.windows
      .filter(_.isOpen)
      .flatMap { m =>
        viewModel.windows.find(_.id == m.id) match
          case None =>
            // Shouldn't get here.
            Batch.empty

          case Some(vm) =>
            Batch(Window.present(context, m, vm))
      }
      .sequence
      .map(
        _.foldLeft(SceneUpdateFragment.empty)(_ |+| _)
      )
