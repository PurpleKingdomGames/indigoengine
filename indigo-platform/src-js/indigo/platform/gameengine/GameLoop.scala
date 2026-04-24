package indigo.platform.gameengine

import indigo.core.Outcome
import indigo.core.config.GameConfig
import indigo.core.config.GameViewport
import indigo.core.dice.Dice
import indigo.core.events.FrameTick
import indigo.core.events.InputEvent
import indigo.core.events.InputState
import indigo.core.time.GameTime
import indigo.core.utils.IndigoLogger
import indigo.gameengine.FrameProcessor
import indigo.platform.assets.AssetCollection
import indigo.render.Renderer
import indigo.render.pipeline.sceneprocessing.SceneProcessor
import indigo.scenegraph.SceneUpdateFragment
import indigo.scenegraph.registers.BoundaryLocator
import indigo.shared.Context
import indigo.shared.IndigoSystemEvent
import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.Seconds

import scala.collection.mutable

final class GameLoop[StartUpData, GameModel](
    rebuildGameLoop: AssetCollection => Seconds => Unit,
    boundaryLocator: BoundaryLocator,
    sceneProcessor: SceneProcessor,
    gameEngine: GameEngine[StartUpData, GameModel],
    gameConfig: GameConfig,
    initialModel: GameModel,
    frameProcessor: FrameProcessor[StartUpData, GameModel],
    startFrameLocked: Boolean,
    renderer: => Renderer
):

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _gameModelState: GameModel = initialModel
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _inputState: InputState = InputState.default
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _frameLocked: Boolean = startFrameLocked

  private val systemActions: mutable.Queue[IndigoSystemEvent] =
    new mutable.Queue[IndigoSystemEvent]()

  private val _randomInstance: scala.util.Random = new scala.util.Random()

  private lazy val _services: Context.Services =
    Context.Services(boundaryLocator, _randomInstance, renderer.captureScreen)

  def gameModelState: GameModel = _gameModelState
  def lock(): Unit              = _frameLocked = true
  def unlock(): Unit            = _frameLocked = false

  def runFrame(time: Seconds, timeDelta: Seconds): Unit =
    if _frameLocked then ()
    else if systemActions.size > 0 then performSystemActions(systemActions.dequeueAll(_ => true).toList, time)
    else runFrameNormal(time, timeDelta)

  def performSystemActions(systemEvents: List[IndigoSystemEvent], runningTime: Seconds): Unit =
    systemEvents.foreach { case IndigoSystemEvent.Rebuild(assetCollection, nextEvent) =>
      IndigoLogger.info("Rebuilding game loop from new asset collection.")
      rebuildGameLoop(assetCollection)(runningTime)
      gameEngine.globalEventStream.pushGlobalEvent(nextEvent)
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def runFrameNormal(time: Seconds, timeDelta: Seconds): Unit =
    val gameTime =
      new GameTime(time, timeDelta)
    val events = gameEngine.globalEventStream.collect ++ Batch(FrameTick)

    // Persist input state
    _inputState = InputState.calculateNext(
      _inputState,
      events.collect { case e: InputEvent => e },
      gameEngine.gamepadInputCapture.giveGamepadState,
      gameTime.running.toMillis
    )

    val context =
      new Context[StartUpData](
        gameEngine.startUpData,
        Context.Frame(
          Dice.fromSeconds(gameTime.running),
          gameTime,
          _inputState,
          GameViewport(renderer.screenWidth, renderer.screenHeight),
          gameConfig.magnification
        ),
        _services
      )

    // Run the frame processor
    val processedFrame: Outcome[(GameModel, SceneUpdateFragment)] =
      frameProcessor.run(
        _gameModelState,
        events,
        context
      )

    // Persist frame state
    val scene =
      processedFrame match
        case oe @ Outcome.Error(e, _) =>
          IndigoLogger.error("The game has crashed...")
          IndigoLogger.error(oe.reportCrash)
          throw e

        case Outcome.Result((gameModel, sceneUpdateFragment), globalEvents) =>
          _gameModelState = gameModel

          globalEvents.foreach(e => gameEngine.globalEventStream.pushGlobalEvent(e))

          sceneUpdateFragment

    // Play audio
    gameEngine.audioPlayer.playAudio(scene.audio)

    // Prepare scene
    val sceneData = sceneProcessor.processScene(
      gameTime,
      scene,
      gameEngine.assetMapping,
      gameConfig.advanced.batchSize,
      events,
      gameEngine.globalEventStream.pushGlobalEvent
    )

    // Render scene
    gameEngine.renderer.drawScene(sceneData, gameTime.running)

    // Process system events
    events
      .collect { case e: IndigoSystemEvent => e }
      .foreach(systemActions.enqueue)
