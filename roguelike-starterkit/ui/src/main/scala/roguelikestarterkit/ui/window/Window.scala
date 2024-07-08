package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.component.Component
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions
import roguelikestarterkit.ui.datatypes.UIContext

/*

Let's start over.

A Component group is a ...group of components. Anything with a 'Component' instance available.

They are laid out according to some options, but the layout is basically sequential. Horizontal or vertical. Overflow is wrapped or hidden.

So far so good. This all works.

A window, should just be an allocation of space on the screen, in which to hold a Component (usually a group). It has some other jobs like managing events, and depth, but it's primary function is to reserve screen space.

Windows, holders of space on the screen, can be controlled (resized, closed, moved, etc) via events.

Those events should come from the components, not the window itself.

---

In order to replicate a "traditional" window, our components need some new functionality:

1. The ability to be anchored inside the component group, i.e. the close button should be top right (less padding), regardless of the component groups designated 'layout' style.

2. The optional ability to be draggable, and when dragged, to fire an event so that we can 'drag the title bar to drag the window' or 'drag the resize button to resize the window'.

---

Missing stuff:

Optional backgrounds on ComponentGroups? Would allow you to make title bars our of a component group and a label, for example.

Oh and we're still missing scrolling. Not totally sure where that goes yet. Clearly it's going to be important. I _think_ it's _probably_ and component group / list thing.

Might also need model windows that sit above everything.

Padding on anchors.

Terminal components, supplying a string isn't always nice, would be good to allow a Batch[Tile] or something.

We need a standard window template. Title bar, close button, resize button.

Warpping needs to take padding into account.

Components need a way to fill the available space.

One problem here is that if you want to use, say, a TextBox, then you need a BoundaryLocator instance. That comes from UIContext, but we can't have UIContext present as it makes the code untestable currently.

 */

final case class Window[A, ReferenceData](
    id: WindowId,
    snapGrid: Size,
    bounds: Bounds,
    content: A,
    component: Component[A, ReferenceData],
    hasFocus: Boolean,
    minSize: Option[Dimensions],
    maxSize: Option[Dimensions],
    state: WindowState,
    present: (UIContext[ReferenceData], Window[A, ReferenceData]) => Outcome[Layer]
):

  // TODO: Does this still make sense? 3 whats?
  lazy val minAllowedSize: Dimensions =
    Dimensions(3)

  def withId(value: WindowId): Window[A, ReferenceData] =
    this.copy(id = value)

  def withBounds(value: Bounds): Window[A, ReferenceData] =
    this.copy(bounds = value)

  def withPosition(value: Coords): Window[A, ReferenceData] =
    withBounds(bounds.moveTo(value))
  def moveTo(position: Coords): Window[A, ReferenceData] =
    withPosition(position)
  def moveTo(x: Int, y: Int): Window[A, ReferenceData] =
    moveTo(Coords(x, y))
  def moveBy(amount: Coords): Window[A, ReferenceData] =
    withPosition(bounds.coords + amount)
  def moveBy(x: Int, y: Int): Window[A, ReferenceData] =
    moveBy(Coords(x, y))

  def withDimensions(value: Dimensions): Window[A, ReferenceData] =
    val d = value.max(minAllowedSize)
    withBounds(bounds.withDimensions(maxSize.fold(d)(_.min(d))))
  def resizeTo(size: Dimensions): Window[A, ReferenceData] =
    withDimensions(size)
  def resizeTo(x: Int, y: Int): Window[A, ReferenceData] =
    resizeTo(Dimensions(x, y))
  def resizeBy(amount: Dimensions): Window[A, ReferenceData] =
    withDimensions(bounds.dimensions + amount)
  def resizeBy(x: Int, y: Int): Window[A, ReferenceData] =
    resizeBy(Dimensions(x, y))

  def withModel(value: A): Window[A, ReferenceData] =
    this.copy(content = value)

  def withFocus(value: Boolean): Window[A, ReferenceData] =
    this.copy(hasFocus = value)
  def focus: Window[A, ReferenceData] =
    withFocus(true)
  def blur: Window[A, ReferenceData] =
    withFocus(false)

  def withMinSize(min: Dimensions): Window[A, ReferenceData] =
    this.copy(minSize = Option(min))
  def withMinSize(width: Int, height: Int): Window[A, ReferenceData] =
    this.copy(minSize = Option(Dimensions(width, height)))
  def noMinSize: Window[A, ReferenceData] =
    this.copy(minSize = None)

  def withMaxSize(max: Dimensions): Window[A, ReferenceData] =
    this.copy(maxSize = Option(max))
  def withMaxSize(width: Int, height: Int): Window[A, ReferenceData] =
    this.copy(maxSize = Option(Dimensions(width, height)))
  def noMaxSize: Window[A, ReferenceData] =
    this.copy(maxSize = None)

  def withState(value: WindowState): Window[A, ReferenceData] =
    this.copy(state = value)
  def open: Window[A, ReferenceData] =
    withState(WindowState.Open)
  def close: Window[A, ReferenceData] =
    withState(WindowState.Closed)

  def isOpen: Boolean =
    state == WindowState.Open
  def isClosed: Boolean =
    state == WindowState.Closed

  def refresh(reference: ReferenceData): Window[A, ReferenceData] =
    this.copy(content =
      component.refresh(
        reference,
        content,
        bounds.dimensions
      )
    )

object Window:

  def apply[A, ReferenceData](
      id: WindowId,
      snapGrid: Size,
      content: A
  )(
      present: (UIContext[ReferenceData], Window[A, ReferenceData]) => Outcome[Layer]
  )(using c: Component[A, ReferenceData]): Window[A, ReferenceData] =
    Window(
      id,
      snapGrid,
      Bounds(Coords.zero, Dimensions.zero),
      content,
      c,
      false,
      None,
      None,
      WindowState.Closed,
      present
    )

  def updateModel[A, ReferenceData](
      context: UIContext[ReferenceData],
      model: Window[A, ReferenceData]
  ): GlobalEvent => Outcome[Window[A, ReferenceData]] =
    case e =>
      model.component
        .updateModel(context.copy(bounds = model.bounds), model.content)(e)
        .map(model.withModel)
