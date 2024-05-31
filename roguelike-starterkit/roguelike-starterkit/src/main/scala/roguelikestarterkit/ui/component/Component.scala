package roguelikestarterkit.ui.component

import indigo.*
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.UiContext

/** A typeclass that confirms that some type `A` can be used as a `Component` provides the necessary
  * operations for that type to act as a component.
  */
trait Component[A, ReferenceData]:

  /** The position and size of the component
    */
  def bounds(reference: ReferenceData, model: A): Bounds

  /** Update this componenets model.
    */
  def updateModel(
      context: UiContext[ReferenceData],
      model: A
  ): GlobalEvent => Outcome[A]

  /** Produce a renderable output for this component, based on the component's model.
    */
  def present(
      context: UiContext[ReferenceData],
      model: A
  ): Outcome[ComponentFragment]

  /** Used internally to instruct the component that the layout has changed in some way, and that it
    * should reflow it's contents - whatever that means in the context of this component type.
    */
  def reflow(model: A): A

  /** Informs the Component that something about its parent has changed in case it needs to take
    * action. Currently the only cascaded change is the bounds.
    */
  def cascade(model: A, parentBounds: Bounds): A
