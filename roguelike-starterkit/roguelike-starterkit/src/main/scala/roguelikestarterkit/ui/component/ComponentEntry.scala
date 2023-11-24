package roguelikestarterkit.ui.component

import indigo.*
import roguelikestarterkit.ui.datatypes.Coords

final case class ComponentEntry[A](offset: Coords, model: A, component: Component[A])
