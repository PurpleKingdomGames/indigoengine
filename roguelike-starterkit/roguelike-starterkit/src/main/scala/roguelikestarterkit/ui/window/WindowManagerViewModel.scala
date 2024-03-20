package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.datatypes.UiContext

final case class WindowManagerViewModel[StartupData, A](windows: Batch[WindowViewModel]):
  def prune(model: WindowManagerModel[StartupData, A]): WindowManagerViewModel[StartupData, A] =
    this.copy(windows = windows.filter(w => model.windows.exists(_.id == w.id)))

  def update(
      context: UiContext,
      model: WindowManagerModel[StartupData, A],
      event: GlobalEvent
  ): Outcome[WindowManagerViewModel[StartupData, A]] =
    WindowManager.updateViewModel(context, model, this)(event)

  def mouseIsOverAnyWindow: Boolean =
    windows.exists(_.mouseIsOver)

  def mouseIsOver: Batch[WindowId] =
    windows.collect { case wvm if wvm.mouseIsOver => wvm.id }

object WindowManagerViewModel:
  def initial[StartupData, A]: WindowManagerViewModel[StartupData, A] =
    WindowManagerViewModel(Batch.empty)
