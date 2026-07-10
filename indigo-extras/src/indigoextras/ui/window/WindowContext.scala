package indigoextras.ui.window

import indigo.core.render.Magnification
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.UIContext

final case class WindowContext[ReferenceData](
    context: UIContext[ReferenceData],
    bounds: Bounds,
    hasFocus: Boolean,
    pointerIsOver: Boolean,
    magnification: Magnification
)

object WindowContext:

  def from[ReferenceData](
      context: UIContext[ReferenceData],
      model: Window[?, ?],
      viewModel: WindowViewModel[?]
  ): WindowContext[ReferenceData] =
    WindowContext(
      context,
      model.bounds(context.frame.viewport, context.magnification),
      model.hasFocus,
      viewModel.pointerIsOver,
      context.magnification
    )
