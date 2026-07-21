package indigo.platform.gameengine

import indigo.core.Outcome
import indigo.core.animation.*
import indigo.core.assets.AssetName
import indigo.core.config.EngineConfig
import indigo.core.datatypes.FontInfo
import indigo.core.datatypes.Rectangle
import indigo.core.dice.Dice
import indigo.core.events.GlobalEvent
import indigo.core.input.GamepadInputCapture
import indigo.core.utils.IndigoLogger
import indigo.platform.IndigoCoreServices
import indigo.platform.NativePlatform
import indigo.platform.assets.*
import indigo.platform.audio.AudioService
import indigo.platform.events.GlobalEventStream
import indigo.platform.locale.NativeLocaleService
import indigo.render.Renderer
import indigo.render.opengl.ContextAndSize
import indigo.render.pipeline.assets.AssetMapping
import indigo.render.pipeline.datatypes.ProcessedSceneData
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

import scala.compiletime.uninitialized

final class GameEngine[StartUpData, GameModel](
    services: IndigoCoreServices[TempImageData, Array[Byte]], // Fake types.
    engineConfig: EngineConfig,
    fonts: Set[FontInfo],
    animations: Set[Animation],
    shaders: Set[ShaderProgram],
    initialise: AssetCollection => Dice => Outcome[Startup[StartUpData]],
    initialModel: StartUpData => Outcome[GameModel],
    frameProccessor: FrameProcessor[StartUpData, GameModel],
    initialisationEvents: Batch[GlobalEvent]
) {

  private val animationsRegister: AnimationsRegister = new AnimationsRegister()
  private val fontRegister: FontRegister             = new FontRegister()
  private val shaderRegister: ShaderRegister         = new ShaderRegister()
  private val boundaryLocator: BoundaryLocator       = new BoundaryLocator(animationsRegister, fontRegister)
  private val sceneProcessor: SceneProcessor = new SceneProcessor(boundaryLocator, animationsRegister, fontRegister)
  private[indigo] val globalEventStream: GlobalEventStream         = new GlobalEventStream()
  private[gameengine] val gamepadInputCapture: GamepadInputCapture = services.gamepadInputCapture
  private[gameengine] val audioService: AudioService               = services.audioService

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var gameLoopInstance: GameLoop[StartUpData, GameModel] = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var accumulatedAssetCollection: AssetCollection = AssetCollection.empty
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private[gameengine] var assetMapping: AssetMapping = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private[gameengine] var renderer: Renderer[ContextAndSize] = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private[gameengine] var startUpData: StartUpData = uninitialized
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var platform: NativePlatform = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var _graphicsContext: ContextAndSize = null

  // Written by start() at boot and by updateAssetCollection() when GameLoop handles
  // IndigoSystemEvent.Rebuild. Read by rebuildGameLoop() to fold into accumulatedAssetCollection.
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var _assetCollection: AssetCollection = null

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def kill(): Unit =

    if platform != null then platform.kill()

    if renderer != null && _graphicsContext != null then renderer.dispose(_graphicsContext)

    animationsRegister.kill()
    fontRegister.kill()
    shaderRegister.kill()
    boundaryLocator.purgeCache()
    sceneProcessor.purgeCaches()
    globalEventStream.kill()
    services.kill()

    ()

  def start(
      // context: ContextAndSize,
      assetCollection: AssetCollection,
      bootEvents: Batch[GlobalEvent]
  ): GameEngine[StartUpData, GameModel] = {

    IndigoLogger.info("Starting Indigo")

    _assetCollection = assetCollection

    // Intialisation / Boot events
    initialisationEvents.foreach(globalEventStream.pushGlobalEvent)
    bootEvents.foreach(globalEventStream.pushGlobalEvent)

    if (engineConfig.autoLoadStandardShaders)
      StandardShaders.all.foreach(shaderRegister.register)
    else shaderRegister.register(StandardShaders.NormalBlend)

    // Arrange config
    IndigoLogger.info("Configuration: " + engineConfig.asString)

    platform = new NativePlatform(
      engineConfig,
      globalEventStream,
      services.imageService
    )

    tryBuildGameLoop()

    this
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def tick(context: ContextAndSize, runningTime: Seconds, timeDelta: Seconds): Unit =
    if context != null then _graphicsContext = context
    tryBuildGameLoop()
    if gameLoopInstance != null then gameLoopInstance.runFrame(runningTime, timeDelta)

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def tryBuildGameLoop(): Unit =
    if gameLoopInstance == null && _graphicsContext != null && _assetCollection != null then
      rebuildGameLoop(true)(Seconds.zero)

  def updateAssetCollection(assetCollection: AssetCollection): Unit =
    _assetCollection = assetCollection

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw", "scalafix:DisableSyntax.null"))
  def rebuildGameLoop(
      firstRun: Boolean
  ): Seconds => Unit =
    runningTime => {
      if !firstRun then
        gameLoopInstance.lock()
        if renderer != null then renderer.dispose(_graphicsContext) // Hmmm.

      fontRegister.clearRegister()
      boundaryLocator.purgeCache()
      sceneProcessor.purgeCaches()

      accumulatedAssetCollection = accumulatedAssetCollection |+| _assetCollection

      services.audioService.addAudioAssets(accumulatedAssetCollection.sounds)

      val dice = if firstRun then Dice.default else Dice.fromSeed(runningTime.toLong)

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
                _graphicsContext,
                shaderRegister.toSet,
                accumulatedAssetCollection
              )
              startUpSuccessData <- GameEngine.initialisedGame(startupData)
              m                  <- modelToUse(startUpSuccessData)
              initialisedGameLoop <- GameEngine.initialiseGameLoop(
                this,
                boundaryLocator,
                sceneProcessor,
                engineConfig,
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

  def resizeAndDraw(sceneData: ProcessedSceneData, runningTime: Seconds): Unit =
    renderer.resize(_graphicsContext)
    renderer.drawScene(_graphicsContext, sceneData, runningTime)

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
      gameEngine: GameEngine[StartUpData, GameModel],
      boundaryLocator: BoundaryLocator,
      sceneProcessor: SceneProcessor,
      engineConfig: EngineConfig,
      initialModel: GameModel,
      frameProccessor: FrameProcessor[StartUpData, GameModel],
      startFrameLocked: Boolean,
      renderer: => Renderer[ContextAndSize]
  ): Outcome[GameLoop[StartUpData, GameModel]] =
    Outcome(
      new GameLoop[StartUpData, GameModel](
        gameEngine.updateAssetCollection,
        gameEngine.rebuildGameLoop(false),
        boundaryLocator,
        sceneProcessor,
        gameEngine,
        engineConfig,
        initialModel,
        frameProccessor,
        startFrameLocked,
        () => Rectangle(renderer.screenWidth, renderer.screenHeight),
        NativeLocaleService()
      )
    )
}
