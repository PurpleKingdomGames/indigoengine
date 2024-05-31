package roguelikestarterkit.ui.components.list

import indigo.*
import roguelikestarterkit.ui.component.*
import roguelikestarterkit.ui.components.group.ComponentLayout
import roguelikestarterkit.ui.datatypes.*

import scala.annotation.tailrec

/** Describes a dynamic list of 'stateless' components, their realtive layout, and propagates update
  * and presention calls.
  */
final case class ComponentList[ReferenceData] private (
    content: ReferenceData => Batch[ComponentListEntry[?, ReferenceData]],
    layout: ComponentLayout,
    components: Batch[ComponentListEntry[?, ReferenceData]],
    bounds: Bounds
):

  private[list] def withComponents(
      components: Batch[ComponentListEntry[?, ReferenceData]]
  ): ComponentList[ReferenceData] =
    this.copy(components = components)

  def withBounds(value: Bounds): ComponentList[ReferenceData] =
    this.copy(bounds = value)

  def withLayout(value: ComponentLayout): ComponentList[ReferenceData] =
    this.copy(layout = value)

  def withPosition(value: Coords): ComponentList[ReferenceData] =
    withBounds(bounds.withPosition(value))
  def moveTo(position: Coords): ComponentList[ReferenceData] =
    withPosition(position)
  def moveTo(x: Int, y: Int): ComponentList[ReferenceData] =
    moveTo(Coords(x, y))
  def moveBy(amount: Coords): ComponentList[ReferenceData] =
    withPosition(bounds.coords + amount)
  def moveBy(x: Int, y: Int): ComponentList[ReferenceData] =
    moveBy(Coords(x, y))

  def withDimensions(value: Dimensions): ComponentList[ReferenceData] =
    withBounds(bounds.withDimensions(value))
  def resizeTo(size: Dimensions): ComponentList[ReferenceData] =
    withDimensions(size)
  def resizeTo(x: Int, y: Int): ComponentList[ReferenceData] =
    resizeTo(Dimensions(x, y))
  def resizeBy(amount: Dimensions): ComponentList[ReferenceData] =
    withDimensions(bounds.dimensions + amount)
  def resizeBy(x: Int, y: Int): ComponentList[ReferenceData] =
    resizeBy(Dimensions(x, y))

object ComponentList:

  def apply[ReferenceData, A](
      bounds: Bounds
  )(contents: ReferenceData => Batch[A])(using
      c: StatelessComponent[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    val f: ReferenceData => Batch[ComponentListEntry[A, ReferenceData]] =
      r => contents(r).map(v => ComponentListEntry(Coords.zero, v, c))

    ComponentList(
      f,
      ComponentLayout.None,
      Batch.empty,
      bounds
    )

  def apply[ReferenceData, A](
      bounds: Bounds
  )(contents: A*)(using
      c: StatelessComponent[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    val f: ReferenceData => Batch[ComponentListEntry[A, ReferenceData]] =
      _ => Batch.fromSeq(contents).map(v => ComponentListEntry(Coords.zero, v, c))

    ComponentList(
      f,
      ComponentLayout.None,
      Batch.empty,
      bounds
    )

  given [ReferenceData]: StatelessComponent[ComponentList[ReferenceData], ReferenceData] with

    def bounds(
        context: ReferenceData,
        model: ComponentList[ReferenceData]
    ): Bounds =
      model.bounds

    // def updateModel(
    //     context: UiContext[ReferenceData],
    //     model: ComponentList[ReferenceData]
    // ): GlobalEvent => Outcome[ComponentList[ReferenceData]] =
    //   case e => Outcome(model)

    def present(
        context: UiContext[ReferenceData],
        model: ComponentList[ReferenceData]
    ): Outcome[ComponentFragment] =
      ListFunctions.present(
        context,
        reflowStateless(context.reference, model.withComponents(model.content(context.reference))).components
      )

    private def reflowStateless(
        context: ReferenceData,
        model: ComponentList[ReferenceData]
    ): ComponentList[ReferenceData] =
      val nextOffset =
        ListFunctions.calculateNextOffset[ReferenceData](model.bounds, model.layout)

      val newComponents =
        model.components.foldLeft(Batch.empty[ComponentListEntry[?, ReferenceData]]) {
          (acc, entry) =>
            val reflowed = entry.copy(
              offset = nextOffset(context, acc)
            )

            acc :+ reflowed
        }

      model.copy(
        components = newComponents
      )
