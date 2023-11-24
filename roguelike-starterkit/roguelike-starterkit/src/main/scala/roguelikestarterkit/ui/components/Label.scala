package roguelikestarterkit.ui.components

import indigo.*
import indigo.syntax.*
import roguelikestarterkit.*
import roguelikestarterkit.tiles.RoguelikeTiles10x10
import roguelikestarterkit.tiles.RoguelikeTiles5x6
import roguelikestarterkit.ui.component.Component
import roguelikestarterkit.ui.component.ComponentFragment
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.CharSheet
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.UiContext

final case class Label(text: String, render: (Coords, String) => Outcome[ComponentFragment]):
  def withText(value: String): Label =
    this.copy(text = value)

object Label:

  given Component[Label] with
    def bounds(model: Label): Bounds =
      Bounds(0, 0, model.text.length, 1)

    def updateModel[StartupData, ContextData](
        context: UiContext[StartupData, ContextData],
        model: Label
    ): GlobalEvent => Outcome[Label] =
      case _ =>
        Outcome(model)

    def present[StartupData, ContextData](
        context: UiContext[StartupData, ContextData],
        model: Label
    ): Outcome[ComponentFragment] =
      model.render(context.bounds.coords, model.text)
