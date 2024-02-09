package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions
import roguelikestarterkit.ui.datatypes.UiContext

final case class WindowManagerModel[StartupData, A](windows: Batch[WindowModel[StartupData, A, _]]):
  def add(windowModels: WindowModel[StartupData, A, _]*): WindowManagerModel[StartupData, A] =
    add(Batch.fromSeq(windowModels))
  def add(windowModels: Batch[WindowModel[StartupData, A, _]]): WindowManagerModel[StartupData, A] =
    this.copy(windows = windows ++ windowModels)

  def remove(id: WindowId): WindowManagerModel[StartupData, A] =
    this.copy(windows = windows.filterNot(_.id == id))

  def giveFocusAndSurfaceAt(coords: Coords): WindowManagerModel[StartupData, A] =
    val reordered =
      windows.reverse.find(w => !w.static && w.bounds.contains(coords)) match
        case None =>
          windows

        case Some(w) =>
          windows.filterNot(_.id == w.id).map(_.blur) :+ w.focus

    this.copy(windows = reordered)

  def update(
      context: UiContext[StartupData, A],
      event: GlobalEvent
  ): Outcome[WindowManagerModel[StartupData, A]] =
    WindowManager.updateModel(context, this)(event)

object WindowManagerModel:
  def initial[StartupData, A]: WindowManagerModel[StartupData, A] =
    WindowManagerModel(Batch.empty)
