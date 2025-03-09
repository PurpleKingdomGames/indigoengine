package demo.models

import demo.Assets
import demo.scenes.NoTerminalUIComponents
import demo.scenes.TerminalUIComponents
import indigo.*
import indigoextras.ui.*
import indigoextras.ui.syntax.*
import roguelikestarterkit.*
import roguelikestarterkit.ui.*

final case class Model(
    pointerOverWindows: Batch[WindowId],
    num: Int,
    components: ComponentGroup[Int],
    button: Button[Unit]
)

object Model:

  import indigo.syntax.*

  val defaultCharSheet: CharSheet =
    CharSheet(
      Assets.assets.AnikkiSquare10x10,
      Size(10),
      RoguelikeTiles.Size10x10.charCrops,
      RoguelikeTiles.Size10x10.Fonts.fontKey
    )

  val initial: Model =
    Model(
      Batch.empty,
      0,
      NoTerminalUIComponents.components,
      TerminalUIComponents.customButton
    )

final case class ChangeValue(value: Int) extends GlobalEvent
