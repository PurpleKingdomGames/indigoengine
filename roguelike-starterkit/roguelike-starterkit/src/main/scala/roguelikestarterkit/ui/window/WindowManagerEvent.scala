package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.datatypes.Coords

enum WindowManagerEvent extends GlobalEvent:
  case Close(id: WindowId)
  case GiveFocusAt(coords: Coords)
