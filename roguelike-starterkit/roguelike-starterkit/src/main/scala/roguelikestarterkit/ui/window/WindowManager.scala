package roguelikestarterkit.ui.window

import indigo.*
import indigo.shared.FrameContext
import roguelikestarterkit.ui.datatypes.CharSheet
import roguelikestarterkit.ui.datatypes.UiContext

final case class WindowManager[StartUpData](
    id: SubSystemId,
    initialMagnification: Int,
    charSheet: CharSheet,
    startUpData: StartUpData,
    windows: Batch[WindowModel[_]]
) extends SubSystem:
  type EventType      = GlobalEvent
  type SubSystemModel = ModelHolder

  def eventFilter: GlobalEvent => Option[GlobalEvent] =
    e => Some(e)

  def initialModel: Outcome[ModelHolder] =
    Outcome(
      ModelHolder.initial(windows, initialMagnification)
    )

  def update(
      context: SubSystemFrameContext,
      model: ModelHolder
  ): GlobalEvent => Outcome[ModelHolder] =
    e =>
      for {
        updatedModel <- WindowManager.updateModel[Unit](
          UiContext(context, charSheet),
          model.model
        )(e)

        updatedViewModel <-
          WindowManager.updateViewModel[Unit](
            UiContext(context, charSheet),
            updatedModel,
            model.viewModel
          )(e)
      } yield ModelHolder(updatedModel, updatedViewModel)

  def present(
      context: SubSystemFrameContext,
      model: ModelHolder
  ): Outcome[SceneUpdateFragment] =
    WindowManager.present(
      UiContext(context, charSheet),
      model.model,
      model.viewModel
    )

  def register(windowModels: WindowModel[_]*): WindowManager[StartUpData] =
    register(Batch.fromSeq(windowModels))
  def register(
      windowModels: Batch[WindowModel[_]]
  ): WindowManager[StartUpData] =
    this.copy(windows = windows ++ windowModels)

final case class ModelHolder(
    model: WindowManagerModel,
    viewModel: WindowManagerViewModel
)
object ModelHolder:
  def initial(
      windows: Batch[WindowModel[_]],
      magnification: Int
  ): ModelHolder =
    ModelHolder(
      WindowManagerModel.initial.register(windows),
      WindowManagerViewModel.initial(magnification)
    )

object WindowManager:

  def apply(id: SubSystemId, magnification: Int, charSheet: CharSheet): WindowManager[Unit] =
    WindowManager(id, magnification, charSheet, (), Batch.empty)

  def apply[StartUpData](
      id: SubSystemId,
      magnification: Int,
      charSheet: CharSheet,
      startUpData: StartUpData
  ): WindowManager[StartUpData] =
    WindowManager(id, magnification, charSheet, startUpData, Batch.empty)

  def updateModel[A](
      context: UiContext,
      model: WindowManagerModel
  ): GlobalEvent => Outcome[WindowManagerModel] =
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

  def updateViewModel[A](
      context: UiContext,
      model: WindowManagerModel,
      viewModel: WindowManagerViewModel
  ): GlobalEvent => Outcome[WindowManagerViewModel] =
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

  def present[A](
      context: UiContext,
      model: WindowManagerModel,
      viewModel: WindowManagerViewModel
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
