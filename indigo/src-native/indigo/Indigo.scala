package indigo

import indigo.bridge.BridgeMsg
import indigo.launchers.GameLauncher
import tyrian.Action
import tyrian.GlobalMsg
import tyrian.Result
import tyrian.TerminalFragment
import tyrian.Watcher
import tyrian.extensions.Extension
import tyrian.extensions.ExtensionId

final case class Indigo(
    extensionId: ExtensionId,
    args: Array[String],
    game: Game[?, ?, ?] | GameLauncher[?, ?],
    onLaunchSuccess: Option[GlobalMsg],
    onLaunchFailure: Option[GlobalMsg]
) extends Extension:

  private val MaxAttempts: Int = 10

  type ExtensionModel = Indigo.ExtensionModel

  def withExtensionId(value: ExtensionId): Indigo =
    this.copy(extensionId = value)

  def withArgs(value: Array[String]): Indigo =
    this.copy(args = value)

  def withGame(value: Game[?, ?, ?] | GameLauncher[?, ?]): Indigo =
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

  def id: ExtensionId = extensionId

  def init: Result[ExtensionModel] =
    Result(Indigo.ExtensionModel(game, MaxAttempts))
      .addGlobalMsgs(Indigo.LaunchMsg.AttemptStart)

  def update(model: ExtensionModel): GlobalMsg => Result[ExtensionModel] =
    case Indigo.LaunchMsg.Retry if model.attempts <= 0 =>
      Result(model)
        .addActions(Action.emit(Indigo.LaunchMsg.Failed))

    case Indigo.LaunchMsg.Retry =>
      val nextDelay =
        val x = MaxAttempts - model.attempts
        Millis(x * x * 100L)

      Result(model.copy(attempts = model.attempts - 1))
        .addActions(Action.emitAfterDelay(Indigo.LaunchMsg.AttemptStart, nextDelay))
        .log(
          s"Indigo Extension failed to find the required container element in the dom, will retry in ${nextDelay.toSeconds.toString()} seconds..."
        )

    case Indigo.LaunchMsg.AttemptStart =>
      Result(model)
        .addActions(Indigo.launchAction(model.game, args))

    case Indigo.LaunchMsg.Started =>
      onLaunchSuccess match
        case None =>
          Result(model)

        case Some(msg) =>
          Result(model)
            .addGlobalMsgs(msg)
            .log("Indigo Extension successfully launched the game.")

    case Indigo.LaunchMsg.Failed =>
      onLaunchFailure match
        case None =>
          Result(model)

        case Some(msg) =>
          Result(model)
            .addGlobalMsgs(msg)
            .log(s"Indigo Extention failed to launch the game after $MaxAttempts attempts.")

    case BridgeMsg.Send(data) =>
      game match
        case _: GameLauncher[?, ?] =>
          Result(model)

        case g: Game[?, ?, ?] =>
          Result(model)
            .addActions(g.bridge.send(data))

    case _ =>
      Result(model)

  def view(model: ExtensionModel): TerminalFragment =
    TerminalFragment.empty

  def watchers(model: ExtensionModel): Batch[Watcher] =
    model.game match
      case _: GameLauncher[?, ?] =>
        Batch.empty

      case g: Game[?, ?, ?] =>
        Batch(g.bridge.watch)

object Indigo:

  def apply(
      extensionId: ExtensionId,
      args: Array[String],
      game: Game[?, ?, ?] | GameLauncher[?, ?]
  ): Indigo =
    Indigo(
      extensionId,
      args,
      game,
      None,
      None
    )

  def apply(
      extensionId: ExtensionId,
      args: Array[String],
      game: Game[?, ?, ?] | GameLauncher[?, ?],
      onLaunchSuccess: GlobalMsg,
      onLaunchFailure: GlobalMsg
  ): Indigo =
    Indigo(
      extensionId,
      args,
      game,
      Some(onLaunchSuccess),
      Some(onLaunchFailure)
    )

  private def launchAction(
      game: Game[?, ?, ?] | GameLauncher[?, ?],
      args: Array[String]
  ): Action =
    Action.run {
      game.launch(args)
      Indigo.LaunchMsg.Started
    }

  enum LaunchMsg extends GlobalMsg:
    case Retry
    case AttemptStart
    case Started
    case Failed

  final case class ExtensionModel(game: Game[?, ?, ?] | GameLauncher[?, ?], attempts: Int)
