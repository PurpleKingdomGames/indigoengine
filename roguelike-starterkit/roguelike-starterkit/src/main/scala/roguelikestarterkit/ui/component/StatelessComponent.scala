package roguelikestarterkit.ui.component

import indigo.*
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.UiContext

/** A typeclass that confirms that some type `A` can be used as a `Component` provides the necessary
  * operations for that type to act as a component.
  */
trait StatelessComponent[A, ReferenceData] extends Component[A, ReferenceData]:

  /** The position and size of the component
    */
  def bounds(model: A): Bounds

  /** Update this componenets model. In stateless componenets, the model is more like a view model,
    * and should be assumed to be a cache for presentation data that could be reset at any time.
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

  def reflow(model: A): A                        = model
  def cascade(model: A, parentBounds: Bounds): A = model
