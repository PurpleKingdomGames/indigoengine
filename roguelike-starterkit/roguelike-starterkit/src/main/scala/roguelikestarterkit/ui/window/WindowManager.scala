package roguelikestarterkit.ui.window

import indigo.*
import indigo.shared.FrameContext
import roguelikestarterkit.ui.datatypes.CharSheet
import roguelikestarterkit.ui.datatypes.UiContext

// TODO: Bring back startUpData as a type param.
final case class WindowManager(
    id: SubSystemId,
    magnification: Int,
    charSheet: CharSheet,
    windows: Batch[WindowModel[Unit, Unit, _]]
) extends SubSystem:
  type EventType      = GlobalEvent
  type SubSystemModel = ModelHolder

  def eventFilter: GlobalEvent => Option[GlobalEvent] =
    e => Some(e)

  def initialModel: Outcome[ModelHolder] =
    Outcome(
      ModelHolder.initial(windows)
    )

  def update(
      context: SubSystemFrameContext,
      model: ModelHolder
  ): GlobalEvent => Outcome[ModelHolder] =
    e =>
      for {
        updatedModel <- WindowManager.updateModel[Unit, Unit](
          UiContext(toFrameContext(context), charSheet),
          model.model
        )(e)

        updatedViewModel <-
          WindowManager.updateViewModel[Unit, Unit](
            UiContext(toFrameContext(context), charSheet),
            updatedModel,
            model.viewModel
          )(e)
      } yield ModelHolder(updatedModel, updatedViewModel)

  def present(
      context: SubSystemFrameContext,
      model: ModelHolder
  ): Outcome[SceneUpdateFragment] =
    WindowManager.present(
      UiContext(toFrameContext(context), charSheet),
      magnification,
      model.model,
      model.viewModel
    )

  def register(windowModels: WindowModel[Unit, Unit, _]*): WindowManager =
    register(Batch.fromSeq(windowModels))
  def register(
      windowModels: Batch[WindowModel[Unit, Unit, _]]
  ): WindowManager =
    this.copy(windows = windows ++ windowModels)

  private def toFrameContext(context: SubSystemFrameContext): FrameContext[Unit] =
    FrameContext(
      context.gameTime,
      context.dice,
      context.inputState,
      context.boundaryLocator,
      ()
    )

final case class ModelHolder(
    model: WindowManagerModel[Unit, Unit],
    viewModel: WindowManagerViewModel[Unit, Unit]
)
object ModelHolder:
  def initial(
      windows: Batch[WindowModel[Unit, Unit, _]]
  ): ModelHolder =
    ModelHolder(
      WindowManagerModel.initial[Unit, Unit].register(windows),
      WindowManagerViewModel.initial[Unit, Unit]
    )

object WindowManager:

  def apply(id: SubSystemId, magnification: Int, charSheet: CharSheet): WindowManager =
    WindowManager(id, magnification, charSheet, Batch.empty)

  def updateModel[StartupData, A](
      context: UiContext,
      model: WindowManagerModel[StartupData, A]
  ): GlobalEvent => Outcome[WindowManagerModel[StartupData, A]] =
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

  def updateViewModel[StartupData, A](
      context: UiContext,
      model: WindowManagerModel[StartupData, A],
      viewModel: WindowManagerViewModel[StartupData, A]
  ): GlobalEvent => Outcome[WindowManagerViewModel[StartupData, A]] =
    case e =>
      val updated =
        val prunedVM = viewModel.prune(model)
        model.windows.flatMap { m =>
          if m.isClosed then Batch.empty
          else
            prunedVM.windows.find(_.id == m.id) match
              case None =>
                Batch(Outcome(WindowViewModel.initial(m.id)))

              case Some(vm) =>
                Batch(vm.update(context, m, e))
        }

      updated.sequence.map(vm => viewModel.copy(windows = vm))

  def present[StartupData, A](
      context: UiContext,
      globalMagnification: Int,
      model: WindowManagerModel[StartupData, A],
      viewModel: WindowManagerViewModel[StartupData, A]
  ): Outcome[SceneUpdateFragment] =
    model.windows
      .filter(_.isOpen)
      .flatMap { m =>
        viewModel.windows.find(_.id == m.id) match
          case None =>
            // Shouldn't get here.
            Batch.empty

          case Some(vm) =>
            Batch(Window.present(context, globalMagnification, m, vm))
      }
      .sequence
      .map(
        _.foldLeft(SceneUpdateFragment.empty)(_ |+| _)
      )
