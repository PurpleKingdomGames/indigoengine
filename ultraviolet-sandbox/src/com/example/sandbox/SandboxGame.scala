package com.example.sandbox

import com.example.sandbox.scenes.*
import com.example.sandbox.shaders.*
import indigo.*
import indigo.json.Json

final class SandboxGame extends Game[SandboxBootData, SandboxStartupData, SandboxGameModel]:

  val gameId: GameId = GameId("sandbox")

  def initialScene(bootData: SandboxBootData): Option[SceneName] =
    Some(NoiseScene.name)

  def scenes(bootData: SandboxBootData): NonEmptyBatch[
    Scene[SandboxStartupData, SandboxGameModel]
  ] =
    NonEmptyBatch(
      OriginalScene,
      ShadersScene,
      NoiseScene
    )

  val eventFilters: EventFilters = EventFilters.Permissive

  def boot(
      flags: Map[String, String]
  ): Outcome[BootResult[SandboxBootData, SandboxGameModel]] =
    Outcome(
      BootResult(
        GameConfig.default
          .withClearColor(RGBA(0.4, 0.2, 0.5, 1)),
        SandboxBootData(
          flags.getOrElse("key", "No entry for 'key'.")
        )
      ).withAssets(
        SandboxAssets.assets ++
          Shaders.assets
      ).withFonts(Fonts.fontInfo)
        .withShaders(
          Shaders.circle,
          Shaders.external,
          Shaders.sea,
          BoxShader.shader,
          CircleShader.shader,
          HexagonShader.shader,
          SegmentShader.shader,
          StarShader.shader,
          TriangleShader.shader,
          CellularNoiseShader.shader,
          PerlinNoiseShader.shader,
          GradientNoiseShader.shader,
          SimplexNoiseShader.shader,
          WhiteNoiseShader.shader
        )
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
                .withMaterial(SandboxAssets.dudeMaterial),
              clips
            )
          )
        )
        .addAnimations(spriteAndAnimations.animations)

    val res: Option[Startup.Success[SandboxStartupData]] = for {
      json <- assetCollection.findTextDataByName(
        AssetName(SandboxAssets.dudeName.toString + "-json")
      )
      aseprite <- Json.asepriteFromJson(json)
      spriteAndAnimations <- aseprite.toSpriteAndAnimations(
        dice,
        SandboxAssets.dudeName
      )
      clips <- aseprite.toClips(SandboxAssets.dudeName)
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
      SceneUpdateFragment.empty
    )

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
