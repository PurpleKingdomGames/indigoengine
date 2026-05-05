package indigo.platform.gameengine

import indigo.core.Outcome
import indigo.core.animation.*
import indigo.core.assets.AssetName
import indigo.core.assets.AssetType
import indigo.core.config.GameConfig
import indigo.core.datatypes.FontInfo
import indigo.core.dice.Dice
import indigo.core.events.GlobalEvent
import indigo.core.input.GamepadInputCapture
import indigo.core.utils.IndigoLogger
import indigo.gameengine.FrameProcessor
import indigo.platform.IndigoCoreServices
import indigo.platform.JsPlatform
import indigo.platform.assets.*
import indigo.platform.audio.AudioPlayer
import indigo.platform.events.GlobalEventStream
import indigo.render.Renderer
import indigo.render.facades.WebGL2RenderingContext
import indigo.render.pipeline.assets.AssetMapping
import indigo.render.pipeline.sceneprocessing.SceneProcessor
import indigo.scenegraph.registers.AnimationsRegister
import indigo.scenegraph.registers.BoundaryLocator
import indigo.scenegraph.registers.FontRegister
import indigo.shaders.BlendShader
import indigo.shaders.EntityShader
import indigo.shaders.ShaderProgram
import indigo.shaders.ShaderRegister
import indigo.shaders.StandardShaders
import indigo.shaders.UltravioletShader
import indigo.shared.Startup
import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.Seconds
import org.scalajs.dom.html
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.*

import scala.compiletime.uninitialized
import scala.concurrent.Future

final class GameEngine[StartUpData, GameModel](
    fonts: Set[FontInfo],
    animations: Set[Animation],
    shaders: Set[ShaderProgram],
    initialise: AssetCollection => Dice => Outcome[Startup[StartUpData]],
    initialModel: StartUpData => Outcome[GameModel],
    frameProccessor: FrameProcessor[StartUpData, GameModel],
    initialisationEvents: Batch[GlobalEvent]
) {

  // TODO: A lot of this should probably be marked as private.

  val stepsToLoad = 4
  val animationsRegister: AnimationsRegister =
    new AnimationsRegister()
  val fontRegister: FontRegister =
    new FontRegister()
  val shaderRegister: ShaderRegister =
    new ShaderRegister()

  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(animationsRegister, fontRegister)
  val sceneProcessor: SceneProcessor =
    new SceneProcessor(boundaryLocator, animationsRegister, fontRegister)

  val audioPlayer: AudioPlayer =
    AudioPlayer.init

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  var gameConfig: GameConfig = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  var globalEventStream: GlobalEventStream = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  var gamepadInputCapture: GamepadInputCapture = null

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  var gameLoopInstance: GameLoop[StartUpData, GameModel] = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var accumulatedAssetCollection: AssetCollection = AssetCollection.empty
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  var assetMapping: AssetMapping = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  var renderer: Renderer = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var startUpData: StartUpData = uninitialized
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  var platform: JsPlatform = null

  def kill(): Unit =
    platform.kill()
    animationsRegister.kill()
    fontRegister.kill()
    shaderRegister.kill()
    boundaryLocator.purgeCache()
    sceneProcessor.purgeCaches()
    audioPlayer.kill()
    globalEventStream.kill()

    ()

  def start(
      canvas: html.Canvas,
      context: WebGL2RenderingContext,
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]],
      bootEvents: Batch[GlobalEvent],
      services: IndigoCoreServices
  ): GameEngine[StartUpData, GameModel] = {

    IndigoLogger.info("Starting Indigo")

    globalEventStream = new GlobalEventStream(audioPlayer, platform)
    gamepadInputCapture = services.gamepadInputCapture

    // Intialisation / Boot events
    initialisationEvents.foreach(globalEventStream.pushGlobalEvent)
    bootEvents.foreach(globalEventStream.pushGlobalEvent)

    if (config.autoLoadStandardShaders)
      StandardShaders.all.foreach(shaderRegister.register)
    else shaderRegister.register(StandardShaders.NormalBlend)

    // Arrange config
    configAsync.map(_.getOrElse(config)).foreach { gc =>
      gameConfig = gc

      IndigoLogger.info("Configuration: " + gameConfig.asString)

      // Arrange initial asset load
      IndigoLogger.info("Attempting to load assets")

      assetsAsync.flatMap(aa => AssetLoader.loadAssets(aa ++ assets)).foreach { assetCollection =>
        IndigoLogger.info("Asset load complete")

        rebuildGameLoop(canvas, context, true)(assetCollection)(Seconds.zero)
      }

    }

    this
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def tick(runningTime: Seconds, timeDelta: Seconds): Unit =
    if gameLoopInstance != null then gameLoopInstance.runFrame(runningTime, timeDelta)

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def rebuildGameLoop(
      canvas: html.Canvas,
      context: WebGL2RenderingContext,
      firstRun: Boolean
  ): AssetCollection => Seconds => Unit =
    ac =>
      runningTime => {
        if !firstRun then gameLoopInstance.lock()

        fontRegister.clearRegister()
        boundaryLocator.purgeCache()
        sceneProcessor.purgeCaches()

        accumulatedAssetCollection = accumulatedAssetCollection |+| ac

        audioPlayer.addAudioAssets(accumulatedAssetCollection.sounds)

        val dice = if firstRun then Dice.default else Dice.fromSeed(runningTime.toLong)

        if firstRun then
          platform = new JsPlatform(
            gameConfig,
            globalEventStream
          )

        initialise(accumulatedAssetCollection)(dice) match {
          case oe @ Outcome.Error(error, _) =>
            val msg =
              if (firstRun) "Error during first initialisation - Halting."
              else "Error during re-initialisation - Halting."

            IndigoLogger.error(msg)
            IndigoLogger.error("Crash report:")
            IndigoLogger.error(oe.reportCrash)
            throw error

          case Outcome.Result(startupData, globalEvents) =>
            globalEvents.foreach(globalEventStream.pushGlobalEvent)

            GameEngine.registerAnimations(animationsRegister, animations ++ startupData.additionalAnimations)

            GameEngine.registerFonts(fontRegister, fonts ++ startupData.additionalFonts)

            GameEngine.registerShaders(
              shaderRegister,
              shaders ++ startupData.additionalShaders,
              accumulatedAssetCollection
            )

            def modelToUse(startUpSuccessData: => StartUpData): Outcome[GameModel] =
              if (firstRun) initialModel(startUpSuccessData)
              else Outcome(gameLoopInstance.gameModelState)

            val loop: Outcome[Unit] =
              for {
                rendererAndAssetMapping <- platform.initialise(
                  shaderRegister.toSet,
                  accumulatedAssetCollection,
                  canvas,
                  context
                )
                startUpSuccessData <- GameEngine.initialisedGame(startupData)
                m                  <- modelToUse(startUpSuccessData)
                initialisedGameLoop <- GameEngine.initialiseGameLoop(
                  canvas,
                  context,
                  this,
                  boundaryLocator,
                  sceneProcessor,
                  gameConfig,
                  m,
                  frameProccessor,
                  !firstRun, // If this isn't the first run, start with it frame locked.
                  renderer
                )
              } yield {
                renderer = rendererAndAssetMapping._1
                assetMapping = rendererAndAssetMapping._2
                gameLoopInstance = initialisedGameLoop
                startUpData = startUpSuccessData
                ()
              }

            loop match {
              case Outcome.Result(_, events) =>
                IndigoLogger.info("Starting main loop, there will be no more info log messages.")
                IndigoLogger.info("You may get first occurrence error logs.")

                events.foreach(globalEventStream.pushGlobalEvent)

                gameLoopInstance.unlock()

                ()

              case oe @ Outcome.Error(e, _) =>
                val msg =
                  if (firstRun) "Error during first engine start up - Halting."
                  else "Error during engine restart - Halting."

                IndigoLogger.error(msg)
                IndigoLogger.error(oe.reportCrash)
                throw e
            }

        }
      }

}

object GameEngine {

  def registerAnimations(animationsRegister: AnimationsRegister, animations: Set[Animation]): Unit =
    animations.foreach(animationsRegister.register)

  def registerFonts(fontRegister: FontRegister, fonts: Set[FontInfo]): Unit =
    fonts.foreach(fontRegister.register)

  def registerShaders(
      shaderRegister: ShaderRegister,
      shaders: Set[ShaderProgram],
      assetCollection: AssetCollection
  ): Unit =
    shaders.foreach {
      case s: EntityShader.Source =>
        shaderRegister.remove(s.id)
        shaderRegister.registerEntityShader(s)

      case s: EntityShader.External =>
        shaderRegister.remove(s.id)
        shaderRegister.registerEntityShader(externalEntityShaderToSource(s, assetCollection))

      case s: BlendShader.Source =>
        shaderRegister.remove(s.id)
        shaderRegister.registerBlendShader(s)

      case s: BlendShader.External =>
        shaderRegister.remove(s.id)
        shaderRegister.registerBlendShader(externalBlendShaderToSource(s, assetCollection))

      case s: UltravioletShader =>
        shaderRegister.remove(s.id)
        shaderRegister.registerUVShader(s)
    }

  def externalEntityShaderToSource(
      external: EntityShader.External,
      assetCollection: AssetCollection
  ): EntityShader.Source =
    EntityShader.Source(
      id = external.id,
      vertex = external.vertex
        .map(a => extractShaderCode(assetCollection.findTextDataByName(a), "indigo-vertex", a))
        .getOrElse(ShaderProgram.defaultVertexProgram),
      fragment = external.fragment
        .map(a => extractShaderCode(assetCollection.findTextDataByName(a), "indigo-fragment", a))
        .getOrElse(ShaderProgram.defaultFragmentProgram),
      prepare = external.prepare
        .map(a => extractShaderCode(assetCollection.findTextDataByName(a), "indigo-prepare", a))
        .getOrElse(ShaderProgram.defaultPrepareProgram),
      light = external.light
        .map(a => extractShaderCode(assetCollection.findTextDataByName(a), "indigo-light", a))
        .getOrElse(ShaderProgram.defaultLightProgram),
      composite = external.composite
        .map(a => extractShaderCode(assetCollection.findTextDataByName(a), "indigo-composite", a))
        .getOrElse(ShaderProgram.defaultCompositeProgram)
    )

  def externalBlendShaderToSource(
      external: BlendShader.External,
      assetCollection: AssetCollection
  ): BlendShader.Source =
    BlendShader.Source(
      id = external.id,
      vertex = external.vertex
        .map(a => extractShaderCode(assetCollection.findTextDataByName(a), "indigo-vertex", a))
        .getOrElse(ShaderProgram.defaultVertexProgram),
      fragment = external.fragment
        .map(a => extractShaderCode(assetCollection.findTextDataByName(a), "indigo-fragment", a))
        .getOrElse(ShaderProgram.defaultFragmentProgram)
    )

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def extractShaderCode(maybeText: Option[String], tag: String, assetName: AssetName): String =
    maybeText.flatMap(s"""//<$tag>[\r\n|\r|\n]((.|\n|\r)*)//</$tag>""".r.findFirstIn) match {
      case Some(program) =>
        program

      case None =>
        val msg = s"Error parsing external shader could not match '$tag' tag pair in asset '$assetName' - Halting."
        IndigoLogger.error(msg)
        throw new Exception(msg)
    }

  def initialisedGame[StartUpData](startupData: Startup[StartUpData]): Outcome[StartUpData] =
    startupData match {
      case e: Startup.Failure =>
        IndigoLogger.info("Game initialisation failed")
        IndigoLogger.info(e.report)
        Outcome.raiseError(new Exception("Game aborted due to start up failure"))

      case x: Startup.Success[?] =>
        IndigoLogger.info("Game initialisation succeeded")
        Outcome(x.success)
    }

  def initialiseGameLoop[StartUpData, GameModel, ViewModel](
      canvas: html.Canvas,
      context: WebGL2RenderingContext,
      gameEngine: GameEngine[StartUpData, GameModel],
      boundaryLocator: BoundaryLocator,
      sceneProcessor: SceneProcessor,
      gameConfig: GameConfig,
      initialModel: GameModel,
      frameProccessor: FrameProcessor[StartUpData, GameModel],
      startFrameLocked: Boolean,
      renderer: => Renderer
  ): Outcome[GameLoop[StartUpData, GameModel]] =
    Outcome(
      new GameLoop[StartUpData, GameModel](
        gameEngine.rebuildGameLoop(canvas, context, false),
        boundaryLocator,
        sceneProcessor,
        gameEngine,
        gameConfig,
        initialModel,
        frameProccessor,
        startFrameLocked,
        renderer
      )
    )
}
