package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions

enum WindowEvent extends GlobalEvent:

  // Events sent to the game

  /** Informs the game when the mouse moves into a window's bounds */
  case MouseOver(id: WindowId)

  /** Informs the game when the mouse moves out of a window's bounds */
  case MouseOut(id: WindowId)

  /** Informs the game when a window has resized */
  case Resized(id: WindowId)

  /** Informs the game when a window has opened */
  case Opened(id: WindowId)

  /** Informs the game when a window has closed */
  case Closed(id: WindowId)

  // User sent events

  /** Tells a window to open */
  case Open(id: WindowId)

  /** Tells a window to open at a specific location */
  case OpenAt(id: WindowId, coords: Coords)

  /** Tells a window to close */
  case Close(id: WindowId)

  /** Tells a window to toggle between open and closed. */
  case Toggle(id: WindowId)

  /** Focuses the top window at the given location */
  case GiveFocusAt(coords: Coords)

  /** Moves a window to the location given */
  case Move(id: WindowId, position: Coords, space: Space)

  /** Resizes a window to a given size */
  case Resize(id: WindowId, dimensions: Dimensions, space: Space)

  /** Changes the bounds of a window */
  case Transform(id: WindowId, bounds: Bounds, space: Space)

  /** Changes the magnification of all windows */
  case ChangeMagnification(newMagnification: Int)

  /** Tells a window request its content to refresh */
  case Refresh(id: WindowId)

  def windowId: Option[WindowId] =
    this match
      case MouseOver(id)          => Some(id)
      case MouseOut(id)           => Some(id)
      case Resized(id)            => Some(id)
      case Opened(id)             => Some(id)
      case Closed(id)             => Some(id)
      case Open(id)               => Some(id)
      case OpenAt(id, _)          => Some(id)
      case Close(id)              => Some(id)
      case Toggle(id)             => Some(id)
      case Move(id, _, _)         => Some(id)
      case Resize(id, _, _)       => Some(id)
      case Transform(id, _, _)    => Some(id)
      case Refresh(id)            => Some(id)
      case GiveFocusAt(_)         => None
      case ChangeMagnification(_) => None

enum Space:
  case Screen
  case Window
