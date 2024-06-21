// package roguelikestarterkit.ui.datatypes

// import indigo.*
// import indigo.scenes.SceneContext

// final case class UIContext[ReferenceData](
//     bounds: Bounds,
//     charSheet: CharSheet,
//     mouseCoords: Coords,
//     gameTime: GameTime,
//     dice: Dice,
//     inputState: InputState,
//     boundaryLocator: BoundaryLocator,
//     reference: ReferenceData,
//     state: UIState
// ):

//   val running: Seconds = gameTime.running
//   val delta: Seconds   = gameTime.delta

//   val mouse: Mouse       = inputState.mouse
//   val keyboard: Keyboard = inputState.keyboard
//   val gamepad: Gamepad   = inputState.gamepad

//   def findBounds(sceneGraphNode: SceneNode): Option[Rectangle] =
//     boundaryLocator.findBounds(sceneGraphNode)

//   def bounds(sceneGraphNode: SceneNode): Rectangle =
//     boundaryLocator.bounds(sceneGraphNode)

//   lazy val screenSpaceBounds: Rectangle =
//     bounds.toScreenSpace(charSheet.size)

//   val isActive: Boolean =
//     state == UIState.Active

// object UIContext:

//   def apply[ReferenceData](
//       subSystemFrameContext: SubSystemFrameContext[ReferenceData],
//       charSheet: CharSheet
//   ): UIContext[ReferenceData] =
//     val mouseCoords = Coords(subSystemFrameContext.mouse.position / charSheet.size.toPoint)
//     UIContext(
//       Bounds.zero,
//       charSheet,
//       mouseCoords,
//       subSystemFrameContext.gameTime,
//       subSystemFrameContext.dice,
//       subSystemFrameContext.inputState,
//       subSystemFrameContext.boundaryLocator,
//       subSystemFrameContext.reference,
//       UIState.Active
//     )

// enum UIState:
//   case Active, InActive
