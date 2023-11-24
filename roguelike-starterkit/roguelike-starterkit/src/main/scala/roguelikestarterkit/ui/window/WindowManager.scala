package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.datatypes.UiContext

object WindowManager:

  def updateModel[StartupData, A](
      context: UiContext[StartupData, A],
      model: WindowManagerModel[StartupData, A]
  ): GlobalEvent => Outcome[WindowManagerModel[StartupData, A]] =
    case WindowManagerEvent.Close(id) =>
      Outcome(model.remove(id))

    case WindowManagerEvent.GiveFocusAt(position) =>
      Outcome(model.giveFocusAndSurfaceAt(position))
        .addGlobalEvents(WindowEvent.Redraw)

    case e =>
      model.windows
        .map(w => Window.updateModel(context, w)(e))
        .sequence
        .map(m => model.copy(windows = m))

  def updateViewModel[StartupData, A](
      context: UiContext[StartupData, A],
      model: WindowManagerModel[StartupData, A],
      viewModel: WindowManagerViewModel[StartupData, A]
  ): GlobalEvent => Outcome[WindowManagerViewModel[StartupData, A]] =
    case e =>
      val updated =
        model.windows.flatMap { m =>
          viewModel.prune(model).windows.find(_.id == m.id) match
            case None =>
              Batch(Outcome(WindowViewModel.initial(m.id)))

            case Some(vm) =>
              Batch(vm.update(context, m, e))
        }

      updated.sequence.map(vm => viewModel.copy(windows = vm))

  def present[StartupData, A](
      context: UiContext[StartupData, A],
      model: WindowManagerModel[StartupData, A],
      viewModel: WindowManagerViewModel[StartupData, A]
  ): Outcome[SceneUpdateFragment] =
    model.windows
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
