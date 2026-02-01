package com.example.sandbox

import com.example.sandbox.scenes.*
import example.TestFont
import indigo.*
import indigo.json.Json
import indigoextras.effectmaterials.LegacyEffects
import indigoextras.effectmaterials.Refraction
import indigoextras.subsystems.FPSCounter

final class SandboxGame extends Game[SandboxBootData, SandboxStartupData, SandboxGameModel]:

  def initialScene(bootData: SandboxBootData): Option[SceneName] =
    Some(WindowsScene.name)

  def scenes(bootData: SandboxBootData): NonEmptyBatch[Scene[SandboxStartupData, SandboxGameModel]] =
    NonEmptyBatch(
      OriginalScene,
      ShapesScene,
      LightsScene,
      RefractionScene,
      LegacyEffectsScene,
      BoundsScene,
      CameraScene,
      TextureTileScene,
      ConfettiScene,
      MutantsScene,
      CratesScene,
      ClipScene,
      TextScene,
      BoxesScene,
      ManyEventHandlers,
      TimelineScene,
      UltravioletScene,
      PointersScene,
      BoundingCircleScene,
      LineReflectionScene,
      CameraWithCloneTilesScene,
      PathFindingScene,
      CaptureScreenScene,
      NineSliceScene,
      SfxScene,
      ComponentUIScene,
      ComponentUIScene2,
      WindowsScene,
      MeshScene,
      WaypointScene,
      ActorPoolScene,
      ActorPoolPhysicsScene,
      PerformerScene,
      PerformerPhysicsScene,
      ViewportResizeScene,
      MultiPointScene
    )

  val eventFilters: EventFilters = EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[SandboxBootData, SandboxGameModel]] = {
    val gameViewport =
      (flags.get("width"), flags.get("height")) match {
        case (Some(w), Some(h)) =>
          GameViewport(w.toInt, h.toInt)

        case _ =>
          GameViewport(SandboxGame.viewportWidth, SandboxGame.viewportHeight)
      }

    Outcome(
      BootResult(
        GameConfig(
          viewport = gameViewport,
          clearColor = RGBA(0.4, 0.2, 0.5, 1),
          magnification = SandboxGame.magnificationLevel
        ).noResize,
        SandboxBootData(flags.getOrElse("key", "No entry for 'key'."), gameViewport)
      ).withAssets(
        SandboxAssets.assets ++
          Shaders.assets ++
          Archetype.assets
      ).withFonts(
        Fonts.fontInfo,
        TestFont.fontInfo
      ).withSubSystems(
        FPSCounter[SandboxGameModel](
          Fonts.fontKey,
          SandboxAssets.smallFontName
        )
          .withLayerKey(Constants.LayerKeys.fps)
          .placeAt { (context, bounds) =>
            Point(0, context.frame.viewport.height - bounds.height)
          }
      ).withShaders(
        Shaders.circle,
        Shaders.external,
        Shaders.sea,
        LegacyEffects.entityShader,
        Archetype.shader,
        UVShaders.circle,
        UVShaders.voronoi,
        UVShaders.redBlend
      ).addShaders(Refraction.shaders)
        .addShaders(indigoextras.ui.shaders.all)
    )
  }

  def setup(
      bootData: SandboxBootData,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[SandboxStartupData]] = {
    println(bootData.message)

    val screenCenter: Point =
      bootData.gameViewport.giveDimensions(SandboxGame.magnificationLevel).center

    def makeStartupData(
        aseprite: Aseprite,
        spriteAndAnimations: SpriteAndAnimations,
        clips: Map[CycleLabel, Clip[Material.Bitmap]]
    ): Startup.Success[SandboxStartupData] =
      Startup
        .Success(
          SandboxStartupData(
            Dude(
              aseprite,
              spriteAndAnimations.sprite
                .withRef(16, 16)      // Initial offset, so when talk about his position it's the center of the sprite
                .moveTo(screenCenter) // Also place him in the middle of the screen initially
                .withMaterial(SandboxAssets.dudeMaterial),
              clips
            ),
            screenCenter,
            bootData.gameViewport
          )
        )
        .addAnimations(spriteAndAnimations.animations)

    val res: Option[Startup.Success[SandboxStartupData]] = for {
      json                <- assetCollection.findTextDataByName(AssetName(SandboxAssets.dudeName.toString + "-json"))
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- aseprite.toSpriteAndAnimations(dice, SandboxAssets.dudeName)
      clips               <- aseprite.toClips(SandboxAssets.dudeName)
    } yield makeStartupData(aseprite, spriteAndAnimations, clips)

    Outcome(res.getOrElse(Startup.Failure("Failed to load the dude")))
  }

  def initialModel(startupData: SandboxStartupData): Outcome[SandboxGameModel] =
    Outcome(SandboxModel.initialModel(startupData))

  def updateModel(
      context: Context[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    SandboxModel.updateModel(context, model)

  def present(
      context: Context[SandboxStartupData],
      model: SandboxGameModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Constants.LayerKeys.background -> Layer.Stack.empty,
        Constants.LayerKeys.game       -> Layer.Stack.empty,
        Constants.LayerKeys.windows    -> Layer.Stack.empty,
        Constants.LayerKeys.fps ->
          Layer.empty
            .withCamera(Camera.default)
            .withMagnification(1)
      )
    )

object SandboxGame:

  val magnificationLevel: Int = 2
  val gameWidth: Int          = 228
  val gameHeight: Int         = 128
  val viewportWidth: Int      = gameWidth * magnificationLevel  // 456
  val viewportHeight: Int     = gameHeight * magnificationLevel // 256

final case class Dude(
    aseprite: Aseprite,
    sprite: Sprite[Material.ImageEffects],
    clips: Map[CycleLabel, Clip[Material.Bitmap]]
)
final case class SandboxBootData(message: String, gameViewport: GameViewport)
final case class SandboxStartupData(dude: Dude, viewportCenter: Point, gameViewport: GameViewport)
final case class SandboxViewModel(
    offset: Point,
    useLightingLayer: Boolean,
    captureScreenScene: CaptureScreenScene.ViewModel
)

final case class Log(message: String) extends GlobalEvent
