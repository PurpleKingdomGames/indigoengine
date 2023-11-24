package roguelikestarterkit.ui.component

import indigo.*
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.UiContext

trait Component[A]:

  def bounds(model: A): Bounds

  def updateModel[StartupData, ContextData](
      context: UiContext[StartupData, ContextData],
      model: A
  ): GlobalEvent => Outcome[A]

  def present[StartupData, ContextData](
      context: UiContext[StartupData, ContextData],
      model: A
  ): Outcome[ComponentFragment]
