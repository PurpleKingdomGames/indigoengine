package roguelikestarterkit.ui.component

import indigo.*
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Dimensions
import roguelikestarterkit.ui.datatypes.UiContext

/** A typeclass that confirms that some type `A` can be used as a `Component` provides the necessary
  * operations for that type to act as a component.
  */
trait StatelessComponent[A, ReferenceData] extends Component[A, ReferenceData]:

  /** The position and size of the component
    */
  def bounds(reference: ReferenceData, model: A): Bounds

  /** Produce a renderable output for this component, based on the component's model.
    */
  def present(
      context: UiContext[ReferenceData],
      model: A
  ): Outcome[ComponentFragment]

  def refresh(reference: ReferenceData, model: A, parentDimensions: Dimensions): A = model
  def updateModel(context: UiContext[ReferenceData], model: A): GlobalEvent => Outcome[A] =
    case e => Outcome(model)
