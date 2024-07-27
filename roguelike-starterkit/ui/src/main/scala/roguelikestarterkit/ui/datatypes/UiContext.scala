package roguelikestarterkit.ui.datatypes

import indigo.*
import indigo.scenes.SceneContext

final case class UIContext[ReferenceData](
    bounds: Bounds,
    snapGrid: Size,
    mouseCoords: Coords,
    gameTime: GameTime,
    dice: Dice,
    inputState: InputState,
    boundaryLocator: BoundaryLocator,
    reference: ReferenceData,
    state: UIState,
    magnification: Int,
    additionalOffset: Coords
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
    bounds.toScreenSpace(snapGrid)

  def moveBoundsBy(offset: Coords): UIContext[ReferenceData] =
    this.copy(bounds = bounds.moveBy(offset))

  val isActive: Boolean =
    state == UIState.Active

  def unitReference: UIContext[Unit] =
    this.copy(reference = ())

  def withAdditionalOffset(offset: Coords): UIContext[ReferenceData] =
    this.copy(additionalOffset = offset)

object UIContext:

  def apply[ReferenceData](
      subSystemFrameContext: SubSystemFrameContext[ReferenceData],
      snapGrid: Size,
      magnification: Int
  ): UIContext[ReferenceData] =
    val mouseCoords = Coords(subSystemFrameContext.mouse.position / snapGrid.toPoint)
    UIContext(
      Bounds.zero,
      snapGrid,
      mouseCoords,
      subSystemFrameContext.gameTime,
      subSystemFrameContext.dice,
      subSystemFrameContext.inputState,
      subSystemFrameContext.boundaryLocator,
      subSystemFrameContext.reference,
      UIState.Active,
      magnification,
      Coords.zero
    )

  def apply[ReferenceData](
      subSystemFrameContext: SubSystemFrameContext[ReferenceData],
      snapGrid: Size,
      magnification: Int,
      additionalOffset: Coords
  ): UIContext[ReferenceData] =
    val mouseCoords = Coords(subSystemFrameContext.mouse.position / snapGrid.toPoint)
    UIContext(
      Bounds.zero,
      snapGrid,
      mouseCoords,
      subSystemFrameContext.gameTime,
      subSystemFrameContext.dice,
      subSystemFrameContext.inputState,
      subSystemFrameContext.boundaryLocator,
      subSystemFrameContext.reference,
      UIState.Active,
      magnification,
      additionalOffset
    )

enum UIState:
  case Active, InActive

  def isActive: Boolean =
    this match
      case UIState.Active   => true
      case UIState.InActive => false

  def isInActive: Boolean =
    !isActive
