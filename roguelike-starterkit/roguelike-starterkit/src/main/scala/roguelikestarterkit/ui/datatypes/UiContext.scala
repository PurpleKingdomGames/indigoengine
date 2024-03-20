package roguelikestarterkit.ui.datatypes

import indigo.*

final case class UiContext(
    bounds: Bounds,
    charSheet: CharSheet,
    mouseCoords: Coords,
    gameTime: GameTime,
    dice: Dice,
    inputState: InputState,
    boundaryLocator: BoundaryLocator
):

  val running: Seconds = gameTime.running
  val delta: Seconds   = gameTime.delta

  val mouse: Mouse       = inputState.mouse
  val keyboard: Keyboard = inputState.keyboard
  val gamepad: Gamepad   = inputState.gamepad

  def findBounds(sceneGraphNode: SceneNode): Option[Rectangle] =
    boundaryLocator.findBounds(sceneGraphNode)

  def bounds(sceneGraphNode: SceneNode): Rectangle =
    boundaryLocator.bounds(sceneGraphNode)

  lazy val screenSpaceBounds: Rectangle =
    bounds.toScreenSpace(charSheet.size)

object UiContext:

  def apply(
      frameContext: FrameContext[_],
      charSheet: CharSheet
  ): UiContext =
    val mouseCoords = Coords(frameContext.mouse.position / charSheet.size.toPoint)
    UiContext(
      Bounds.zero,
      charSheet,
      mouseCoords,
      frameContext.gameTime,
      frameContext.dice,
      frameContext.inputState,
      frameContext.boundaryLocator
    )
