package roguelikestarterkit.ui.components.list

import indigo.*
import roguelikestarterkit.ui.component.StatelessComponent
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Coords

/** `ComponentEntry`s record a components model, position, and relevant component typeclass instance
  * for use inside a `ComponentList`.
  */
final case class ComponentListEntry[A, ReferenceData](offset: Coords, model: A, component: StatelessComponent[A, ReferenceData])
