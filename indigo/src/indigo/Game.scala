package indigo

import indigo.BootResult
import indigo.MinimalLauncher
import indigo.bridge.BridgeData
import indigo.bridge.BridgeMsg
import indigo.bridge.TyrianIndigoNextBridge
import indigo.core.Outcome
import indigo.core.dice.Dice
import indigo.core.events.EventFilters
import indigo.core.events.GlobalEvent
import indigo.core.utils.IndigoLogger
import indigo.frameprocessors.GameFrameProcessor
import indigo.gameengine.GameEngine
import indigo.platform.assets.AssetCollection
import indigo.scenegraph.SceneUpdateFragment
import indigo.scenes.Scene
import indigo.scenes.SceneManager
import indigo.scenes.SceneName
import indigo.shaders.library
import indigo.shared.Context
import indigo.shared.Startup
import indigo.shared.subsystems.SubSystemsRegister
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.NonEmptyBatch
import org.scalajs.dom.Element
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.*
import tyrian.Action
import tyrian.Watcher

import scala.annotation.nowarn
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
trait Game[BootData, StartUpData, Model] extends MinimalLauncher[StartUpData, Model]:

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

    private[indigo] val _bridge: TyrianIndigoNextBridge[Model] =
      new TyrianIndigoNextBridge

    /** Send events from Tyrian to Indigo
      */
    def send(data: BridgeData): Action =
      Action(_bridge.send(BridgeMsg.Send(data)))

    /** Allows Tyrian to watch for messages from Indigo
      */
    def watch: Watcher =
      Watcher(_bridge.subscribe)

  end bridge

  private val subSystemsRegister: SubSystemsRegister[Model] =
    new SubSystemsRegister()

  private def indigoGame(bootUp: BootResult[BootData, Model]): GameEngine[StartUpData, Model] = {

    val bridgeSubSystem = bridge._bridge.subSystem

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

    val frameProcessor: GameFrameProcessor[StartUpData, Model] =
      new GameFrameProcessor(
        subSystemsRegister,
        sceneManager,
        eventFilters,
        updateModel,
        (ctx, m) => present(ctx, m)
      )

    new GameEngine[StartUpData, Model](
      bootUp.fonts,
      bootUp.animations,
      bootUp.shaders,
      (ac: AssetCollection) => (d: Dice) => setup(bootUp.bootData, ac, d),
      (sd: StartUpData) => initialModel(sd),
      frameProcessor,
      subSystemEvents
    )
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  protected def ready(flags: Map[String, String]): Element => GameEngine[StartUpData, Model] =
    parentElement =>
      boot(flags) match
        case oe @ Outcome.Error(e, _) =>
          IndigoLogger.error("Error during boot - Halting")
          IndigoLogger.error(oe.reportCrash)
          throw e

        case Outcome.Result(b, evts) =>
          indigoGame(b).start(parentElement, b.gameConfig, Future(None), b.assets, Future(Set()), evts)

end Game

object Game:

  trait ShaderToy extends Game[ShaderToy.Model, ShaderToy.Model, ShaderToy.Model]:

    given [A](using toUBO: ToUniformBlock[A]): Conversion[A, UniformBlock] with
      def apply(value: A): UniformBlock = toUBO.toUniformBlock(value)

    private val Channel0Name: String = "channel0"
    private val Channel1Name: String = "channel1"
    private val Channel2Name: String = "channel2"
    private val Channel3Name: String = "channel3"

    /** Your shader's default configuration settings, values like the viewport size can be overriden with flags.
      */
    def config: GameConfig

    /** A fixed set of assets that will be loaded before the game starts, typically for loading an external shader file.
      */
    def assets: Set[AssetType]

    /** An optional path to an image asset you would like to be mapped to channel 0 for your shader to use.
      */
    def channel0: Option[AssetPath]

    /** An optional path to an image asset you would like to be mapped to channel 1 for your shader to use.
      */
    def channel1: Option[AssetPath]

    /** An optional path to an image asset you would like to be mapped to channel 2 for your shader to use.
      */
    def channel2: Option[AssetPath]

    /** An optional path to an image asset you would like to be mapped to channel 3 for your shader to use.
      */
    def channel3: Option[AssetPath]

    /** The uniform blocks (data) you want to pass to your shader. Example:
      *
      * ```scala
      * import indigo.*
      * import indigo.syntax.shaders.*
      * import ultraviolet.syntax.*
      *
      * final case class CustomData(color: vec4, customTime: Float) extends FragmentEnvReference derives ToUniformBlock
      * def uniformBlocks: Batch[UniformBlock] = Batch(CustomData(RGBA.Magenta.toUVVec4, 0.seconds.toFloat))
      * ```
      *
      * As long as the field types in your case class are ultraviolet types, you can pass them to your shader, see
      * Ultraviolet docs for more info.
      *
      * Many standard Indigo types are supported for the data fields, but you will need a separate case class for the
      * Shader side of the data contract definition, i.e. This is valid too:
      *
      * ```scala
      * // For use with Indigo's shader setup. Note: derives ToUniformBlock, but doesn't need to extend FragmentEnvReference
      * final case class CustomDataIndigo(color: RGBA, customTime: Seconds) derives ToUniformBlock
      *
      * // For use with Ultraviolet's UBO definitions. Note extends FragmentEnvReference, but doesn't derive ToUniformBlock
      * final case class CustomDataUV(color: vec4, customTime: Float) extends FragmentEnvReference
      * ```
      */
    def uniformBlocks: Batch[UniformBlock]

    /** The shader you want to render
      */
    def shader: ShaderProgram

    def scenes(bootData: ShaderToy.Model): NonEmptyBatch[Scene[ShaderToy.Model, ShaderToy.Model]] =
      NonEmptyBatch(Scene.empty[ShaderToy.Model, ShaderToy.Model])

    def initialScene(bootData: ShaderToy.Model): Option[SceneName] =
      None

    def eventFilters: EventFilters =
      EventFilters.BlockAll

    final def boot(flags: Map[String, String]): Outcome[BootResult[ShaderToy.Model, ShaderToy.Model]] =
      val width  = flags.get("width").map(_.toInt).getOrElse(config.viewport.width)
      val height = flags.get("height").map(_.toInt).getOrElse(config.viewport.height)
      val c0     = flags.get(Channel0Name).map(p => AssetPath(p)).orElse(channel0)
      val c1     = flags.get(Channel1Name).map(p => AssetPath(p)).orElse(channel1)
      val c2     = flags.get(Channel2Name).map(p => AssetPath(p)).orElse(channel2)
      val c3     = flags.get(Channel3Name).map(p => AssetPath(p)).orElse(channel3)

      val channelAssets: Set[AssetType] =
        (c0.toSet.map(Channel0Name -> _) ++
          c1.toSet.map(Channel1Name -> _) ++
          c2.toSet.map(Channel2Name -> _) ++
          c3.toSet.map(Channel3Name -> _)).map { case (channel, path) =>
          AssetType.Image(AssetName(channel), path)
        }

      val configWithOverrides =
        config
          .withViewport(width, height)
          .modifyAdvancedSettings(
            _.withAutoLoadStandardShaders(false)
          )

      val bootData =
        ShaderToy.Model(
          Size(width, height),
          c0.map(_ => AssetName(Channel0Name)),
          c1.map(_ => AssetName(Channel1Name)),
          c2.map(_ => AssetName(Channel2Name)),
          c3.map(_ => AssetName(Channel3Name))
        )

      Outcome(
        BootResult(
          configWithOverrides,
          bootData
        )
          .withShaders(
            shader,
            SceneBlendShader.shader
          )
          .withAssets(assets ++ channelAssets)
      )

    final def setup(
        bootData: ShaderToy.Model,
        assetCollection: AssetCollection,
        dice: Dice
    ): Outcome[Startup[ShaderToy.Model]] =
      Outcome(
        Startup.Success(
          bootData
        )
      )

    final def initialModel(startupData: ShaderToy.Model): Outcome[ShaderToy.Model] =
      Outcome(startupData)

    final def updateModel(
        context: Context[ShaderToy.Model],
        model: ShaderToy.Model
    ): GlobalEvent => Outcome[ShaderToy.Model] = {
      case ViewportResize(vp) =>
        Outcome(model.copy(viewport = vp.size))

      case KeyboardEvent.KeyUp(Key.KEY_F) =>
        Outcome(model, Batch(ToggleFullScreen))

      case _ =>
        Outcome(model)
    }

    final def present(context: Context[ShaderToy.Model], model: ShaderToy.Model): Outcome[SceneUpdateFragment] =
      Outcome(
        SceneUpdateFragment(
          Layer(
            BlankEntity(
              model.viewport,
              ShaderData(
                shader.id,
                uniformBlocks,
                model.channel0,
                model.channel1,
                model.channel2,
                model.channel3
              )
            )
          ).withBlendMaterial(SceneBlendShader.material)
        )
      )

  object ShaderToy:

    final case class Model(
        viewport: Size,
        channel0: Option[AssetName],
        channel1: Option[AssetName],
        channel2: Option[AssetName],
        channel3: Option[AssetName]
    )

    object SceneBlendShader:

      val shader: UltravioletShader =
        UltravioletShader(
          ShaderId("[indigo_engine_shader_blend]"),
          BlendShader.vertex(library.NoOp.vertex, ()),
          BlendShader.fragment(
            fragment,
            Env.reference
          )
        )

      import ultraviolet.syntax.*

      trait Env extends BlendFragmentEnvReference
      object Env:
        val reference: Env = new Env {}

      @nowarn
      inline def fragment =
        Shader[Env] { env =>
          def fragment(color: vec4): vec4 =
            env.SRC
        }

      val material: BlendMaterial =
        new BlendMaterial:
          def toShaderData: ShaderData =
            ShaderData(shader.id)

end Game
