package indigoextras.ui.window

import indigo.*
import indigoextras.ui.components.datatypes.Anchor as Ankor
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.Coords
import indigoextras.ui.datatypes.Dimensions

enum WindowEvent extends GlobalEvent derives CanEqual:

  // Events sent to the game

  /** Informs the game when the pointer moves into a window's bounds */
  case PointerOver(id: WindowId)

  /** Informs the game when the pointer moves out of a window's bounds */
  case PointerOut(id: WindowId)

  /** Informs the game when a window has resized */
  case Resized(id: WindowId)

  /** Informs the game when a window has opened */
  case Opened(id: WindowId)

  /** Informs the game when a window has closed */
  case Closed(id: WindowId)

  /** Informs the game when a window has gained focus */
  case Focused(id: WindowId)

  /** Informs the game when a window has lost focus */
  case Blurred(id: WindowId)

  /** Informs the game that magnification of all windows has changed */
  case MagnificationChanged(newMagnification: Magnification)

  // User sent events

  /** Tells a window to open */
  case Open(id: WindowId)

  /** Tells a window to open at a specific location */
  case OpenAt(id: WindowId, coords: Coords)

  /** Tells a window to close */
  case Close(id: WindowId)

  /** Closes whichever window is focused */
  case CloseFocused

  /** Tells a window to toggle between open and closed. */
  case Toggle(id: WindowId)

  /** Brings a window into focus */
  case Focus(id: WindowId)

  /** Focuses the top window at the given location */
  case GiveFocusAt(coords: Coords)

  /** Moves a window to the location given */
  case Move(id: WindowId, position: Coords, space: Space)

  /** Anchors a window on the screen */
  case Anchor(id: WindowId, anchor: Ankor)

  /** Resizes a window to a given size */
  case Resize(id: WindowId, dimensions: Dimensions, space: Space)

  /** Changes the bounds of a window */
  case Transform(id: WindowId, bounds: Bounds, space: Space)

  /** Changes the magnification of all windows */
  case ChangeMagnification(newMagnification: Magnification)

  /** Tells a window request its content to refresh */
  case Refresh(id: WindowId)

  def windowId: Option[WindowId] =
    this match
      case PointerOver(id)         => Some(id)
      case PointerOut(id)          => Some(id)
      case Resized(id)             => Some(id)
      case Opened(id)              => Some(id)
      case Closed(id)              => Some(id)
      case Open(id)                => Some(id)
      case OpenAt(id, _)           => Some(id)
      case Close(id)               => Some(id)
      case Toggle(id)              => Some(id)
      case Move(id, _, _)          => Some(id)
      case Anchor(id, _)           => Some(id)
      case Resize(id, _, _)        => Some(id)
      case Transform(id, _, _)     => Some(id)
      case Refresh(id)             => Some(id)
      case Focus(id)               => Some(id)
      case Focused(id)             => Some(id)
      case Blurred(id)             => Some(id)
      case GiveFocusAt(_)          => None
      case ChangeMagnification(_)  => None
      case MagnificationChanged(_) => None
      case CloseFocused            => None

  def isNotification: Boolean =
    this match
      case Opened(_)               => true
      case Closed(_)               => true
      case Focused(_)              => true
      case Blurred(_)              => true
      case PointerOver(_)          => true
      case PointerOut(_)           => true
      case Resized(_)              => true
      case MagnificationChanged(_) => true
      case ChangeMagnification(_)  => false
      case Open(_)                 => false
      case OpenAt(_, _)            => false
      case Close(_)                => false
      case Toggle(_)               => false
      case Move(_, _, _)           => false
      case Anchor(_, _)            => false
      case Resize(_, _, _)         => false
      case Transform(_, _, _)      => false
      case Refresh(_)              => false
      case Focus(_)                => false
      case GiveFocusAt(_)          => false
      case CloseFocused            => false
