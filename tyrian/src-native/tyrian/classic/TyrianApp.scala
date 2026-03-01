package tyrian.classic

import cats.effect.kernel.Async
import tyrian.Location
import tyrian.platform.Cmd
import tyrian.platform.Sub
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
  def init(args: Array[String]): (Model, Cmd[F, Msg])

  /** The update method allows you to modify the model based on incoming messages (events). As well as an updated model,
    * you can also produce commands to run.
    */
  def update(model: Model): Msg => (Model, Cmd[F, Msg])

  /** Used to render your current model into an HTML view.
    */
  def view(model: Model): Terminal[Msg]

  /** Subscriptions are typically processes that run for a period of time and emit discrete events based on some world
    * event, e.g. a mouse moving might emit it's coordinates.
    */
  def subscriptions(model: Model): Sub[F, Msg]

  /** Launch the app with the supplied simple args.
    */
  def launch(args: Array[String]): Unit =
    ready(args)

  private def _init(args: Array[String]): (Model, Cmd[F, Msg]) =
    val (m, cmd) = init(args)
    (m, cmd)

  private def _update(model: Model): Msg => (Model, Cmd[F, Msg]) =
    msg => update(model)(msg)

  private def _view(model: Model): Terminal[Msg] =
    view(model)

  private def _subscriptions(model: Model): Sub[F, Msg] =
    subscriptions(model)

  private def ready(args: Array[String]): Unit =
    run(
      TyrianApp.start[F, Model, Msg](
        _init(args),
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
      view: Model => Terminal[Msg],
      subscriptions: Model => Sub[F, Msg]
  ): F[Nothing] =
    val router: Location => Option[Msg] = _ => None

    TyrianRuntime[F, Model, Msg, Terminal, Unit](
      router,
      init._1,
      init._2,
      update,
      view,
      subscriptions,
      ()
    )
