package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.datatypes.Coords

enum WindowEvent extends GlobalEvent:
  case MoveBy(id: WindowId, dragData: DragData)
  case MoveTo(id: WindowId, position: Coords)
  case ResizeBy(id: WindowId, dragData: DragData)
  case Redraw
  case ClearData
