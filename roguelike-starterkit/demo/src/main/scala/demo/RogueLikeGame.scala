package demo

import indigo.*
import indigo.scenes.*
import indigoextras.subsystems.FPSCounter
import roguelikestarterkit.*
import roguelikestarterkit.ui.components.common.ComponentId
import roguelikestarterkit.ui.components.group.BoundsMode
import roguelikestarterkit.ui.components.group.ComponentGroup
import roguelikestarterkit.ui.components.list.ComponentList

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object RogueLikeGame extends IndigoGame[Size, Size, Model, ViewModel]:

  val magnification: Int = 2

  def initialScene(bootData: Size): Option[SceneName] =
    Option(UISubSystemScene.name)

  def scenes(bootData: Size): NonEmptyList[Scene[Size, Model, ViewModel]] =
    NonEmptyList(
      NoTerminalUI,
      UIScene,
      UISubSystemScene,
      LightingScene,
      RogueTerminalEmulatorScene,
      TerminalTextScene,
      TerminalEmulatorScene
    )

  val eventFilters: EventFilters =
    EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[Size, Model]] =
    Outcome(
      BootResult(
        Config.config.withMagnification(magnification),
        Config.config.viewport.size / 2
      )
        .withFonts(RoguelikeTiles.Size10x10.Fonts.fontInfo)
        .withAssets(Assets.assets.assetSet)
        .withShaders(
          shaders.all ++ Set(
            TerminalTextScene.customShader(ShaderId("my shader"))
          )
        )
        .withSubSystems(FPSCounter(Point(10, 350)))
    )

  def initialModel(startupData: Size): Outcome[Model] =
    Outcome(Model.initial)

  def initialViewModel(startupData: Size, model: Model): Outcome[ViewModel] =
    Outcome(ViewModel.initial)

  def setup(bootData: Size, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Size]] =
    Outcome(Startup.Success(bootData))

  def updateModel(context: FrameContext[Size], model: Model): GlobalEvent => Outcome[Model] =
    case KeyboardEvent.KeyUp(Key.PAGE_UP) =>
      Outcome(model).addGlobalEvents(SceneEvent.LoopPrevious)

    case KeyboardEvent.KeyUp(Key.PAGE_DOWN) =>
      Outcome(model).addGlobalEvents(SceneEvent.LoopNext)

    case Log(msg) =>
      IndigoLogger.info(msg)
      Outcome(model)

    case SceneEvent.SceneChange(_, _, _) =>
      Outcome(model.copy(mouseOverWindows = Batch.empty))

    case _ =>
      Outcome(model)

  def updateViewModel(
      context: FrameContext[Size],
      model: Model,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: FrameContext[Size],
      model: Model,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)

final case class Model(mouseOverWindows: Batch[WindowId], components: ComponentGroup[Int])

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
      ComponentGroup(BoundsMode.fixed(200, 200))
        .add(
          ComponentList(Dimensions(200, 40)) { (_: Int) =>
            (1 to 3).toBatch.map { i =>
              ComponentId("lbl" + i) -> Label[Int](
                "Custom rendered label " + i,
                (_, label) => Bounds(0, 0, 150, 10)
              ) { case (offset, label, dimensions) =>
                Outcome(
                  Layer(
                    TextBox(label)
                      .withColor(RGBA.Red)
                      .moveTo(offset.unsafeToPoint)
                      .withSize(dimensions.unsafeToSize)
                  )
                )
              }
            }
          }
        )
        .add(
          Label[Int](
            "Another label",
            (_, label) => Bounds(0, 0, 150, 10)
          ) { case (offset, label, dimensions) =>
            Outcome(
              Layer(
                TextBox(label)
                  .withColor(RGBA.White)
                  .moveTo(offset.unsafeToPoint)
                  .withSize(dimensions.unsafeToSize)
              )
            )
          }
        )
    )

final case class ViewModel()
object ViewModel:
  def initial: ViewModel =
    ViewModel()

final case class Log(msg: String) extends GlobalEvent
