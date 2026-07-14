package indigoextras.ui.components

import indigo.*
import indigoextras.ui.component.*
import indigoextras.ui.components.datatypes.ComponentEntry
import indigoextras.ui.components.datatypes.ComponentId
import indigoextras.ui.components.datatypes.ComponentLayout
import indigoextras.ui.components.datatypes.ContainerLikeFunctions
import indigoextras.ui.components.datatypes.Padding
import indigoextras.ui.datatypes.*

/** Describes a dynamic list of components, and their realtive layout.
  */
final case class ComponentList[ReferenceData] private[components] (
    content: UIContext[ReferenceData] => Batch[ComponentEntry[?, ReferenceData]],
    stateMap: Map[ComponentId, Any],
    layout: ComponentLayout,
    dimensions: Dimensions,
    background: Bounds => Layer
):
  def addOne[A](entry: UIContext[ReferenceData] => (ComponentId, A))(using
      c: Component[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    addSingle(entry)

  def addOne[A](entry: (ComponentId, A))(using
      c: Component[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    addSingle(_ => entry)

  def add[A](entries: Batch[UIContext[ReferenceData] => (ComponentId, A)])(using
      c: Component[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    entries.foldLeft(this) { case (acc, next) => acc.addSingle(next) }

  def add[A](entries: (UIContext[ReferenceData] => (ComponentId, A))*)(using
      c: Component[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    Batch.fromSeq(entries).foldLeft(this) { case (acc, next) => acc.addSingle(next) }

  def add[A](entries: UIContext[ReferenceData] => Batch[(ComponentId, A)])(using
      c: Component[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    this.copy(
      content = (ctx: UIContext[ReferenceData]) =>
        content(ctx) ++ entries(ctx).map(v => ComponentEntry(v._1, Coords.zero, v._2, c, None))
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

  def withBackground(present: Bounds => Layer): ComponentList[ReferenceData] =
    this.copy(background = present)

  private def addSingle[A](entry: UIContext[ReferenceData] => (ComponentId, A))(using
      c: Component[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    val f =
      (ctx: UIContext[ReferenceData]) =>
        content(ctx) :+ {
          val (id, a) = entry(ctx)
          ComponentEntry(id, Coords.zero, a, c, None)
        }

    this.copy(
      content = f
    )

object ComponentList:

  def apply[ReferenceData, A](
      dimensions: Dimensions
  )(contents: UIContext[ReferenceData] => Batch[(ComponentId, A)])(using
      c: Component[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    val f: UIContext[ReferenceData] => Batch[ComponentEntry[A, ReferenceData]] =
      ctx => contents(ctx).map(v => ComponentEntry(v._1, Coords.zero, v._2, c, None))

    ComponentList(
      f,
      Map.empty,
      ComponentLayout.Vertical(Padding.zero),
      dimensions,
      _ => Layer.empty
    )

  def apply[ReferenceData, A](
      dimensions: Dimensions
  )(contents: (ComponentId, A)*)(using
      c: Component[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    val f: UIContext[ReferenceData] => Batch[ComponentEntry[A, ReferenceData]] =
      _ => Batch.fromSeq(contents).map(v => ComponentEntry(v._1, Coords.zero, v._2, c, None))

    ComponentList(
      f,
      Map.empty,
      ComponentLayout.Vertical(Padding.zero),
      dimensions,
      _ => Layer.empty
    )

  given [ReferenceData]: Component[ComponentList[ReferenceData], ReferenceData] with

    def bounds(
        context: UIContext[ReferenceData],
        model: ComponentList[ReferenceData]
    ): Bounds =
      Bounds(model.dimensions)

    def updateModel(
        context: UIContext[ReferenceData],
        model: ComponentList[ReferenceData]
    ): GlobalEvent => Outcome[ComponentList[ReferenceData]] =
      case e =>
        val nextStateMap =
          ContainerLikeFunctions
            .routeOrBroadcast(context, model.dimensions, entries(context, model))(e)
            .map(_.map(e => e.id -> e.model).toMap)

        nextStateMap.map { newStateMap =>
          model.copy(stateMap = newStateMap)
        }

    override def hitTest(
        context: UIContext[ReferenceData],
        model: ComponentList[ReferenceData],
        event: GlobalEvent
    ): Boolean =
      ContainerLikeFunctions.hitTest(context, entries(context, model), event)

    override def hasPointerCapture(
        context: UIContext[ReferenceData],
        model: ComponentList[ReferenceData]
    ): Boolean =
      ContainerLikeFunctions.hasPointerCapture(context, entries(context, model))

    def present(
        context: UIContext[ReferenceData],
        model: ComponentList[ReferenceData]
    ): Outcome[Layer] =
      ContainerLikeFunctions
        .present(
          context,
          model.dimensions,
          entries(context, model)
        )
        .map { components =>
          val background = model.background(Bounds(context.parent.coords, model.dimensions))
          Layer.Stack(background, components)
        }

    // ComponentList's have a fixed size, so we don't need to do anything here,
    // and since this component's size doesn't change, nor do we need to
    // propagate further.
    def refresh(
        context: UIContext[ReferenceData],
        model: ComponentList[ReferenceData]
    ): ComponentList[ReferenceData] =
      model

    /** Rebuilds the entries from the content function, restoring each child's last known state from the stateMap, and
      * reflows their layout offsets so that things like pointer clicks land in the right place.
      */
    private def entries(
        context: UIContext[ReferenceData],
        model: ComponentList[ReferenceData]
    ): Batch[ComponentEntry[?, ReferenceData]] =
      contentReflow(
        context,
        model.dimensions,
        model.layout,
        model.content(context).map { entry =>
          model.stateMap.get(entry.id) match
            case None =>
              entry

            case Some(savedState) =>
              entry.copy(model = savedState.asInstanceOf[entry.Out])
        }
      )

    private def contentReflow(
        context: UIContext[ReferenceData],
        dimensions: Dimensions,
        layout: ComponentLayout,
        entries: Batch[ComponentEntry[?, ReferenceData]]
    ): Batch[ComponentEntry[?, ReferenceData]] =
      val nextOffset =
        ContainerLikeFunctions
          .calculateNextOffset[ReferenceData](dimensions, layout)

      entries.foldLeft(Batch.empty[ComponentEntry[?, ReferenceData]]) { (acc, entry) =>
        val reflowed = entry.copy(
          offset = nextOffset(context, acc)
        )

        acc :+ reflowed
      }
