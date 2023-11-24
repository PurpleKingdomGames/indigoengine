package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.CharSheet
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions
import roguelikestarterkit.ui.datatypes.UiContext

final case class WindowModel[StartupData, CA, A](
    id: WindowId,
    charSheet: CharSheet,
    bounds: Bounds,
    title: Option[String],
    contentModel: A,
    updateContentModel: (UiContext[StartupData, CA], A) => GlobalEvent => Outcome[A],
    presentContentModel: (UiContext[StartupData, CA], A) => Outcome[SceneUpdateFragment],
    draggable: Boolean,
    resizable: Boolean,
    closeable: Boolean,
    hasFocus: Boolean,
    static: Boolean
):

  def withId(value: WindowId): WindowModel[StartupData, CA, A] =
    this.copy(id = value)

  def withBounds(value: Bounds): WindowModel[StartupData, CA, A] =
    this.copy(bounds = value)

  def withPosition(value: Coords): WindowModel[StartupData, CA, A] =
    withBounds(bounds.moveTo(value))
  def moveTo(position: Coords): WindowModel[StartupData, CA, A] =
    withPosition(position)
  def moveTo(x: Int, y: Int): WindowModel[StartupData, CA, A] =
    moveTo(Coords(x, y))
  def moveBy(amount: Coords): WindowModel[StartupData, CA, A] =
    withPosition(bounds.coords + amount)
  def moveBy(x: Int, y: Int): WindowModel[StartupData, CA, A] =
    moveBy(Coords(x, y))

  def withDimensions(value: Dimensions): WindowModel[StartupData, CA, A] =
    withBounds(bounds.withDimensions(value))
  def resizeTo(size: Dimensions): WindowModel[StartupData, CA, A] =
    withDimensions(size)
  def resizeTo(x: Int, y: Int): WindowModel[StartupData, CA, A] =
    resizeTo(Dimensions(x, y))
  def resizeBy(amount: Dimensions): WindowModel[StartupData, CA, A] =
    withDimensions(bounds.dimensions + amount)
  def resizeBy(x: Int, y: Int): WindowModel[StartupData, CA, A] =
    resizeBy(Dimensions(x, y))

  def withTitle(value: String): WindowModel[StartupData, CA, A] =
    this.copy(title = Option(value))

  def withModel(value: A): WindowModel[StartupData, CA, A] =
    this.copy(contentModel = value)

  def updateModel(
      f: (UiContext[StartupData, CA], A) => GlobalEvent => Outcome[A]
  ): WindowModel[StartupData, CA, A] =
    this.copy(updateContentModel = f)

  def present(
      f: (UiContext[StartupData, CA], A) => Outcome[SceneUpdateFragment]
  ): WindowModel[StartupData, CA, A] =
    this.copy(presentContentModel = f)

  def withDraggable(value: Boolean): WindowModel[StartupData, CA, A] =
    this.copy(draggable = value)
  def isDraggable: WindowModel[StartupData, CA, A] =
    withDraggable(true)
  def notDraggable: WindowModel[StartupData, CA, A] =
    withDraggable(false)

  def withResizable(value: Boolean): WindowModel[StartupData, CA, A] =
    this.copy(resizable = value)
  def isResizable: WindowModel[StartupData, CA, A] =
    withResizable(true)
  def notResizable: WindowModel[StartupData, CA, A] =
    withResizable(false)

  def withCloseable(value: Boolean): WindowModel[StartupData, CA, A] =
    this.copy(closeable = value)
  def isCloseable: WindowModel[StartupData, CA, A] =
    withCloseable(true)
  def notCloseable: WindowModel[StartupData, CA, A] =
    withCloseable(false)

  def withFocus(value: Boolean): WindowModel[StartupData, CA, A] =
    this.copy(hasFocus = value)
  def focus: WindowModel[StartupData, CA, A] =
    withFocus(true)
  def blur: WindowModel[StartupData, CA, A] =
    withFocus(false)

  def withStatic(value: Boolean): WindowModel[StartupData, CA, A] =
    this.copy(static = value)
  def isStatic: WindowModel[StartupData, CA, A] =
    withStatic(true)
  def notStatic: WindowModel[StartupData, CA, A] =
    withStatic(false)

object WindowModel:

  def apply[StartupData, CA, A](
      id: WindowId,
      charSheet: CharSheet,
      content: A
  ): WindowModel[StartupData, CA, A] =
    WindowModel(
      id,
      charSheet,
      Bounds(Coords.zero, Dimensions.zero),
      None,
      contentModel = content,
      updateContentModel = (_, _) => _ => Outcome(content),
      presentContentModel = (_, _) => Outcome(SceneUpdateFragment.empty),
      false,
      false,
      false,
      false,
      false
    )
