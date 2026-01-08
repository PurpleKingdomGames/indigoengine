package indigo.next

import indigo.GameLauncher
import org.scalajs.dom.Element
import tyrian.next.Action
import tyrian.next.GlobalMsg
import tyrian.next.HtmlFragment
import tyrian.next.Result
import tyrian.next.Watcher
import tyrian.next.extensions.Extension
import tyrian.next.extensions.ExtensionId

final class Indigo(
    extensionId: ExtensionId,
    game: IndigoNext[?, ?, ?, ?] | GameLauncher[?, ?, ?],
    find: () => Option[Element],
    onLaunchSuccess: GlobalMsg,
    onLaunchFailure: GlobalMsg
) extends Extension:

  private val MaxAttempts: Int = 10

  type ExtensionModel = Indigo.ExtensionModel

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
        .addActions(Indigo.launchAction(model.game, find))

    case Indigo.LaunchMsg.Started =>
      Result(model)
        .addActions(Action.emit(onLaunchSuccess))
        .log("Indigo Extension successfully launched the game.")

    case Indigo.LaunchMsg.Failed =>
      Result(model)
        .addActions(Action.emit(onLaunchFailure))
        .log(s"Indigo Extention failed to launch the game after $MaxAttempts attempts.")

    case _ =>
      Result(model)

  def view(model: ExtensionModel): HtmlFragment =
    HtmlFragment.empty

  def watchers(model: ExtensionModel): Batch[Watcher] =
    model.game match
      case _: GameLauncher[?, ?, ?] =>
        Batch.empty

      case g: IndigoNext[?, ?, ?, ?] =>
        Batch(g.bridge.watch)

object Indigo:

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def launchAction(game: IndigoNext[?, ?, ?, ?] | GameLauncher[?, ?, ?], find: () => Option[Element]): Action =
    Action.run {
      find() match
        case Some(elem) if elem != null =>
          game.launch(elem, Map.empty[String, String])
          Indigo.LaunchMsg.Started

        case _ =>
          Indigo.LaunchMsg.Retry
    }

  enum LaunchMsg extends GlobalMsg:
    case Retry
    case AttemptStart
    case Started
    case Failed

  final case class ExtensionModel(game: IndigoNext[?, ?, ?, ?] | GameLauncher[?, ?, ?], attempts: Int)
