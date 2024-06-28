package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Bounds.dimensions
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions
import roguelikestarterkit.ui.datatypes.UIContext
import roguelikestarterkit.ui.shaders.LayerMask

object WindowView:

  def present[A, ReferenceData](
      context: UIContext[ReferenceData],
      model: Window[A, ReferenceData],
      viewModel: WindowViewModel[ReferenceData]
  ): Outcome[Layer] =
    model.windowContent
      .present(
        context.copy(bounds = viewModel.contentRectangle),
        model.contentModel
      )
      .flatMap {
        case l: Layer.Content =>
          model.present(context, model, viewModel).map { windowChrome =>
            Layer.Stack(
              windowChrome,
              l.withBlendMaterial(
                LayerMask(
                  viewModel.contentRectangle
                    .toScreenSpace(context.snapGrid * viewModel.magnification)
                )
              )
            )
          }

        case l: Layer.Stack =>
          val masked =
            l.layers.map {
              case l: Layer.Content =>
                l.withBlendMaterial(
                  LayerMask(
                    viewModel.contentRectangle
                      .toScreenSpace(context.snapGrid * viewModel.magnification)
                  )
                )

              case l =>
                l
            }

          model.present(context, model, viewModel).map { windowChrome =>
            Layer.Stack(
              windowChrome :: masked
            )
          }
      }

  def calculateContentRectangle[A, ReferenceData](
      workingBounds: Bounds,
      model: Window[A, ReferenceData]
  ): Bounds =
    if model.title.isDefined then
      workingBounds
        .resize((workingBounds.dimensions - Dimensions(2, 4)).max(Dimensions.zero))
        .moveTo(workingBounds.coords + Coords(1, 3))
    else
      workingBounds
        .resize((workingBounds.dimensions - Dimensions(2, 2)).max(Dimensions.zero))
        .moveTo(workingBounds.coords + Coords(1, 1))
