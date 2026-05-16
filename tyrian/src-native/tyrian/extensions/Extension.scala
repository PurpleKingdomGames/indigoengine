package tyrian.extensions

import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.Seconds
import tyrian.GlobalMsg
import tyrian.Result
import tyrian.TerminalFragment
import tyrian.Watcher

// TODO: Can this be merged with the JS version?

/** Extensions are mini-tyrian app's that are mechanically and automatically composed into their parent application.
  *
  * They are the Tyrian version of Indigo's SubSystems.
  */
sealed trait Extension:

  /** Type representing this extension's model
    */
  type ExtensionModel

  /** All Tyrian extensions must have an identifier, and it must be unique.
    */
  def id: ExtensionId

  /** Tells Tyrian how to produce the initial version of the extensions model. Since the result is wrapped in a Result
    * type, Actions (Cmds) and messages can emited here.
    */
  def init: Result[ExtensionModel]

  /** Updates the extensions model based on incoming msg's and optionally produces side effects.
    */
  def update(model: ExtensionModel): GlobalMsg => Result[ExtensionModel]

  /** Produces declaritive descriptions of views to be presented.
    */
  def view(model: ExtensionModel): TerminalFragment

  /** Allows the extension to watch for changes in external resources, and produce messages for the app to consume and
    * act on.
    */
  def watchers(model: ExtensionModel): Batch[Watcher]

object Extension:

  trait Standard extends Extension
  trait Graphical[GraphicsContext] extends Extension:

    /** Used for per-frame drawing directly to a graphics context.
      */
    def draw(context: GraphicsContext, runningTime: Seconds, timeDelta: Seconds, model: ExtensionModel): Unit
