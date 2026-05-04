package com.example.sandbox

import com.example.sandbox.scenes.*
import example.TestFont
import indigo.*
import indigo.json.Json
import indigoextras.effectmaterials.LegacyEffects
import indigoextras.effectmaterials.Refraction
import indigoextras.subsystems.FPSCounter

final class SandboxGame extends Game[SandboxBootData, SandboxStartupData, SandboxGameModel]:

  val gameId: GameId = GameId("sandbox")

  def initialScene(bootData: SandboxBootData): Option[SceneName] =
    Some(NineSliceScene.name)

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

  def boot(flags: Map[String, String]): Outcome[BootResult[SandboxBootData, SandboxGameModel]] =
    Outcome(
      BootResult(
        GameConfig.default
          .withClearColor(RGBA(0.4, 0.2, 0.5, 1)),
        SandboxBootData(flags.getOrElse("key", "No entry for 'key'."))
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

  def setup(
      bootData: SandboxBootData,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[SandboxStartupData]] = {
    println(bootData.message)

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
                .withRef(16, 16) // Initial offset, so when talk about his position it's the center of the sprite
                .moveTo(SandboxGame.screenCenter) // Also place him in the middle of the screen initially
                .withMaterial(SandboxAssets.dudeMaterial),
              clips
            )
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

  val gameWidth: Int      = 228
  val gameHeight: Int     = 128
  val viewportWidth: Int  = gameWidth * 2  // 456
  val viewportHeight: Int = gameHeight * 2 // 256
  val screenCenter: Point =
    Point(viewportWidth, viewportHeight) / 2

final case class Dude(
    aseprite: Aseprite,
    sprite: Sprite[Material.ImageEffects],
    clips: Map[CycleLabel, Clip[Material.Bitmap]]
)
final case class SandboxBootData(message: String)
final case class SandboxStartupData(dude: Dude)
final case class SandboxViewModel(
    offset: Point,
    useLightingLayer: Boolean
)

final case class Log(message: String) extends GlobalEvent
