package demo.models

import demo.Assets
import indigo.*
import roguelikestarterkit.*
import roguelikestarterkit.ui.components.ComponentGroup
import roguelikestarterkit.ui.components.ComponentList
import roguelikestarterkit.ui.components.datatypes.BoundsMode
import roguelikestarterkit.ui.components.datatypes.ComponentId

final case class Model(pointerOverWindows: Batch[WindowId], components: ComponentGroup[Int])

object Model:

  import indigo.syntax.*

  val defaultCharSheet: CharSheet =
    CharSheet(
      Assets.assets.AnikkiSquare10x10,
      Size(10),
      RoguelikeTiles.Size10x10.charCrops,
      RoguelikeTiles.Size10x10.Fonts.fontKey
    )

  val initial: Model =
    Model(
      Batch.empty,
      ComponentGroup(BoundsMode.fixed(200, 200))
        .add(
          ComponentList(Dimensions(200, 40)) { (_: Int) =>
            (1 to 3).toBatch.map { i =>
              ComponentId("lbl" + i) -> Label[Int](
                "Custom rendered label " + i,
                (_, label) => Bounds(0, 0, 150, 10)
              ) { case (offset, label, dimensions) =>
                Outcome(
                  Layer(
                    TextBox(label)
                      .withColor(RGBA.Red)
                      .moveTo(offset.unsafeToPoint)
                      .withSize(dimensions.unsafeToSize)
                  )
                )
              }
            }
          }
        )
        .add(
          Label[Int](
            "Another label",
            (_, label) => Bounds(0, 0, 150, 10)
          ) { case (offset, label, dimensions) =>
            Outcome(
              Layer(
                TextBox(label)
                  .withColor(RGBA.White)
                  .moveTo(offset.unsafeToPoint)
                  .withSize(dimensions.unsafeToSize)
              )
            )
          }
        )
    )
