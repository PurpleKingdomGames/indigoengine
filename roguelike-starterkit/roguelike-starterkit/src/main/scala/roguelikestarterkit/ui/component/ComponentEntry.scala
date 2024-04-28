package roguelikestarterkit.ui.component

import indigo.*
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Coords

/** `ComponentEntry`s record a components model, position, and relevant component typeclass instance
  * for use inside a `ComponentGroup`.
  */
final case class ComponentEntry[A](offset: Coords, model: A, component: Component[A]):

  def propagatedChange(parentBounds: Bounds): ComponentEntry[A] =
    this.copy(model = component.propagatedChange(model, parentBounds))
