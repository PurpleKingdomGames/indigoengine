package roguelikestarterkit.ui.components.group

import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions

/** Describes how a ComponentGroup responds to changes in its parents bounds.
  */
final case class BoundsType(width: FitMode, height: FitMode)

object BoundsType:

  val default: BoundsType =
    BoundsType(FitMode.Available, FitMode.Content)

  def fixed(dimensions: Dimensions): BoundsType =
    BoundsType(
      FitMode.Fixed(dimensions.width),
      FitMode.Fixed(dimensions.height)
    )

  def fixed(width: Int, height: Int): BoundsType =
    fixed(Dimensions(width, height))

  def inherit: BoundsType =
    BoundsType(FitMode.Available, FitMode.Available)

  def fit: BoundsType =
    BoundsType(FitMode.Content, FitMode.Content)

  def halfHorizontal: BoundsType =
    BoundsType(
      FitMode.Relative(0.5),
      FitMode.Available
    )

  def halfVertical: BoundsType =
    BoundsType(
      FitMode.Available,
      FitMode.Relative(0.5)
    )

  def relative(relativeWidth: Double, relativeHeight: Double): BoundsType =
    BoundsType(
      FitMode.Relative(relativeWidth),
      FitMode.Relative(relativeHeight)
    )
