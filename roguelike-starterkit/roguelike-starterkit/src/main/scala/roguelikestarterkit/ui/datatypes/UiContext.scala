package roguelikestarterkit.ui.datatypes

import indigo.*
import indigo.scenes.SceneContext

final case class UiContext[ReferenceData](
    bounds: Bounds,
    charSheet: CharSheet,
    mouseCoords: Coords,
    gameTime: GameTime,
    dice: Dice,
    inputState: InputState,
    boundaryLocator: BoundaryLocator,
    reference: ReferenceData
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
      frameContext: FrameContext[?],
      charSheet: CharSheet
  ): UiContext[Unit] =
    val mouseCoords = Coords(frameContext.mouse.position / charSheet.size.toPoint)
    UiContext(
      Bounds.zero,
      charSheet,
      mouseCoords,
      frameContext.gameTime,
      frameContext.dice,
      frameContext.inputState,
      frameContext.boundaryLocator,
      ()
    )

  def apply[ReferenceData](
      subSystemFrameContext: SubSystemFrameContext[ReferenceData],
      charSheet: CharSheet
  ): UiContext[ReferenceData] =
    val mouseCoords = Coords(subSystemFrameContext.mouse.position / charSheet.size.toPoint)
    UiContext(
      Bounds.zero,
      charSheet,
      mouseCoords,
      subSystemFrameContext.gameTime,
      subSystemFrameContext.dice,
      subSystemFrameContext.inputState,
      subSystemFrameContext.boundaryLocator,
      subSystemFrameContext.reference
    )

  def apply(
      sceneContext: SceneContext[?],
      charSheet: CharSheet
  ): UiContext[Unit] =
    val mouseCoords = Coords(sceneContext.mouse.position / charSheet.size.toPoint)
    UiContext(
      Bounds.zero,
      charSheet,
      mouseCoords,
      sceneContext.gameTime,
      sceneContext.dice,
      sceneContext.inputState,
      sceneContext.boundaryLocator,
      ()
    )
