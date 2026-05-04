package demo.models

import demo.Assets
import demo.scenes.NoTerminalUIComponents
import demo.scenes.TerminalUIComponents
import indigo.*
import indigoextras.ui.*
import roguelikestarterkit.*
import roguelikestarterkit.ui.*

final case class GameModel(
    pointerOverWindows: Batch[WindowId],
    num: Int,
    components: ComponentGroup[Int],
    button: Button[Unit],
    viewportSize: Size
)

object GameModel:

  val defaultCharSheet: CharSheet =
    CharSheet(
      Assets.assets.AnikkiSquare10x10,
      Size(10),
      RoguelikeTiles.Size10x10.charCrops,
      RoguelikeTiles.Size10x10.Fonts.fontKey
    )

  val initial: GameModel =
    GameModel(
      Batch.empty,
      0,
      NoTerminalUIComponents.components,
      TerminalUIComponents.customButton,
      Size.one
    )

final case class ChangeValue(value: Int) extends GlobalEvent
