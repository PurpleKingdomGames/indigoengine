package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.datatypes.UiContext

final case class WindowManagerViewModel[StartupData, A](windows: Batch[WindowViewModel]):
  def prune(model: WindowManagerModel[StartupData, A]): WindowManagerViewModel[StartupData, A] =
    this.copy(windows = windows.filter(w => model.windows.exists(_.id == w.id)))

  def update(
      context: UiContext[StartupData, A],
      model: WindowManagerModel[StartupData, A],
      event: GlobalEvent
  ): Outcome[WindowManagerViewModel[StartupData, A]] =
    WindowManager.updateViewModel(context, model, this)(event)

object WindowManagerViewModel:
  def initial[StartupData, A]: WindowManagerViewModel[StartupData, A] =
    WindowManagerViewModel(Batch.empty)
