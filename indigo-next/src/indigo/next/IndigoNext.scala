package indigo.next

import indigo.BootResult
import indigo.GameLauncher
import indigo.core.Outcome
import indigo.core.dice.Dice
import indigo.core.events.EventFilters
import indigo.core.events.GlobalEvent
import indigo.core.utils.IndigoLogger
import indigo.gameengine.GameEngine
import indigo.next.frameprocessors.NextFrameProcessor
import indigo.next.scenes.Scene
import indigo.next.scenes.SceneManager
import indigo.next.scenes.SceneName
import indigo.platform.assets.AssetCollection
import indigo.scenegraph.SceneUpdateFragment
import indigo.shared.Context
import indigo.shared.Startup
import indigo.shared.subsystems.SubSystemsRegister
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.NonEmptyBatch
import org.scalajs.dom.Element
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.*
import tyrian.next.Action
import tyrian.next.Watcher
import indigo.next.bridge.TyrianIndigoNextBridge

import scala.concurrent.Future

/** A trait representing a game with scene management baked in
  *
  * @example
  *   `object MyGame extends IndigoGame[BootData, StartUpData, Model, Unit]`
  *
  * @tparam BootData
  *   The class type representing you a successful game boot up
  * @tparam StartUpData
  *   The class type representing your successful startup data
  * @tparam Model
  *   The class type representing your game's model
  * @tparam Unit
  *   The class type representing your game's view model
  */
trait IndigoNext[BootData, StartUpData, Model] extends GameLauncher[StartUpData, Model, Unit] {

  /** A non-empty ordered list of scenes
    *
    * @param bootData
    *   Data created during initial game boot.
    * @return
    *   A list of scenes that ensures at least one scene exists.
    */
  def scenes(bootData: BootData): NonEmptyBatch[Scene[StartUpData, Model]]

  /** Optional name of the first scene. If None is provided then the first scene is the head of the scenes list.
    *
    * @param bootData
    *   Data created during initial game boot.
    * @return
    *   Optionally return the scene to start the game on, otherwise the first scene is used.
    */
  def initialScene(bootData: BootData): Option[SceneName]

  /** Event filters represent a mapping from events to possible events, and act like a firewall to prevent unnecessary
    * event processing by the model or view model.
    */
  def eventFilters: EventFilters

  /** `boot` provides the initial boot up function for your game, accepting commandline-like arguments and allowing you
    * to declare pre-request assets assets and data that must be in place for your game to get going.
    *
    * @param flags
    *   A simply key-value object/map passed in during initial boot.
    * @return
    *   Bootup data consisting of a custom data type, animations, subsystems, assets, fonts, and the game's config.
    */
  def boot(flags: Map[String, String]): Outcome[BootResult[BootData, Model]]

  /** The `setup` function is your only opportunity to do an initial work to set up your game. For example, perhaps one
    * of your assets was a JSON description of a map or an animation sequence, you could process that now, which is why
    * you have access to the `AssetCollection` object. `setup` is typically only called when new assets are loaded. In a
    * simple game this may only be once, but if assets are dynamically loaded, set up will be called again.
    *
    * @param bootData
    *   Data created during initial game boot.
    * @param assetCollection
    *   Access to the Asset collection in order to, for example, parse text files.
    * @param dice
    *   Pseudorandom number generator
    * @return
    *   Return start up data, which can include animations and fonts that could not be declared at boot time.
    */
  def setup(bootData: BootData, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[StartUpData]]

  /** Set up of your initial model state
    *
    * @param startupData
    *   Access to Startup data in case you need it for the model
    * @return
    *   An instance of your game model
    */
  def initialModel(startupData: StartUpData): Outcome[Model]

  /** A pure function for updating your game's model in the context of the running frame and the events acting upon it.
    *
    * @param context
    *   The context the frame should be produced in, including the time, input state, a dice instance, the state of the
    *   inputs, and a read only reference to your start up data.
    * @param model
    *   The latest version of the model to read from.
    * @return
    *   A function that maps GlobalEvent's to the next version of your model, and encapsulates failures or resulting
    *   events within the Outcome wrapper.
    */
  def updateModel(context: Context[StartUpData], model: Model): GlobalEvent => Outcome[Model]

  /** A pure function for presenting your game. The result is a side effect free declaration of what you intend to be
    * presented to the player next.
    *
    * @param context
    *   The context the frame should be produced in, including the time, input state, a dice instance, the state of the
    *   inputs, and a read only reference to your start up data.
    * @param model
    *   The latest version of the model to read from.
    * @return
    *   A function that produces a description of what to present next, and encapsulates failures or resulting events
    *   within the Outcome wrapper.
    */
  def present(context: Context[StartUpData], model: Model): Outcome[SceneUpdateFragment]

  object bridge:

    // def translateTo: GlobalEvent => Option[GlobalMsg]
    // def translateFrom: GlobalMsg => Option[GlobalEvent]

    private[next] val _bridge: TyrianIndigoNextBridge[Model] =
      new TyrianIndigoNextBridge

    /** Send events from Tyrian to Indigo
      */
    def send(event: GlobalEvent): Action =
      Action(_bridge.publish(event))
      
      // // println("Send was called with: " + msg)
      // val next = translateFrom(msg)
      // // println("Translated to: " + next)
      // // println("bridge: " + bridge)
      // Action(bridge.publish(next))

    /** Allows Tyrian to watch for messages from Indigo
      */
    def watch: Watcher =
      Watcher(_bridge.subscribe)

  end bridge

  private val subSystemsRegister: SubSystemsRegister[Model] =
    new SubSystemsRegister()

  private def indigoGame(bootUp: BootResult[BootData, Model]): GameEngine[StartUpData, Model, Unit] = {

    val bridgeSubSystem = bridge._bridge.subSystem(???)

    val subSystemEvents = subSystemsRegister.register(Batch.fromSet(bootUp.subSystems ++ Set(bridgeSubSystem)))

    val sceneManager: SceneManager[StartUpData, Model] = {
      val s = scenes(bootUp.bootData)

      initialScene(bootUp.bootData) match {
        case Some(name) =>
          SceneManager(s, name)

        case None =>
          SceneManager(s, s.head.name)
      }
    }

    val frameProcessor: NextFrameProcessor[StartUpData, Model] =
      new NextFrameProcessor(
        subSystemsRegister,
        sceneManager,
        eventFilters,
        updateModel,
        (ctx, m) => present(ctx, m)
      )

    new GameEngine[StartUpData, Model, Unit](
      bootUp.fonts,
      bootUp.animations,
      bootUp.shaders,
      (ac: AssetCollection) => (d: Dice) => setup(bootUp.bootData, ac, d),
      (sd: StartUpData) => initialModel(sd),
      (_: StartUpData) => (_: Model) => Outcome(()),
      frameProcessor,
      subSystemEvents
    )
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  protected def ready(flags: Map[String, String]): Element => GameEngine[StartUpData, Model, Unit] =
    parentElement =>
      boot(flags) match
        case oe @ Outcome.Error(e, _) =>
          IndigoLogger.error("Error during boot - Halting")
          IndigoLogger.error(oe.reportCrash)
          throw e

        case Outcome.Result(b, evts) =>
          indigoGame(b).start(parentElement, b.gameConfig, Future(None), b.assets, Future(Set()), evts)

}
