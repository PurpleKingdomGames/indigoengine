package tyrian.extensions

import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.Seconds
import tyrian.GlobalMsg
import tyrian.Result
import tyrian.Watcher

/** Extensions are mini-tyrian apps that are mechanically and automatically composed into their parent application.
  *
  * They are the Tyrian version of Indigo's SubSystems.
  */
sealed trait Extension[GraphicsContext, View]:

  /** Type representing this extension's model
    */
  type ExtensionModel

  /** All Tyrian extensions must have an identifier, and it must be unique.
    */
  def id: ExtensionId

  /** Tells Tyrian how to produce the initial version of the extensions model. Since the result is wrapped in a Result
    * type, Actions (Cmds) and messages can emitted here.
    */
  def init: Result[ExtensionModel]

  /** Updates the extensions model based on incoming msgs and optionally produces side effects.
    */
  def update(model: ExtensionModel): GlobalMsg => Result[ExtensionModel]

  /** Produces declarative descriptions of views to be presented.
    */
  def view(model: ExtensionModel): View

  /** Allows the extension to watch for changes in external resources, and produce messages for the app to consume and
    * act on.
    */
  def watchers(model: ExtensionModel): Batch[Watcher]

  /** Invoked once when terminal apps start up, guaranteed to run and complete before the first draw (has no effect on
    * JS). Provides an opportunity for synchronous setup side-effects, such as putting the terminal into raw mode, so
    * that the very first frame is rendered into a correctly prepared environment.
    *
    * `prepare` is the mirror of [[teardown]]: extensions are prepared before the main app's first draw and torn down
    * before the main app exits. Like `teardown`, it has access to the extension's model so that references to resources
    * acquired here can be retained for later clean up.
    */
  def prepare(model: ExtensionModel): Unit

  /** Invoked when terminal apps exit (has no effect on JS). Provides an opportunity for sign-off messages to the user,
    * or for clean up side-effects to take place.
    *
    * Unlike the main app, extensions have the ability to have stateful teardowns, since they have access to their
    * extension's model, that can hold references to resources that require clean up. Extensions are cleaned up before
    * the main app, and any non-fatal exceptions are caught and reported so that one failing extension doesn't prevent
    * subsequent extensions from terminating cleanly.
    *
    * Note: `teardown` may not be invoked if you run the native version through your build tool, but will be invoked if
    * you run the executable directly.
    */
  def teardown(model: ExtensionModel): Unit

object Extension:

  trait Standard[View] extends Extension[Unit, View]
  trait Graphical[GraphicsContext, View] extends Extension[GraphicsContext, View]:

    /** Used for per-frame drawing directly to a graphics context.
      */
    def draw(context: GraphicsContext, runningTime: Seconds, model: ExtensionModel): ExtensionModel

    def provideContext(model: ExtensionModel): Option[GraphicsContext]
