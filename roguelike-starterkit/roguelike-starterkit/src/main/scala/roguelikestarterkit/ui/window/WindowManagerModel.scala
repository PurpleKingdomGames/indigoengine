package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions
import roguelikestarterkit.ui.datatypes.UiContext

final case class WindowManagerModel[ReferenceData](windows: Batch[WindowModel[?, ReferenceData]]):
  def register(windowModels: WindowModel[?, ReferenceData]*): WindowManagerModel[ReferenceData] =
    register(Batch.fromSeq(windowModels))
  def register(
      windowModels: Batch[WindowModel[?, ReferenceData]]
  ): WindowManagerModel[ReferenceData] =
    this.copy(windows = windows ++ windowModels)

  def open(ids: WindowId*): WindowManagerModel[ReferenceData] =
    open(Batch.fromSeq(ids))

  def open(ids: Batch[WindowId]): WindowManagerModel[ReferenceData] =
    this.copy(windows = windows.map(w => if ids.exists(_ == w.id) then w.open else w))

  def close(id: WindowId): WindowManagerModel[ReferenceData] =
    this.copy(windows = windows.map(w => if w.id == id then w.close else w))

  def giveFocusAndSurfaceAt(coords: Coords): WindowManagerModel[ReferenceData] =
    val reordered =
      windows.reverse.find(w => !w.static && w.bounds.contains(coords)) match
        case None =>
          windows

        case Some(w) =>
          windows.filterNot(_.id == w.id).map(_.blur) :+ w.focus

    this.copy(windows = reordered)

  def moveTo(id: WindowId, position: Coords): WindowManagerModel[ReferenceData] =
    this.copy(windows = windows.map(w => if w.id == id then w.moveTo(position) else w))

  def resizeTo(id: WindowId, dimensions: Dimensions): WindowManagerModel[ReferenceData] =
    this.copy(windows = windows.map(w => if w.id == id then w.resizeTo(dimensions) else w))

  def transformTo(id: WindowId, bounds: Bounds): WindowManagerModel[ReferenceData] =
    this.copy(windows =
      windows.map(w =>
        // Note: We do _not_ use .withBounds here because that won't do the min size checks.
        if w.id == id then w.moveTo(bounds.coords).resizeTo(bounds.dimensions) else w
      )
    )

  def update(
      context: UiContext[ReferenceData],
      event: GlobalEvent
  ): Outcome[WindowManagerModel[ReferenceData]] =
    WindowManager.updateModel(context, this)(event)

object WindowManagerModel:
  def initial[ReferenceData]: WindowManagerModel[ReferenceData] =
    WindowManagerModel(Batch.empty)
