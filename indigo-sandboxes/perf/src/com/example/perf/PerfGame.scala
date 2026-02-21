package com.example.perf

import indigo.*
import indigo.json.Json
import indigoextras.subsystems.FPSCounter

final class PerfGame extends Game[Unit, Dude, DudeModel] {

  val eventFilters: EventFilters =
    EventFilters {
      case e: FrameTick =>
        Some(e)

      case e: KeyboardEvent.KeyDown =>
        Some(e)

      case e: KeyboardEvent.KeyUp =>
        Some(e)

      case _ =>
        None
    }

  def boot(flags: Map[String, String]): Outcome[BootResult[Unit, DudeModel]] =
    Outcome {
      BootResult
        .noData(
          GameConfig(
            viewport = GameViewport(PerfGame.viewportWidth, PerfGame.viewportHeight),
            frameRateLimit = None,
            clearColor = RGBA(0.4, 0.2, 0.5, 1),
            magnification = PerfGame.magnificationLevel,
            resizePolicy = ResizePolicy.NoResize,
            transparentBackground = false,
            advanced = AdvancedGameConfig.default
              .withBatchSize(512)
              .withAutoLoadStandardShaders(false)
              .withContextMenu
          ).noResize
        )
        .withAssets(PerfAssets.assets)
        .withFonts(Fonts.fontInfo)
        .withSubSystems(
          FPSCounter(Fonts.fontKey, PerfAssets.smallFontName)
            .moveTo(10, 565)
        )
        .withShaders(
          StandardShaders.Bitmap,
          StandardShaders.ImageEffects,
          StandardShaders.NormalBlend,
          StandardShaders.ShapeBox
        )
    }

  def scenes(bootData: Unit): NonEmptyBatch[Scene[Dude, DudeModel]] =
    NonEmptyBatch(Scene.empty)

  def initialScene(bootData: Unit): Option[SceneName] =
    None

  def initialModel(startupData: Dude): Outcome[DudeModel] =
    Outcome(PerfModel.initialModel(startupData))

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Dude]] = {
    def makeStartupData(aseprite: Aseprite, spriteAndAnimations: SpriteAndAnimations): Startup.Success[Dude] =
      Startup
        .Success(
          Dude(
            aseprite,
            spriteAndAnimations.sprite
              .withRef(16, 16) // Initial offset, so when talk about his position it's the center of the sprite
              .moveTo(
                PerfGame.viewportWidth / 2 / PerfGame.magnificationLevel,
                PerfGame.viewportHeight / 2 / PerfGame.magnificationLevel
              ) // Also place him in the middle of the screen initially
          )
        )
        .addAnimations(spriteAndAnimations.animations)

    val res: Option[Startup.Success[Dude]] = for {
      json                <- assetCollection.findTextDataByName(AssetName(PerfAssets.dudeName.toString + "-json"))
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- aseprite.toSpriteAndAnimations(dice, PerfAssets.dudeName)
    } yield makeStartupData(aseprite, spriteAndAnimations)

    Outcome(res.getOrElse(Startup.Failure("Failed to load the dude")))
  }

  def updateModel(context: Context[Dude], model: DudeModel): GlobalEvent => Outcome[DudeModel] =
    PerfModel.updateModel(model)

  def present(context: Context[Dude], model: DudeModel): Outcome[SceneUpdateFragment] =
    Outcome(PerfView.updateView(model))

}

object PerfGame:

  val viewportWidth: Int      = 800
  val viewportHeight: Int     = 600
  val magnificationLevel: Int = 1

final case class Dude(aseprite: Aseprite, sprite: Sprite[?])
