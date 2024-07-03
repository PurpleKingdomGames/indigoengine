package roguelikestarterkit.ui.components.common

import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions

enum Anchor:
  case None
  case TopLeft
  case TopCenter
  case TopRight
  case CenterLeft
  case Center
  case CenterRight
  case BottomLeft
  case BottomCenter
  case BottomRight

  def position(area: Dimensions, component: Dimensions): Coords =
    Anchor.position(this, area, component)

object Anchor:

  def position(anchor: Anchor, area: Dimensions, component: Dimensions): Coords =
    anchor match
      case Anchor.None =>
        Coords.zero

      case Anchor.TopLeft =>
        Coords.zero

      case Anchor.TopCenter =>
        Coords((area.width - component.width) / 2, 0)

      case Anchor.TopRight =>
        Coords(area.width - component.width, 0)

      case Anchor.CenterLeft =>
        Coords(0, (area.height - component.height) / 2)

      case Anchor.Center =>
        Coords((area.width - component.width) / 2, (area.height - component.height) / 2)

      case Anchor.CenterRight =>
        Coords(area.width - component.width, (area.height - component.height) / 2)

      case Anchor.BottomLeft =>
        Coords(0, area.height - component.height)

      case Anchor.BottomCenter =>
        Coords((area.width - component.width) / 2, area.height - component.height)

      case Anchor.BottomRight =>
        Coords(area.width - component.width, area.height - component.height)
