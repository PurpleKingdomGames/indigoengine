package tyrian

import cats.effect.kernel.Async
import cats.effect.kernel.Clock
import cats.effect.kernel.Ref
import cats.effect.std.Dispatcher
import tyrian.platform.Cmd
import tyrian.platform.Sub
import tyrian.platform.runtime.PresentView
import tyrian.platform.runtime.TyrianRuntime

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianApp[F[_]: Async, Msg, Model]:

  /** Specifies the number of queued tasks that can be consumed at any one time. Default is 1024 which is assumed to be
    * more than sufficient, however the value can be tweaked in your app by overriding this value.
    */
  def MaxConcurrentTasks: Int = 1024

  val run: F[Nothing] => Unit

  /** Used to initialise your app. Accepts simple flags and produces the initial model state, along with any commands to
    * run at start up, in order to trigger other processes.
    */
  def init(flags: Map[String, String]): (Model, Cmd[F, Msg])

  /** The update method allows you to modify the model based on incoming messages (events). As well as an updated model,
    * you can also produce commands to run.
    */
  def update(model: Model): Msg => (Model, Cmd[F, Msg])

  /** Used to render your current model into an HTML view.
    */
  def view(model: Model): NativeView[Msg]

  /** Subscriptions are typically processes that run for a period of time and emit discrete events based on some world
    * event, e.g. a mouse moving might emit it's coordinates.
    */
  def subscriptions(model: Model): Sub[F, Msg]

  /** Launch the app and attach it to an element with the given id, with the supplied simple flags. Can only be called
    * from Scala.
    */
  def launch(flags: Map[String, String]): Unit =
    ready(flags)

  private def _init(flags: Map[String, String]): (Model, Cmd[F, Msg]) =
    val (m, cmd) = init(flags)
    (m, cmd)

  private def _update(model: Model): Msg => (Model, Cmd[F, Msg]) =
    msg => update(model)(msg)

  private def _view(model: Model): NativeView[Msg] =
    view(model)

  private def _subscriptions(model: Model): Sub[F, Msg] =
    subscriptions(model)

  def ready(flags: Map[String, String]): Unit =
    run(
      TyrianApp.start[F, Model, Msg](
        _init(flags),
        _update,
        _view,
        _subscriptions
      )
    )

object TyrianApp:

  /** Directly starts the app. Computes the initial state of the given application, renders it on the given DOM element,
    * and listens to user actions
    * @param init
    *   initial state
    * @param update
    *   state transition function
    * @param view
    *   view function
    * @param subscriptions
    *   subscriptions function
    * @tparam F
    *   The effect type to use, e.g. `IO`
    * @tparam Model
    *   Type of model
    * @tparam Msg
    *   Type of messages
    */
  def start[F[_]: Async, Model, Msg](
      init: (Model, Cmd[F, Msg]),
      update: Model => Msg => (Model, Cmd[F, Msg]),
      view: Model => NativeView[Msg],
      subscriptions: Model => Sub[F, Msg]
  ): F[Nothing] =
    println("Starting")
    val router: Location => Option[Msg] = _ => None

    TyrianRuntime[F, Model, Msg, NativeView, NativeViewState](
      router,
      init._1,
      init._2,
      update,
      view,
      subscriptions,
      NativeViewState.initialise()
    )

sealed trait NativeView[Msg]:
  def draw: String =
    this match
      case p: NativeView.Print[_] =>
        p.msg

object NativeView:

  final case class Print[Msg](msg: String) extends NativeView[Msg]

  given PresentView[NativeView, NativeViewState] with
    def draw[F[_], Model, Msg](
        dispatcher: Dispatcher[F],
        viewState: Ref[F, NativeViewState],
        model: Ref[F, Model],
        view: Model => NativeView[Msg],
        onMsg: Msg => Unit,
        router: Location => Option[Msg]
    )(using F: Async[F], clock: Clock[F]): F[Unit] =
      F.map(model.get) { m =>
        println(view(m).draw)
      }

final case class NativeViewState()
object NativeViewState:
  def initialise(): NativeViewState =
    NativeViewState()
