package indigoextras.ui.components

import indigo.*
import indigoextras.ui.component.Component
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.UIContext

import scala.annotation.targetName
import indigoextras.ui.datatypes.Coords

/** `TextArea`s are a simple stateless component that render multi-line text.
  */
final case class TextArea[ReferenceData](
    text: UIContext[ReferenceData] => String,
    render: (UIContext[ReferenceData], TextArea[ReferenceData]) => Outcome[Layer],
    calculateBounds: (UIContext[ReferenceData], String) => Bounds
):
  def withText(value: String): TextArea[ReferenceData] =
    this.copy(text = _ => value)
  def withText(f: UIContext[ReferenceData] => String): TextArea[ReferenceData] =
    this.copy(text = f)

object TextArea:

  def apply[ReferenceData](text: String, calculateBounds: (UIContext[ReferenceData], String) => Bounds)(
      present: (UIContext[ReferenceData], TextArea[ReferenceData]) => Outcome[Layer]
  ): TextArea[ReferenceData] =
    TextArea(
      (_: UIContext[ReferenceData]) => text,
      present,
      calculateBounds
    )

  @targetName("TextAreaRefToString")
  def apply[ReferenceData](
      text: UIContext[ReferenceData] => String,
      calculateBounds: (UIContext[ReferenceData], String) => Bounds
  )(
      present: (UIContext[ReferenceData], TextArea[ReferenceData]) => Outcome[Layer]
  ): TextArea[ReferenceData] =
    TextArea(
      text,
      present,
      calculateBounds
    )

  def apply[ReferenceData](text: String, bounds: Bounds)(
      present: (UIContext[ReferenceData], TextArea[ReferenceData]) => Outcome[Layer]
  ): TextArea[ReferenceData] =
    TextArea(
      (_: UIContext[ReferenceData]) => text,
      present,
      (_, _) => bounds
    )

  @targetName("TextAreaRefToStringFixedBounds")
  def apply[ReferenceData](
      text: UIContext[ReferenceData] => String,
      bounds: Bounds
  )(
      present: (UIContext[ReferenceData], TextArea[ReferenceData]) => Outcome[Layer]
  ): TextArea[ReferenceData] =
    TextArea(
      text,
      present,
      (_, _) => bounds
    )

  given [ReferenceData]: Component[TextArea[ReferenceData], ReferenceData] with
    def bounds(context: UIContext[ReferenceData], model: TextArea[ReferenceData]): Bounds =
      model.calculateBounds(context, model.text(context))

    def updateModel(
        context: UIContext[ReferenceData],
        model: TextArea[ReferenceData]
    ): GlobalEvent => Outcome[TextArea[ReferenceData]] =
      _ => Outcome(model)

    def present(
        context: UIContext[ReferenceData],
        model: TextArea[ReferenceData]
    ): Outcome[Layer] =
      model.render(
        context,
        model
      )

    def refresh(
        context: UIContext[ReferenceData],
        model: TextArea[ReferenceData]
    ): TextArea[ReferenceData] =
      model

    def hitTest(
        context: UIContext[ReferenceData],
        model: TextArea[ReferenceData],
        event: GlobalEvent
    ): Boolean =
      event match
        case _: WheelEvent => false
        case _ => coordsInBounds(context.pointerCoords, model.calculateBounds(context, model.text(context)), context)

    def hasPointerCapture(context: UIContext[ReferenceData], model: TextArea[ReferenceData]): Boolean =
      false

  private def coordsInBounds[ReferenceData](pnt: Coords, bounds: Bounds, context: UIContext[ReferenceData]): Boolean =
    context.pointerIsWithinActiveInputBounds &&
      bounds
        .moveBy(context.parent.coords + context.parent.additionalOffset)
        .contains(pnt)
