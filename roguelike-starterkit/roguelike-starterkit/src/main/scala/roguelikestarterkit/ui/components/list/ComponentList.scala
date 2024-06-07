package roguelikestarterkit.ui.components.list

import indigo.*
import roguelikestarterkit.Padding
import roguelikestarterkit.ui.component.*
import roguelikestarterkit.ui.components.common.ComponentEntry
import roguelikestarterkit.ui.components.common.ComponentLayout
import roguelikestarterkit.ui.components.common.ContainerLikeFunctions
import roguelikestarterkit.ui.datatypes.*

import scala.annotation.tailrec

/** Describes a dynamic list of 'stateless' components, their realtive layout, and propagates update
  * and presention calls.
  */
final case class ComponentList[ReferenceData] private (
    content: ReferenceData => Batch[ComponentEntry[?, ReferenceData]],
    layout: ComponentLayout,
    dimensions: Dimensions
):

  private def addSingle[A](entry: ReferenceData => A)(using
      c: StatelessComponent[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    val f =
      (r: ReferenceData) => content(r) :+ ComponentEntry(Coords.zero, entry(r), c)

    this.copy(
      content = f
    )

  def addOne[A](entry: ReferenceData => A)(using
      c: StatelessComponent[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    addSingle(entry)

  def addOne[A](entry: A)(using
      c: StatelessComponent[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    addSingle(_ => entry)

  def add[A](entries: Batch[ReferenceData => A])(using
      c: StatelessComponent[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    entries.foldLeft(this) { case (acc, next) => acc.addSingle(next) }

  def add[A](entries: (ReferenceData => A)*)(using
      c: StatelessComponent[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    Batch.fromSeq(entries).foldLeft(this) { case (acc, next) => acc.addSingle(next) }

  def add[A](entries: ReferenceData => Batch[A])(using
      c: StatelessComponent[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    this.copy(
      content =
        (r: ReferenceData) => content(r) ++ entries(r).map(v => ComponentEntry(Coords.zero, v, c))
    )

  def withDimensions(value: Dimensions): ComponentList[ReferenceData] =
    this.copy(dimensions = value)

  def withLayout(value: ComponentLayout): ComponentList[ReferenceData] =
    this.copy(layout = value)

  def resizeTo(size: Dimensions): ComponentList[ReferenceData] =
    withDimensions(size)
  def resizeTo(x: Int, y: Int): ComponentList[ReferenceData] =
    resizeTo(Dimensions(x, y))
  def resizeBy(amount: Dimensions): ComponentList[ReferenceData] =
    withDimensions(dimensions + amount)
  def resizeBy(x: Int, y: Int): ComponentList[ReferenceData] =
    resizeBy(Dimensions(x, y))

object ComponentList:

  def apply[ReferenceData, A](
      dimensions: Dimensions
  )(contents: ReferenceData => Batch[A])(using
      c: StatelessComponent[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    val f: ReferenceData => Batch[ComponentEntry[A, ReferenceData]] =
      r => contents(r).map(v => ComponentEntry(Coords.zero, v, c))

    ComponentList(
      f,
      ComponentLayout.Vertical(Padding.zero),
      dimensions
    )

  def apply[ReferenceData, A](
      dimensions: Dimensions
  )(contents: A*)(using
      c: StatelessComponent[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    val f: ReferenceData => Batch[ComponentEntry[A, ReferenceData]] =
      _ => Batch.fromSeq(contents).map(v => ComponentEntry(Coords.zero, v, c))

    ComponentList(
      f,
      ComponentLayout.Vertical(Padding.zero),
      dimensions
    )

  given [ReferenceData]: StatelessComponent[ComponentList[ReferenceData], ReferenceData] with

    def bounds(
        context: ReferenceData,
        model: ComponentList[ReferenceData]
    ): Bounds =
      Bounds(model.dimensions)

    def present(
        context: UiContext[ReferenceData],
        model: ComponentList[ReferenceData]
    ): Outcome[ComponentFragment] =
      ContainerLikeFunctions.present(context, contentReflow(context.reference, model))

    private def contentReflow(
        reference: ReferenceData,
        model: ComponentList[ReferenceData]
    ): Batch[ComponentEntry[?, ReferenceData]] =
      val nextOffset =
        ContainerLikeFunctions
          .calculateNextOffset[ReferenceData](model.dimensions, model.layout)

      model.content(reference).foldLeft(Batch.empty[ComponentEntry[?, ReferenceData]]) {
        (acc, entry) =>
          val reflowed = entry.copy(
            offset = nextOffset(reference, acc)
          )

          acc :+ reflowed
      }
