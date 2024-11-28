package roguelikestarterkit.ui.components.datatypes

import roguelikestarterkit.ui.component.Component
import roguelikestarterkit.ui.datatypes.Coords

/** `ComponentEntry` s record a components model, position, and relevant component typeclass
  * instance for use inside a `ComponentGroup`.
  */
final case class ComponentEntry[A, ReferenceData](
    id: ComponentId,
    offset: Coords,
    model: A,
    component: Component[A, ReferenceData],
    anchor: Option[Anchor]
):
  type Out = A
