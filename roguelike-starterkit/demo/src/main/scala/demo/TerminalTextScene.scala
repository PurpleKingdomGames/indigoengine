package demo

import indigo.*
import indigo.scenes.*
import io.indigoengine.roguelike.starterkit.*

object TerminalTextScene extends Scene[Unit, Unit, Unit]:

  type SceneModel     = Unit
  type SceneViewModel = Unit

  val name: SceneName =
    SceneName("TerminalText scene")

  val modelLens: Lens[Unit, Unit] =
    Lens.keepLatest

  val viewModelLens: Lens[Unit, Unit] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem] =
    Set()

  def updateModel(context: SceneContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(model).addGlobalEvents(SceneEvent.JumpTo(TerminalEmulatorScene.name))

    case _ =>
      Outcome(model)

  def updateViewModel(
      context: SceneContext[Unit],
      model: Unit,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  val size = Size(30)

  def message: String =
    """
    |╔═════════════════════╗
    |║ Hit Space to Start! ║
    |╚═════════════════════╝
    |""".stripMargin

  def present(
      context: SceneContext[Unit],
      model: Unit,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Text(
          message,
          RoguelikeTiles.Size10x10.Fonts.fontKey,
          TerminalText(Assets.tileMap, RGB.Cyan, RGBA.Blue)
        ),
        Text(
          message,
          RoguelikeTiles.Size10x10.Fonts.fontKey,
          TerminalText(Assets.tileMap, RGB.Yellow, RGBA.Red).withShaderId(ShaderId("my shader"))
        ).moveBy(0, 40)
      )
    )

  def customShader(shaderId: ShaderId): UltravioletShader =
    UltravioletShader.entityFragment(
      shaderId,
      EntityShader.fragment[ShaderImpl.Env](
        ShaderImpl.frag,
        ShaderImpl.Env.ref
      )
    )

  object ShaderImpl:

    import ultraviolet.syntax.*

    final case class Env(
        FOREGROUND: vec3,
        BACKGROUND: vec4,
        MASK: vec4
    ) extends FragmentEnvReference

    object Env:
      val ref =
        Env(vec3(0.0f), vec4(0.0f), vec4(0.0f))

    final case class RogueLikeTextData(
        FOREGROUND: vec3,
        BACKGROUND: vec4,
        MASK: vec4
    )

    inline def frag: Shader[Env, Unit] =
      Shader[Env] { env =>
        ubo[RogueLikeTextData]

        def fragment(color: vec4): vec4 =
          val maskDiff: Boolean = abs(env.CHANNEL_0.x - env.MASK.x) < 0.001f &&
            abs(env.CHANNEL_0.y - env.MASK.y) < 0.001f &&
            abs(env.CHANNEL_0.z - env.MASK.z) < 0.001f &&
            abs(env.CHANNEL_0.w - env.MASK.w) < 0.001f

          val c: vec4 =
            if (maskDiff) {
              env.BACKGROUND
            } else {
              vec4(env.CHANNEL_0.rgb * (env.FOREGROUND.rgb * env.CHANNEL_0.a), env.CHANNEL_0.a)
            }

          c * (1.0f - (vec4(env.SCREEN_COORDS.x) / 250.0f)) // Example custom mod
      }
