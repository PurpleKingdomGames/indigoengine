package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.component.Component
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
    model.component
      .present(
        context.copy(bounds = model.bounds),
        model.content
      )
      .map(_.toLayer)
      .flatMap {
        case l: Layer.Content =>
          model.background(WindowContext.from(model, viewModel)).map { windowChrome =>
            Layer.Stack(
              windowChrome,
              l.withBlendMaterial(
                LayerMask(
                  (model.bounds + Bounds(
                    model.mask.left,
                    model.mask.top,
                    -model.mask.right,
                    -model.mask.bottom
                  ))
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
                    (model.bounds + Bounds(
                      model.mask.left,
                      model.mask.top,
                      -model.mask.right,
                      -model.mask.bottom
                    ))
                      .toScreenSpace(context.snapGrid * viewModel.magnification)
                  )
                )

              case l =>
                l
            }

          model.background(WindowContext.from(model, viewModel)).map { windowChrome =>
            Layer.Stack(
              windowChrome :: masked
            )
          }
      }
