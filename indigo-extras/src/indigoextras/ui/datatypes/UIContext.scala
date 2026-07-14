package indigoextras.ui.datatypes

import indigo.*
import indigo.scenes.SceneContext

final case class UIContext[ReferenceData](
    // Specific to UIContext
    parent: Parent,
    snapGrid: Size,
    _pointerCoords: Coords,
    state: UIState,
    magnification: Magnification,
    // The following are all the same as in SubSystemContext
    reference: ReferenceData,
    frame: Context.Frame,
    services: Context.Services,
    activeInputBounds: Option[Bounds] = Option.empty
):

  lazy val isActive: Boolean =
    state == UIState.Active

  lazy val pointerCoords: Coords =
    Coords.fromScreenSpace(_pointerCoords.unsafeToPoint, snapGrid, magnification)

  def withParent(newParent: Parent): UIContext[ReferenceData] =
    this.copy(parent = newParent)

  def withParentBounds(newBounds: Bounds): UIContext[ReferenceData] =
    withParent(parent.withBounds(newBounds))

  def withActiveInputBounds(newClip: Bounds): UIContext[ReferenceData] =
    this.copy(activeInputBounds = Option(newClip))

  def pushActiveInputBounds(newClip: Bounds): UIContext[ReferenceData] =
    this.copy(activeInputBounds = activeInputBounds.map(intersect(_, newClip)).orElse(Option(newClip)))

  def pointerIsWithinActiveInputBounds: Boolean =
    activeInputBounds.forall(_.contains(pointerCoords))

  def pointerIsWithin(bounds: Bounds): Boolean =
    pointerIsWithinActiveInputBounds &&
      bounds
        .moveBy(parent.coords + parent.additionalOffset)
        .contains(pointerCoords)

  private def intersect(a: Bounds, b: Bounds): Bounds =
    val left   = math.max(a.left, b.left)
    val top    = math.max(a.top, b.top)
    val right  = math.min(a.right, b.right)
    val bottom = math.min(a.bottom, b.bottom)

    if right <= left || bottom <= top then Bounds.zero
    else Bounds(left, top, right - left, bottom - top)

  def moveParentTo(newPosition: Coords): UIContext[ReferenceData] =
    withParent(parent.moveTo(newPosition))
  def moveParentTo(x: Int, y: Int): UIContext[ReferenceData] =
    withParent(parent.moveTo(x, y))
  def moveParentBy(offset: Coords): UIContext[ReferenceData] =
    withParent(parent.moveBy(offset))
  def moveParentBy(x: Int, y: Int): UIContext[ReferenceData] =
    withParent(parent.moveBy(x, y))

  def resizeParentTo(newDimensions: Dimensions): UIContext[ReferenceData] =
    withParent(parent.resize(newDimensions))
  def resizeParentTo(width: Int, height: Int): UIContext[ReferenceData] =
    withParent(parent.resize(width, height))
  def resizeParentBy(amount: Dimensions): UIContext[ReferenceData] =
    withParent(parent.resizeBy(amount))
  def resizeParentBy(width: Int, height: Int): UIContext[ReferenceData] =
    withParent(parent.resizeBy(width, height))

  def withSnapGrid(newSnapGrid: Size): UIContext[ReferenceData] =
    this.copy(snapGrid = newSnapGrid)
  def clearSnapGrid: UIContext[ReferenceData] =
    this.copy(snapGrid = Size(1))

  def withPointerCoords(coords: Coords): UIContext[ReferenceData] =
    this.copy(_pointerCoords = coords)

  def withState(newState: UIState): UIContext[ReferenceData] =
    this.copy(state = newState)
  def makeActive: UIContext[ReferenceData] =
    withState(UIState.Active)
  def makeInActive: UIContext[ReferenceData] =
    withState(UIState.InActive)

  def withMagnification(newMagnification: Magnification): UIContext[ReferenceData] =
    this.copy(magnification = newMagnification)

  def withReferenceData[NewReferenceData](newReference: NewReferenceData): UIContext[NewReferenceData] =
    this.copy(reference = newReference)
  def unitReference: UIContext[Unit] =
    this.copy(reference = ())

object UIContext:

  def apply[ReferenceData](
      subSystemContext: SubSystemContext[ReferenceData],
      snapGrid: Size,
      magnification: Magnification
  ): UIContext[ReferenceData] =
    UIContext(
      Parent.default,
      snapGrid,
      Coords(subSystemContext.frame.input.pointer.position),
      UIState.Active,
      magnification,
      subSystemContext.reference,
      subSystemContext.frame,
      subSystemContext.services
    )

  def apply(ctx: Context, magnification: Magnification): UIContext[Unit] =
    fromContext(ctx, (), magnification)

  def apply(ctx: Context): UIContext[Unit] =
    fromContext(ctx, (), Magnification.x1)

  def apply(ctx: SceneContext, magnification: Magnification): UIContext[Unit] =
    fromSceneContext(ctx, (), magnification)

  def apply(ctx: SceneContext): UIContext[Unit] =
    fromSceneContext(ctx, (), Magnification.x1)

  def fromContext[ReferenceData](
      ctx: Context,
      reference: ReferenceData,
      magnification: Magnification
  ): UIContext[ReferenceData] =
    UIContext(
      Parent.default,
      Size.one,
      Coords(ctx.frame.input.pointer.position),
      UIState.Active,
      magnification,
      reference,
      ctx.frame,
      ctx.services
    )

  def fromSceneContext[ReferenceData](
      ctx: SceneContext,
      reference: ReferenceData,
      magnification: Magnification
  ): UIContext[ReferenceData] =
    fromContext(ctx.toContext, reference, magnification)

  def fromSubSystemContext[ReferenceData](
      ctx: SubSystemContext[?],
      reference: ReferenceData,
      magnification: Magnification
  ): UIContext[ReferenceData] =
    fromContext(ctx.toContext, reference, magnification)

enum UIState derives CanEqual:
  case Active, InActive

  def isActive: Boolean =
    this match
      case UIState.Active   => true
      case UIState.InActive => false

  def isInActive: Boolean =
    !isActive
