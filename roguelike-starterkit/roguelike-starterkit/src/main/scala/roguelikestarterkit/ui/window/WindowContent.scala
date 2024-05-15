package roguelikestarterkit.ui.window

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.Layer
import roguelikestarterkit.Component
import roguelikestarterkit.ComponentGroup
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.UiContext

trait WindowContent[A, ReferenceData]:

  /** Update this content's model.
    */
  def updateModel(
      context: UiContext[ReferenceData],
      model: A
  ): GlobalEvent => Outcome[A]

  /** Produce a renderable output for this content base on the model
    */
  def present(
      context: UiContext[ReferenceData],
      model: A
  ): Outcome[Layer]

  /** Called when the window's content area bounds changes, gives the model an opportunity to
    * respond to the new content area.
    */
  def cascade(model: A, newBounds: Bounds): A

  /** Called when a window has been told to refresh its content, possibly by the content itself.
    */
  def refresh(model: A): A

object WindowContent:

  given [A, ReferenceData](using
      comp: Component[A, ReferenceData]
  ): WindowContent[A, ReferenceData] with

    def updateModel(
        context: UiContext[ReferenceData],
        model: A
    ): GlobalEvent => Outcome[A] =
      e => comp.updateModel(context, model)(e)

    def present(
        context: UiContext[ReferenceData],
        model: A
    ): Outcome[Layer] =
      comp.present(context, model).map(_.toLayer)

    def cascade(
        model: A,
        newBounds: Bounds
    ): A =
      comp.cascade(model, newBounds)

    def refresh(model: A): A =
      comp.reflow(model)
