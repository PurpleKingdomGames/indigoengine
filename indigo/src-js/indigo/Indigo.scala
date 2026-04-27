package indigo

import cats.effect.IO
import indigo.core.time.FPS
import indigo.internal.CanvasAndContext
import indigo.internal.WorldEventWatchers
import indigo.platform.events.GlobalEventCallback
import indigo.render.facades.WebGL2RenderingContext
import org.scalajs.dom.document
import org.scalajs.dom.html
import tyrian.*
import tyrian.Html.*
import tyrian.classic.Sub
import tyrian.extensions.Extension
import tyrian.extensions.ExtensionId
import tyrian.syntax.*

final case class Indigo(
    extensionId: ExtensionId,
    flags: Map[String, String],
    game: Game[?, ?, ?],
    containerMarkerId: MarkerId,
    onLaunchSuccess: Option[GlobalMsg],
    onLaunchFailure: Option[GlobalMsg],
    eventMapping: PartialIso[GlobalMsg, GlobalEvent],
    frameRatePolicy: FrameRatePolicy
) extends Extension:

  type ExtensionModel = Indigo.ExtensionModel

  def withExtensionId(value: ExtensionId): Indigo =
    this.copy(extensionId = value)

  def withFlags(value: Map[String, String]): Indigo =
    this.copy(flags = value)

  def withGame(value: Game[?, ?, ?]): Indigo =
    this.copy(game = value)

  def withOnLaunchSuccess(value: Option[GlobalMsg]): Indigo =
    this.copy(onLaunchSuccess = value)
  def sendLaunchSuccessMsg(value: GlobalMsg): Indigo =
    this.copy(onLaunchSuccess = Some(value))
  def noLaunchSuccessMsg: Indigo =
    this.copy(onLaunchSuccess = None)

  def withOnLaunchFailure(value: Option[GlobalMsg]): Indigo =
    this.copy(onLaunchFailure = value)
  def sendLaunchFailureMsg(value: GlobalMsg): Indigo =
    this.copy(onLaunchFailure = Some(value))
  def noLaunchFailureMsg: Indigo =
    this.copy(onLaunchFailure = None)

  def withEventMapping(value: PartialIso[GlobalMsg, GlobalEvent]): Indigo =
    this.copy(eventMapping = value)

  def withFrameRatePolicy(value: FrameRatePolicy): Indigo =
    this.copy(frameRatePolicy = value)
  def unlimitedFrameRate: Indigo =
    withFrameRatePolicy(FrameRatePolicy.Unlimited)
  def targetFrameRate(target: FPS): Indigo =
    withFrameRatePolicy(FrameRatePolicy.Skip(target))

  def id: ExtensionId = extensionId

  def init: Result[ExtensionModel] =
    Result(Indigo.ExtensionModel(game))
      .addGlobalMsgs(Indigo.Msg.Launch(Indigo.LaunchStatus.AttemptStart(extensionId)))

  def update(model: ExtensionModel): GlobalMsg => Result[ExtensionModel] =
    case m: Indigo.Msg =>
      handleMsg(model)(m)

    case msg =>
      eventMapping
        .to(msg)
        .foreach: e =>
          model.game.events.push(e)

      Result(model)

  private def handleMsg(model: Indigo.ExtensionModel): Indigo.Msg => Result[Indigo.ExtensionModel] =
    case Indigo.Msg.WorldEvents(events) =>
      events.foreach: e =>
        model.game.events.push(e)

      Result(model)

    case Indigo.Msg.Halt(gameId) =>
      if game.gameId == gameId then
        Result(model.copy(running = false))
          .addActions(
            Action.sideEffect {
              game.system.halt()
            }
          )
      else Result(model)

    case Indigo.Msg.GameTick(gameId, runningTime) =>
      if game.gameId == gameId && model.running then
        Indigo
          .processFrameTick(
            model.lastUpdated,
            runningTime,
            frameRatePolicy
          )
          .flatMap {
            case Indigo.TickUpdateResult.Wait =>
              Result(model)

            case Indigo.TickUpdateResult.RunNow(timeDelta, updatedAt) =>
              Result(model.copy(lastUpdated = updatedAt))
                .addActions(
                  Action.sideEffect {
                    game.system.tick(updatedAt, timeDelta)
                  }
                )
          }
      else Result(model)

    case Indigo.Msg.Launch(Indigo.LaunchStatus.Retry(extId)) =>
      if extId == extensionId && model.attempts <= 0 then
        Result(model)
          .addActions(Action.emit(Indigo.Msg.Launch(Indigo.LaunchStatus.Failed(extId))))
      else if extId == extensionId then
        val nextDelay =
          val x = Indigo.MaxStartupAttempts - model.attempts
          Millis(x * x * 100L)

        Result(model.copy(attempts = model.attempts - 1))
          .addActions(
            Action.emitAfterDelay(Indigo.Msg.Launch(Indigo.LaunchStatus.AttemptStart(extensionId)), nextDelay)
          )
          .log(
            s"Indigo Extension failed to find the required container element in the dom, will retry in ${nextDelay.toSeconds.toString()} seconds..."
          )
      else Result(model)

    case Indigo.Msg.Launch(Indigo.LaunchStatus.AttemptStart(extId)) =>
      if extId == extensionId then
        val maybeCanvas =
          Option(document.getElementById(Indigo.CanvasId))
            .flatMap(e => if e.isInstanceOf[html.Canvas] then Option(e.asInstanceOf[html.Canvas]) else None)

        Result(
          model.copy(
            _eventWatchers = maybeCanvas.map(WorldEventWatchers.init)
          )
        )
          .addActions(
            Indigo.launchAction(
              extensionId,
              model.game,
              maybeCanvas,
              flags
            )
          )
      else Result(model)

    case Indigo.Msg.Launch(Indigo.LaunchStatus.Started(extId)) =>
      if extId == extensionId then
        onLaunchSuccess match
          case None =>
            Result(model)

          case Some(msg) =>
            Result(model)
              .addGlobalMsgs(msg)
              .log("Indigo Extension successfully launched the game.")
      else Result(model)

    case Indigo.Msg.Launch(Indigo.LaunchStatus.Failed(extId)) =>
      if extId == extensionId then
        onLaunchFailure match
          case None =>
            Result(model)

          case Some(msg) =>
            Result(model)
              .addGlobalMsgs(msg)
              .log(s"Indigo Extension failed to launch the game after ${Indigo.MaxStartupAttempts} attempts.")
      else Result(model)

  def view(model: ExtensionModel): HtmlFragment =
    // TODO: Size?
    HtmlFragment.insert(
      containerMarkerId,
      canvas(tyrian.Html.id := Indigo.CanvasId, width := 800, height := 600)()
    )

  def watchers(model: ExtensionModel): Batch[Watcher] =
    val gameTickWatcher =
      if model.running then Batch(Indigo.tick(game.gameId))
      else Batch.empty

    val worldEventWatchers =
      model._eventWatchers match
        case None    => Batch.empty
        case Some(w) => w.watchers

    Batch.fromOption(
      model.game.events.eventCallback.map: eventCallback =>
        Indigo.indigoEventWatcher(extensionId, eventMapping, eventCallback)
    ) ++
      gameTickWatcher ++ worldEventWatchers

object Indigo:

  val CanvasId: String = "indigo-canvas"

  val MaxStartupAttempts: Int = 10

  def apply(
      extensionId: ExtensionId,
      flags: Map[String, String],
      game: Game[?, ?, ?],
      containerMarkerId: MarkerId
  ): Indigo =
    Indigo(
      extensionId,
      flags,
      game,
      containerMarkerId,
      None,
      None,
      PartialIso.none,
      FrameRatePolicy.Skip(FPS.`60`)
    )

  def apply(
      extensionId: ExtensionId,
      flags: Map[String, String],
      game: Game[?, ?, ?],
      containerMarkerId: MarkerId,
      onLaunchSuccess: GlobalMsg,
      onLaunchFailure: GlobalMsg
  ): Indigo =
    Indigo(
      extensionId,
      flags,
      game,
      containerMarkerId,
      Some(onLaunchSuccess),
      Some(onLaunchFailure),
      PartialIso.none,
      FrameRatePolicy.Skip(FPS.`60`)
    )

  private def launchAction(
      extensionId: ExtensionId,
      game: Game[?, ?, ?],
      maybeCanvas: Option[html.Canvas],
      flags: Map[String, String]
  ): Action =
    Action.run {
      maybeCanvas match
        case Some(canvas) =>
          val context: WebGL2RenderingContext = CanvasAndContext.setupContext(canvas)

          game.launch(canvas, context, flags)
          Indigo.Msg.Launch(LaunchStatus.Started(extensionId))

        case _ =>
          Indigo.Msg.Launch(LaunchStatus.Retry(extensionId))
    }

  final case class ExtensionModel(
      game: Game[?, ?, ?],
      attempts: Int,
      lastUpdated: Seconds,
      running: Boolean,
      _eventWatchers: Option[WorldEventWatchers]
  )
  object ExtensionModel:
    def apply(game: Game[?, ?, ?]): ExtensionModel =
      ExtensionModel(
        game,
        MaxStartupAttempts,
        Seconds.zero,
        running = true,
        None
      )

  private def indigoEventWatcher(
      extensionId: ExtensionId,
      eventMapping: PartialIso[GlobalMsg, GlobalEvent],
      globalEventStream: GlobalEventCallback
  ): Watcher =
    val sub = Sub.Observe[IO, GlobalEvent, GlobalMsg, Unit](
      id = "indigo-event-exchange-" + extensionId.toString,
      acquire = (callback: Either[Throwable, GlobalEvent] => Unit) =>
        IO(
          globalEventStream.registerEventCallback(event => callback(Right(event)))
        ),
      release = (_: Unit) =>
        IO(
          globalEventStream.clearEventCallback()
        ),
      toMsg = (event: GlobalEvent) => eventMapping.from(event)
    )
    Watcher(sub)

  private[indigo] def processFrameTick(
      lastUpdated: Seconds,
      runningTime: Seconds,
      frameRatePolicy: FrameRatePolicy
  ): Result[TickUpdateResult] =
    val timeSinceLastUpdate = runningTime - lastUpdated

    frameRatePolicy match
      case FrameRatePolicy.Unlimited =>
        Result(TickUpdateResult.RunNow(timeSinceLastUpdate, runningTime))

      case FrameRatePolicy.Skip(target) =>
        val targetFrameDuration = target.asFrameDuration // E.g. 16.7ms or 0.016s for 60fps

        if timeSinceLastUpdate >= targetFrameDuration then
          Result(TickUpdateResult.RunNow(timeSinceLastUpdate, runningTime))
        else Result(TickUpdateResult.Wait)

  def tick(gameId: GameId): Watcher =
    Watcher.animationFrameTick(s"[indigo-tick:${gameId.asString}]") { runningTime =>
      Indigo.Msg.GameTick(gameId, runningTime)
    }

  enum LaunchStatus:
    case Retry(extensionId: ExtensionId)
    case AttemptStart(extensionId: ExtensionId)
    case Started(extensionId: ExtensionId)
    case Failed(extensionId: ExtensionId)

  enum Msg extends GlobalMsg:
    case GameTick(gameId: GameId, runningTime: Seconds)
    case Halt(gameId: GameId)
    case Launch(status: LaunchStatus)
    case WorldEvents(events: Batch[GlobalEvent])

  enum TickUpdateResult derives CanEqual:
    case Wait
    case RunNow(timeDelta: Seconds, updatedAt: Seconds)
