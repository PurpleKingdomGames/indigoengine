package indigo

import indigo.shaders.UniformDataHelpers

import scala.annotation.targetName

object syntax:

  extension (d: Double)
    def toRadians: Radians = Radians(d)
    def radians: Radians   = Radians(d)
    def toSeconds: Seconds = Seconds(d)
    def second: Seconds    = Seconds(d)
    def seconds: Seconds   = Seconds(d)
    def toVolume: Volume   = Volume(d)
    def volume: Volume     = Volume(d)
    def toZoom: Zoom       = Zoom(d)
    def zoom: Zoom         = Zoom(d)

  extension (i: Int)
    def toFPS: FPS       = FPS(i)
    def fps: FPS         = FPS(i)
    def toMillis: Millis = Millis(i)
    def millis: Millis   = Millis(i)

  extension (l: Long)
    def toMillis: Millis = Millis(l)
    def millis: Millis   = Millis(l)

  extension (s: String)
    def toAnimationKey: AnimationKey = AnimationKey(s)
    def toAssetName: AssetName       = AssetName(s)
    def toAssetPath: AssetPath       = AssetPath(s)
    def toAssetTag: AssetTag         = AssetTag(s)
    def toBindingKey: BindingKey     = BindingKey(s)
    def toLayerKey: LayerKey         = LayerKey(s)
    def toCloneId: CloneId           = CloneId(s)
    def toCycleLabel: CycleLabel     = CycleLabel(s)
    def toFontKey: FontKey           = FontKey(s)
    def toScene: scenes.SceneName    = scenes.SceneName(s)
    def toShaderId: ShaderId         = ShaderId(s)

  extension (t: (Double, Double)) def toVector2: Vector2 = Vector2(t._1, t._2)

  extension (t: (Double, Double, Double))
    def toRGB: RGB         = RGB(t._1, t._2, t._3)
    def toVector3: Vector3 = Vector3(t._1, t._2, t._3)

  extension (t: (Double, Double, Double, Double))
    def toRGBA: RGBA       = RGBA(t._1, t._2, t._3, t._4)
    def toVector4: Vector4 = Vector4(t._1, t._2, t._3, t._4)

  extension (t: (Int, Int))
    def toPoint: Point = Point(t._1, t._2)
    def toSize: Size   = Size(t._1, t._2)

  extension [A](values: scalajs.js.Array[A]) def toBatch: Batch[A] = Batch.fromJSArray(values)
  extension [A](values: Array[A]) def toBatch: Batch[A]            = Batch.fromArray(values)
  extension [A](values: List[A]) def toBatch: Batch[A]             = Batch.fromList(values)
  extension [A](values: Set[A]) def toBatch: Batch[A]              = Batch.fromSet(values)
  extension [A](values: Seq[A]) def toBatch: Batch[A]              = Batch.fromSeq(values)
  extension [A](values: IndexedSeq[A]) def toBatch: Batch[A]       = Batch.fromIndexedSeq(values)
  extension [A](values: Iterator[A]) def toBatch: Batch[A]         = Batch.fromIterator(values)
  extension [K, V](values: Map[K, V]) def toBatch: Batch[(K, V)]   = Batch.fromMap(values)
  extension (values: Range) def toBatch: Batch[Int]                = Batch.fromRange(values)

  extension [A](values: Option[A])
    def toBatch: Batch[A]                          = Batch.fromOption(values)
    def toOutcome(error: => Throwable): Outcome[A] = Outcome.fromOption(values, error)

  val ==: = indigoengine.shared.collections.Batch.==:
  val :== = indigoengine.shared.collections.Batch.:==

  extension [A](b: Batch[Outcome[A]]) def sequence: Outcome[Batch[A]]                 = Outcome.sequenceBatch(b)
  extension [A](b: NonEmptyBatch[Outcome[A]]) def sequence: Outcome[NonEmptyBatch[A]] = Outcome.sequenceNonEmptyBatch(b)
  extension [A](l: List[Outcome[A]]) def sequence: Outcome[List[A]]                   = Outcome.sequenceList(l)
  extension [A](l: NonEmptyList[Outcome[A]]) def sequence: Outcome[NonEmptyList[A]]   = Outcome.sequenceNonEmptyList(l)

  extension [A](b: Batch[Option[A]]) def sequence: Option[Batch[A]]                 = Batch.sequenceOption(b)
  extension [A](b: NonEmptyBatch[Option[A]]) def sequence: Option[NonEmptyBatch[A]] = NonEmptyBatch.sequenceOption(b)
  extension [A](l: List[Option[A]]) def sequence: Option[List[A]]                   = NonEmptyList.sequenceListOption(l)
  extension [A](l: NonEmptyList[Option[A]]) def sequence: Option[NonEmptyList[A]]   = NonEmptyList.sequenceOption(l)

  extension (s: Size) def toGameViewport: GameViewport = GameViewport(s)

  // GRADIENT_FROM_TO (vec4), GRADIENT_FROM_COLOR (vec4), GRADIENT_TO_COLOR (vec4),
  extension (fill: Fill)
    def toUniformData(prefix: String): Batch[(Uniform, ShaderPrimitive)] =
      UniformDataHelpers.fillToUniformData(fill, prefix)

  // Timeline animations
  object animations:
    import indigo.core.animation.timeline.*
    import indigo.core.temporal.SignalFunction
    import scala.annotation.targetName

    def timeline[A](animations: TimelineAnimation[A]*): Timeline[A] =
      Timeline(Batch.fromSeq(animations).flatMap(_.compile.toWindows))

    def layer[A](timeslots: TimeSlot[A]*): TimelineAnimation[A] =
      TimelineAnimation(Batch.fromSeq(timeslots))

    @targetName("SF_ctxfn_lerp")
    def lerp: Seconds ?=> SignalFunction[Seconds, Double] = over ?=> SignalFunction.lerp(over)

    @targetName("SF_ctxfn_easeIn")
    def easeIn: Seconds ?=> SignalFunction[Seconds, Double] = over ?=> SignalFunction.easeIn(over)

    @targetName("SF_ctxfn_easeOut")
    def easeOut: Seconds ?=> SignalFunction[Seconds, Double] = over ?=> SignalFunction.easeOut(over)

    @targetName("SF_ctxfn_easeInOut")
    def easeInOut: Seconds ?=> SignalFunction[Seconds, Double] = over ?=> SignalFunction.easeInOut(over)

    export TimeSlot.start
    export TimeSlot.startAfter
    export TimeSlot.pause
    export TimeSlot.show
    export TimeSlot.animate

    export SignalFunction.lerp
    export SignalFunction.easeIn
    export SignalFunction.easeOut
    export SignalFunction.easeInOut
    export SignalFunction.wrap
    export SignalFunction.clamp
    export SignalFunction.step
    export SignalFunction.sin
    export SignalFunction.cos
    export SignalFunction.orbit
    export SignalFunction.pulse
    export SignalFunction.smoothPulse
    export SignalFunction.multiply
  end animations

  object shaders:

    extension (c: RGBA)
      def toUVVec4: ultraviolet.syntax.vec4 =
        ultraviolet.syntax.vec4(c.r.toFloat, c.g.toFloat, c.b.toFloat, c.a.toFloat)
    extension (c: RGB)
      def toUVVec3: ultraviolet.syntax.vec3 =
        ultraviolet.syntax.vec3(c.r.toFloat, c.g.toFloat, c.b.toFloat)
    extension (p: Point)
      def toUVVec2: ultraviolet.syntax.vec2 =
        ultraviolet.syntax.vec2(p.x.toFloat, p.y.toFloat)
    extension (s: Size)
      def toUVVec2: ultraviolet.syntax.vec2 =
        ultraviolet.syntax.vec2(s.width.toFloat, s.height.toFloat)
    extension (v: Vector2)
      def toUVVec2: ultraviolet.syntax.vec2 =
        ultraviolet.syntax.vec2(v.x.toFloat, v.y.toFloat)
    extension (v: Vector3)
      def toUVVec3: ultraviolet.syntax.vec3 =
        ultraviolet.syntax.vec3(v.x.toFloat, v.y.toFloat, v.z.toFloat)
    extension (v: Vector4)
      def toUVVec4: ultraviolet.syntax.vec4 =
        ultraviolet.syntax.vec4(v.x.toFloat, v.y.toFloat, v.z.toFloat, v.w.toFloat)
    extension (r: Rectangle)
      def toUVVec4: ultraviolet.syntax.vec4 =
        ultraviolet.syntax.vec4(r.x.toFloat, r.y.toFloat, r.width.toFloat, r.height.toFloat)
    extension (m: Matrix4)
      def toUVMat4: ultraviolet.syntax.mat4 =
        ultraviolet.syntax.mat4(m.toArray.map(_.toFloat))
    extension (m: Millis) def toUVFloat: Float  = m.toFloat
    extension (r: Radians) def toUVFloat: Float = r.toFloat
    extension (s: Seconds)
      @targetName("ext_Seconds_toUVFloat")
      def toUVFloat: Float = s.toFloat
    extension (d: Double)
      @targetName("ext_Double_toUVFloat")
      def toUVFloat: Float = d.toFloat
    extension (i: Int)
      @targetName("ext_Int_toUVFloat")
      def toUVFloat: Float = i.toFloat
    extension (l: Long)
      @targetName("ext_Long_toUVFloat")
      def toUVFloat: Float = l.toFloat
    extension (a: Array[Float])
      def toUVArray: ultraviolet.syntax.array[Singleton & Int, Float] =
        ultraviolet.syntax.array(a)
    extension (a: scalajs.js.Array[Float])
      def toUVArray: ultraviolet.syntax.array[Singleton & Int, Float] =
        ultraviolet.syntax.array(a.toArray)

  end shaders

end syntax

object mutable:

  type CacheKey = indigo.core.utils.CacheKey
  val CacheKey: indigo.core.utils.CacheKey.type = indigo.core.utils.CacheKey

  type ToCacheKey[A] = indigo.core.utils.ToCacheKey[A]
  val ToCacheKey: indigo.core.utils.ToCacheKey.type = indigo.core.utils.ToCacheKey

  type QuickCache[A] = indigo.core.utils.QuickCache[A]
  val QuickCache: indigo.core.utils.QuickCache.type = indigo.core.utils.QuickCache

end mutable

val logger: indigo.core.utils.IndigoLogger.type = indigo.core.utils.IndigoLogger

type Startup[SuccessType] = shared.Startup[SuccessType]
val Startup: shared.Startup.type = shared.Startup

type GameTime = indigo.core.time.GameTime
val GameTime: indigo.core.time.GameTime.type = indigo.core.time.GameTime

type Millis = indigoengine.shared.datatypes.Millis
val Millis: indigoengine.shared.datatypes.Millis.type = indigoengine.shared.datatypes.Millis

type Seconds = indigoengine.shared.datatypes.Seconds
val Seconds: indigoengine.shared.datatypes.Seconds.type = indigoengine.shared.datatypes.Seconds

type FPS = indigo.core.time.FPS
val FPS: indigo.core.time.FPS.type = indigo.core.time.FPS

type Dice = indigo.core.dice.Dice
val Dice: indigo.core.dice.Dice.type = indigo.core.dice.Dice

type AssetCollection = platform.assets.AssetCollection

type AssetName = indigo.core.assets.AssetName
val AssetName: indigo.core.assets.AssetName.type = indigo.core.assets.AssetName

type AssetPath = indigo.core.assets.AssetPath
val AssetPath: indigo.core.assets.AssetPath.type = indigo.core.assets.AssetPath

type AssetTag = indigo.core.assets.AssetTag
val AssetTag: indigo.core.assets.AssetTag.type = indigo.core.assets.AssetTag

type Material = indigo.scenegraph.materials.Material
val Material: indigo.scenegraph.materials.Material.type = indigo.scenegraph.materials.Material

type FillType = indigo.scenegraph.materials.FillType
val FillType: indigo.scenegraph.materials.FillType.type = indigo.scenegraph.materials.FillType

type LightingModel = indigo.scenegraph.materials.LightingModel
val LightingModel: indigo.scenegraph.materials.LightingModel.type = indigo.scenegraph.materials.LightingModel

type Texture = indigo.scenegraph.materials.Texture
val Texture: indigo.scenegraph.materials.Texture.type = indigo.scenegraph.materials.Texture

type BlendMaterial = indigo.scenegraph.materials.BlendMaterial
val BlendMaterial: indigo.scenegraph.materials.BlendMaterial.type = indigo.scenegraph.materials.BlendMaterial

type ShaderData = indigo.shaders.ShaderData
val ShaderData: indigo.shaders.ShaderData.type = indigo.shaders.ShaderData

type ShaderProgram = indigo.shaders.ShaderProgram

type BlendShader = indigo.shaders.BlendShader
val BlendShader: indigo.shaders.BlendShader.type = indigo.shaders.BlendShader

type EntityShader = indigo.shaders.EntityShader
val EntityShader: indigo.shaders.EntityShader.type = indigo.shaders.EntityShader

type UltravioletShader = indigo.shaders.UltravioletShader
val UltravioletShader: indigo.shaders.UltravioletShader.type = indigo.shaders.UltravioletShader

type VertexEnv = indigo.shaders.library.IndigoUV.VertexEnv
val VertexEnv: indigo.shaders.library.IndigoUV.VertexEnv.type =
  indigo.shaders.library.IndigoUV.VertexEnv

type VertexEnvReference = indigo.shaders.library.IndigoUV.VertexEnvReference

type FragmentEnv = indigo.shaders.library.IndigoUV.FragmentEnv
val FragmentEnv: indigo.shaders.library.IndigoUV.FragmentEnv.type =
  indigo.shaders.library.IndigoUV.FragmentEnv

type FragmentEnvReference = indigo.shaders.library.IndigoUV.FragmentEnvReference

type BlendFragmentEnv = indigo.shaders.library.IndigoUV.BlendFragmentEnv
val BlendFragmentEnv: indigo.shaders.library.IndigoUV.BlendFragmentEnv.type =
  indigo.shaders.library.IndigoUV.BlendFragmentEnv

type BlendFragmentEnvReference = indigo.shaders.library.IndigoUV.BlendFragmentEnvReference

type ShaderId = indigo.shaders.ShaderId
val ShaderId: indigo.shaders.ShaderId.type = indigo.shaders.ShaderId

type Uniform = indigo.shaders.Uniform
val Uniform: indigo.shaders.Uniform.type = indigo.shaders.Uniform

type ToUniformBlock[A] = indigo.shaders.ToUniformBlock[A]
val ToUniformBlock: indigo.shaders.ToUniformBlock.type = indigo.shaders.ToUniformBlock

type UniformBlock = indigo.shaders.UniformBlock
val UniformBlock: indigo.shaders.UniformBlock.type = indigo.shaders.UniformBlock

val StandardShaders: indigo.shaders.StandardShaders.type = indigo.shaders.StandardShaders

type ShaderPrimitive = indigo.shaders.ShaderPrimitive
val ShaderPrimitive: indigo.shaders.ShaderPrimitive.type = indigo.shaders.ShaderPrimitive

type Outcome[T] = indigo.core.Outcome[T]
val Outcome: indigo.core.Outcome.type = indigo.core.Outcome

type Key = indigo.core.constants.Key
val Key: indigo.core.constants.Key.type = indigo.core.constants.Key

type KeyCode = indigo.core.constants.KeyCode
val KeyCode: indigo.core.constants.KeyCode.type = indigo.core.constants.KeyCode

type KeyLocation = indigo.core.constants.KeyLocation
val KeyLocation: indigo.core.constants.KeyLocation.type = indigo.core.constants.KeyLocation

type Batch[A] = indigoengine.shared.collections.Batch[A]
val Batch: indigoengine.shared.collections.Batch.type = indigoengine.shared.collections.Batch

type NonEmptyBatch[A] = indigoengine.shared.collections.NonEmptyBatch[A]
val NonEmptyBatch: indigoengine.shared.collections.NonEmptyBatch.type = indigoengine.shared.collections.NonEmptyBatch

type NonEmptyList[A] = indigoengine.shared.collections.NonEmptyList[A]
val NonEmptyList: indigoengine.shared.collections.NonEmptyList.type = indigoengine.shared.collections.NonEmptyList

type Signal[A] = indigo.core.temporal.Signal[A]
val Signal: indigo.core.temporal.Signal.type = indigo.core.temporal.Signal

type SignalReader[R, A] = indigo.core.temporal.SignalReader[R, A]
val SignalReader: indigo.core.temporal.SignalReader.type = indigo.core.temporal.SignalReader

type SignalState[S, A] = indigo.core.temporal.SignalState[S, A]
val SignalState: indigo.core.temporal.SignalState.type = indigo.core.temporal.SignalState

type SignalFunction[A, B] = indigo.core.temporal.SignalFunction[A, B]
val SignalFunction: indigo.core.temporal.SignalFunction.type = indigo.core.temporal.SignalFunction

type SubSystem[Model] = shared.subsystems.SubSystem[Model]

type SubSystemId = shared.subsystems.SubSystemId
val SubSystemId: shared.subsystems.SubSystemId.type = shared.subsystems.SubSystemId

/** defaultGameConfig Provides a useful default config set up:
  *   - Game Viewport = 550 x 400
  *   - FPS = 30
  *   - Clear color = Black
  *   - Magnification = 1
  *   - No advanced settings enabled
  * @return
  *   A GameConfig instance
  */
val defaultGameConfig: indigo.core.config.GameConfig =
  indigo.core.config.GameConfig.default

/** noRender Convenience value, alias for SceneUpdateFragment.empty
  * @return
  *   An Empty SceneUpdateFragment
  */
val noRender: indigo.scenegraph.SceneUpdateFragment =
  indigo.scenegraph.SceneUpdateFragment.empty

// events

type GlobalEvent    = indigo.core.events.GlobalEvent
type SubSystemEvent = indigo.core.events.SubSystemEvent
type ViewEvent      = indigo.core.events.ViewEvent
type InputEvent     = indigo.core.events.InputEvent

type EventFilters = indigo.core.events.EventFilters
val EventFilters: indigo.core.events.EventFilters.type = indigo.core.events.EventFilters

type AccessControl = indigo.core.events.AccessControl
val AccessControl: indigo.core.events.AccessControl.type = indigo.core.events.AccessControl

type RendererDetails = indigo.core.events.RendererDetails
val RendererDetails: indigo.core.events.RendererDetails.type = indigo.core.events.RendererDetails

type ViewportResize = indigo.core.events.ViewportResize
val ViewportResize: indigo.core.events.ViewportResize.type = indigo.core.events.ViewportResize

val ToggleFullScreen: indigo.core.events.ToggleFullScreen.type             = indigo.core.events.ToggleFullScreen
val EnterFullScreen: indigo.core.events.EnterFullScreen.type               = indigo.core.events.EnterFullScreen
val ExitFullScreen: indigo.core.events.ExitFullScreen.type                 = indigo.core.events.ExitFullScreen
val FullScreenEntered: indigo.core.events.FullScreenEntered.type           = indigo.core.events.FullScreenEntered
val FullScreenEnterError: indigo.core.events.FullScreenEnterError.type     = indigo.core.events.FullScreenEnterError
val FullScreenExited: indigo.core.events.FullScreenExited.type             = indigo.core.events.FullScreenExited
val FullScreenExitError: indigo.core.events.FullScreenExitError.type       = indigo.core.events.FullScreenExitError
val ApplicationGainedFocus: indigo.core.events.ApplicationGainedFocus.type = indigo.core.events.ApplicationGainedFocus
val CanvasGainedFocus: indigo.core.events.CanvasGainedFocus.type           = indigo.core.events.CanvasGainedFocus
val ApplicationLostFocus: indigo.core.events.ApplicationLostFocus.type     = indigo.core.events.ApplicationLostFocus
val CanvasLostFocus: indigo.core.events.CanvasLostFocus.type               = indigo.core.events.CanvasLostFocus

type InputState = indigo.core.events.InputState
val InputState: indigo.core.events.InputState.type = indigo.core.events.InputState

type InputMapping[A] = indigo.core.events.InputMapping[A]
val InputMapping: indigo.core.events.InputMapping.type = indigo.core.events.InputMapping

type Combo = indigo.core.events.Combo
val Combo: indigo.core.events.Combo.type = indigo.core.events.Combo

type GamepadInput = indigo.core.events.GamepadInput
val GamepadInput: indigo.core.events.GamepadInput.type = indigo.core.events.GamepadInput

type WheelEvent = indigo.core.events.WheelEvent
val WheelEvent: indigo.core.events.WheelEvent.type = indigo.core.events.WheelEvent

type MouseState = indigo.core.input.MouseState
val MouseState: indigo.core.input.MouseState.type = indigo.core.input.MouseState

type Mouse = indigo.core.input.Mouse
val Mouse: indigo.core.input.Mouse.type = indigo.core.input.Mouse

type MouseInput = indigo.core.events.MouseInput
val MouseInput: indigo.core.events.MouseInput.type = indigo.core.events.MouseInput

type MouseEvent = indigo.core.events.MouseEvent
val MouseEvent: indigo.core.events.MouseEvent.type = indigo.core.events.MouseEvent

type MouseButton = indigo.core.events.MouseButton
val MouseButton: indigo.core.events.MouseButton.type = indigo.core.events.MouseButton

@deprecated("Use `ScrollDirection` instead", "0.22.0")
type MouseWheel = indigo.core.events.MouseWheel
@deprecated("Use `ScrollDirection` instead", "0.22.0")
val MouseWheel: indigo.core.events.MouseWheel.type = indigo.core.events.MouseWheel

type TouchState = indigo.core.input.TouchState
val TouchState: indigo.core.input.TouchState.type = indigo.core.input.TouchState

type Finger = indigo.core.input.Finger
val Finger: indigo.core.input.Finger.type = indigo.core.input.Finger

type TouchEvent = indigo.core.events.TouchEvent
val TouchEvent: indigo.core.events.TouchEvent.type = indigo.core.events.TouchEvent

type PenState = indigo.core.input.PenState
val PenState: indigo.core.input.PenState.type = indigo.core.input.PenState

type Pen = indigo.core.input.Pen
val Pen: indigo.core.input.Pen.type = indigo.core.input.Pen

type PenEvent = indigo.core.events.PenEvent
val PenEvent: indigo.core.events.PenEvent.type = indigo.core.events.PenEvent

type Wheel = indigo.core.input.Wheel
val Wheel: indigo.core.input.Wheel.type = indigo.core.input.Wheel

type ScrollDirection = indigo.core.events.WheelDirection
val ScrollDirection: indigo.core.events.WheelDirection.type = indigo.core.events.WheelDirection

type PointerState = indigo.core.input.PointerState
val PointerState: indigo.core.input.PointerState.type = indigo.core.input.PointerState

type Pointer = indigo.core.input.Pointer
val Pointer: indigo.core.input.Pointer.type = indigo.core.input.Pointer

type PointerEvent = indigo.core.events.PointerEvent
val PointerEvent: indigo.core.events.PointerEvent.type = indigo.core.events.PointerEvent

type Keyboard = indigo.core.input.Keyboard
val Keyboard: indigo.core.input.Keyboard.type = indigo.core.input.Keyboard

type KeyboardEvent = indigo.core.events.KeyboardEvent
val KeyboardEvent: indigo.core.events.KeyboardEvent.type = indigo.core.events.KeyboardEvent

type FrameTick = indigo.core.events.FrameTick.type
val FrameTick: indigo.core.events.FrameTick.type = indigo.core.events.FrameTick

type PlaySound = indigo.core.events.PlaySound
val PlaySound: indigo.core.events.PlaySound.type = indigo.core.events.PlaySound

type NetworkEvent = indigo.core.events.NetworkEvent
val NetworkEvent: indigo.core.events.NetworkEvent.type = indigo.core.events.NetworkEvent

type NetworkSendEvent    = indigo.core.events.NetworkSendEvent
type NetworkReceiveEvent = indigo.core.events.NetworkReceiveEvent

type StorageActionType = indigo.core.events.StorageActionType
val StorageActionType: indigo.core.events.StorageActionType.type = indigo.core.events.StorageActionType

type StorageKey = indigo.core.events.StorageKey
val StorageKey: indigo.core.events.StorageKey.type = indigo.core.events.StorageKey

type StorageEvent = indigo.core.events.StorageEvent
val StorageEvent: indigo.core.events.StorageEvent.type = indigo.core.events.StorageEvent

type StorageEventError = indigo.core.events.StorageEventError
val StorageEventError: indigo.core.events.StorageEventError.type = indigo.core.events.StorageEventError

type FetchKeyAt = indigo.core.events.StorageEvent.FetchKeyAt
val FetchKeyAt: indigo.core.events.StorageEvent.FetchKeyAt.type = indigo.core.events.StorageEvent.FetchKeyAt

type KeyFoundAt = indigo.core.events.StorageEvent.KeyFoundAt
val KeyFoundAt: indigo.core.events.StorageEvent.KeyFoundAt.type = indigo.core.events.StorageEvent.KeyFoundAt

type FetchKeys = indigo.core.events.StorageEvent.FetchKeys
val FetchKeys: indigo.core.events.StorageEvent.FetchKeys.type = indigo.core.events.StorageEvent.FetchKeys

type KeysFound = indigo.core.events.StorageEvent.KeysFound
val KeysFound: indigo.core.events.StorageEvent.KeysFound.type = indigo.core.events.StorageEvent.KeysFound

type Save = indigo.core.events.StorageEvent.Save
val Save: indigo.core.events.StorageEvent.Save.type = indigo.core.events.StorageEvent.Save

type Load = indigo.core.events.StorageEvent.Load
val Load: indigo.core.events.StorageEvent.Load.type = indigo.core.events.StorageEvent.Load

type Delete = indigo.core.events.StorageEvent.Delete
val Delete: indigo.core.events.StorageEvent.Delete.type = indigo.core.events.StorageEvent.Delete

val DeleteAll: indigo.core.events.StorageEvent.DeleteAll.type = indigo.core.events.StorageEvent.DeleteAll

type Loaded = indigo.core.events.StorageEvent.Loaded
val Loaded: indigo.core.events.StorageEvent.Loaded.type = indigo.core.events.StorageEvent.Loaded

type AssetEvent = indigo.core.events.AssetEvent
val AssetEvent: indigo.core.events.AssetEvent.type = indigo.core.events.AssetEvent

type LoadAsset = indigo.core.events.AssetEvent.LoadAsset
val LoadAsset: indigo.core.events.AssetEvent.LoadAsset.type = indigo.core.events.AssetEvent.LoadAsset

type LoadAssetBatch = indigo.core.events.AssetEvent.LoadAssetBatch
val LoadAssetBatch: indigo.core.events.AssetEvent.LoadAssetBatch.type = indigo.core.events.AssetEvent.LoadAssetBatch

type AssetBatchLoaded = indigo.core.events.AssetEvent.AssetBatchLoaded
val AssetBatchLoaded: indigo.core.events.AssetEvent.AssetBatchLoaded.type =
  indigo.core.events.AssetEvent.AssetBatchLoaded

type AssetBatchLoadError = indigo.core.events.AssetEvent.AssetBatchLoadError
val AssetBatchLoadError: indigo.core.events.AssetEvent.AssetBatchLoadError.type =
  indigo.core.events.AssetEvent.AssetBatchLoadError

// Data

type FontChar = indigo.core.datatypes.FontChar
val FontChar: indigo.core.datatypes.FontChar.type = indigo.core.datatypes.FontChar

type FontInfo = indigo.core.datatypes.FontInfo
val FontInfo: indigo.core.datatypes.FontInfo.type = indigo.core.datatypes.FontInfo

type FontKey = indigo.core.datatypes.FontKey
val FontKey: indigo.core.datatypes.FontKey.type = indigo.core.datatypes.FontKey

type TextAlignment = indigo.core.datatypes.TextAlignment
val TextAlignment: indigo.core.datatypes.TextAlignment.type = indigo.core.datatypes.TextAlignment

type Rectangle = indigo.core.datatypes.Rectangle
val Rectangle: indigo.core.datatypes.Rectangle.type = indigo.core.datatypes.Rectangle

type Circle = indigo.core.datatypes.Circle
val Circle: indigo.core.datatypes.Circle.type = indigo.core.datatypes.Circle

type Point = indigo.core.datatypes.Point
val Point: indigo.core.datatypes.Point.type = indigo.core.datatypes.Point

type Size = indigo.core.datatypes.Size
val Size: indigo.core.datatypes.Size.type = indigo.core.datatypes.Size

type Vector2 = indigo.core.datatypes.Vector2
val Vector2: indigo.core.datatypes.Vector2.type = indigo.core.datatypes.Vector2

type Vector3 = indigo.core.datatypes.Vector3
val Vector3: indigo.core.datatypes.Vector3.type = indigo.core.datatypes.Vector3

type Vector4 = indigo.core.datatypes.Vector4
val Vector4: indigo.core.datatypes.Vector4.type = indigo.core.datatypes.Vector4

type Matrix3 = indigo.core.datatypes.Matrix3
val Matrix3: indigo.core.datatypes.Matrix3.type = indigo.core.datatypes.Matrix3

type Matrix4 = indigo.core.datatypes.Matrix4
val Matrix4: indigo.core.datatypes.Matrix4.type = indigo.core.datatypes.Matrix4

type Degrees = indigoengine.shared.datatypes.Degrees
val Degrees: indigoengine.shared.datatypes.Degrees.type = indigoengine.shared.datatypes.Degrees

type Radians = indigoengine.shared.datatypes.Radians
val Radians: indigoengine.shared.datatypes.Radians.type = indigoengine.shared.datatypes.Radians

type BindingKey = indigo.core.datatypes.BindingKey
val BindingKey: indigo.core.datatypes.BindingKey.type = indigo.core.datatypes.BindingKey

type Fill = indigo.core.datatypes.Fill
val Fill: indigo.core.datatypes.Fill.type = indigo.core.datatypes.Fill

type Stroke = indigo.core.datatypes.Stroke
val Stroke: indigo.core.datatypes.Stroke.type = indigo.core.datatypes.Stroke

type RGB = indigoengine.shared.datatypes.RGB
val RGB: indigoengine.shared.datatypes.RGB.type = indigoengine.shared.datatypes.RGB

type RGBA = indigoengine.shared.datatypes.RGBA
val RGBA: indigoengine.shared.datatypes.RGBA.type = indigoengine.shared.datatypes.RGBA

type Flip = indigo.core.datatypes.Flip
val Flip: indigo.core.datatypes.Flip.type = indigo.core.datatypes.Flip

// shared

type AssetType = indigo.core.assets.AssetType
val AssetType: indigo.core.assets.AssetType.type = indigo.core.assets.AssetType

type ResizePolicy = indigo.core.config.ResizePolicy
val ResizePolicy: indigo.core.config.ResizePolicy.type = indigo.core.config.ResizePolicy

type GameConfig = indigo.core.config.GameConfig
val GameConfig: indigo.core.config.GameConfig.type = indigo.core.config.GameConfig

type GameViewport = indigo.core.config.GameViewport
val GameViewport: indigo.core.config.GameViewport.type = indigo.core.config.GameViewport

type AdvancedGameConfig = indigo.core.config.AdvancedGameConfig
val AdvancedGameConfig: indigo.core.config.AdvancedGameConfig.type = indigo.core.config.AdvancedGameConfig

type RenderingTechnology = indigo.core.config.RenderingTechnology
val RenderingTechnology: indigo.core.config.RenderingTechnology.type = indigo.core.config.RenderingTechnology

val IndigoLogger: indigo.core.utils.IndigoLogger.type = indigo.core.utils.IndigoLogger

type Aseprite = shared.formats.Aseprite
val Aseprite: shared.formats.Aseprite.type = shared.formats.Aseprite

type SpriteAndAnimations = shared.formats.SpriteAndAnimations
val SpriteAndAnimations: shared.formats.SpriteAndAnimations.type = shared.formats.SpriteAndAnimations

type TiledMap = shared.formats.TiledMap
val TiledMap: shared.formats.TiledMap.type = shared.formats.TiledMap

type TiledGridMap[A] = shared.formats.TiledGridMap[A]
val TiledGridMap: shared.formats.TiledGridMap.type = shared.formats.TiledGridMap

type TiledGridLayer[A] = shared.formats.TiledGridLayer[A]
val TiledGridLayer: shared.formats.TiledGridLayer.type = shared.formats.TiledGridLayer

type TiledGridCell[A] = shared.formats.TiledGridCell[A]
val TiledGridCell: shared.formats.TiledGridCell.type = shared.formats.TiledGridCell

type TileSheet = indigo.shared.formats.TileSheet
val TileSheet: indigo.shared.formats.TileSheet.type = indigo.shared.formats.TileSheet

type Gamepad = indigo.core.input.Gamepad
val Gamepad: indigo.core.input.Gamepad.type = indigo.core.input.Gamepad

type GamepadDPad = indigo.core.input.GamepadDPad
val GamepadDPad: indigo.core.input.GamepadDPad.type = indigo.core.input.GamepadDPad

type GamepadAnalogControls = indigo.core.input.GamepadAnalogControls
val GamepadAnalogControls: indigo.core.input.GamepadAnalogControls.type = indigo.core.input.GamepadAnalogControls

type AnalogAxis = indigo.core.input.AnalogAxis
val AnalogAxis: indigo.core.input.AnalogAxis.type = indigo.core.input.AnalogAxis

type GamepadButtons = indigo.core.input.GamepadButtons
val GamepadButtons: indigo.core.input.GamepadButtons.type = indigo.core.input.GamepadButtons

type ImageType = shared.ImageType
val ImageType: shared.ImageType.type = shared.ImageType

type BoundaryLocator = indigo.scenegraph.registers.BoundaryLocator

type Context[StartUpData] = shared.Context[StartUpData]
val Context: shared.Context.type = shared.Context

type SubSystemContext[ReferenceData] = shared.subsystems.SubSystemContext[ReferenceData]
val SubSystemContext: shared.subsystems.SubSystemContext.type = shared.subsystems.SubSystemContext

//WebSockets

type WebSocketEvent = shared.networking.WebSocketEvent
val WebSocketEvent: shared.networking.WebSocketEvent.type = shared.networking.WebSocketEvent

type WebSocketConfig = shared.networking.WebSocketConfig
val WebSocketConfig: shared.networking.WebSocketConfig.type = shared.networking.WebSocketConfig

type WebSocketId = shared.networking.WebSocketId
val WebSocketId: shared.networking.WebSocketId.type = shared.networking.WebSocketId

type WebSocketReadyState = shared.networking.WebSocketReadyState
val WebSocketReadyState: shared.networking.WebSocketReadyState.type = shared.networking.WebSocketReadyState

// Http

val HttpMethod: shared.networking.HttpMethod.type = shared.networking.HttpMethod

type HttpRequest = shared.networking.HttpRequest
val HttpRequest: shared.networking.HttpRequest.type = shared.networking.HttpRequest

type HttpReceiveEvent = shared.networking.HttpReceiveEvent
val HttpReceiveEvent: shared.networking.HttpReceiveEvent.type = shared.networking.HttpReceiveEvent

val HttpError: shared.networking.HttpReceiveEvent.HttpError.type = shared.networking.HttpReceiveEvent.HttpError

type HttpResponse = shared.networking.HttpReceiveEvent.HttpResponse
val HttpResponse: shared.networking.HttpReceiveEvent.HttpResponse.type = shared.networking.HttpReceiveEvent.HttpResponse

// Scene graph

type SceneUpdateFragment = indigo.scenegraph.SceneUpdateFragment
val SceneUpdateFragment: indigo.scenegraph.SceneUpdateFragment.type = indigo.scenegraph.SceneUpdateFragment

type Camera = indigo.scenegraph.Camera
val Camera: indigo.scenegraph.Camera.type = indigo.scenegraph.Camera

type Zoom = indigo.scenegraph.Zoom
val Zoom: indigo.scenegraph.Zoom.type = indigo.scenegraph.Zoom

type Layer = indigo.scenegraph.Layer
val Layer: indigo.scenegraph.Layer.type = indigo.scenegraph.Layer

type LayerEntry = indigo.scenegraph.LayerEntry
val LayerEntry: indigo.scenegraph.LayerEntry.type = indigo.scenegraph.LayerEntry

type LayerKey = indigo.scenegraph.LayerKey
val LayerKey: indigo.scenegraph.LayerKey.type = indigo.scenegraph.LayerKey

type Blending = indigo.scenegraph.Blending
val Blending: indigo.scenegraph.Blending.type = indigo.scenegraph.Blending

type Blend = indigo.scenegraph.Blend
val Blend: indigo.scenegraph.Blend.type = indigo.scenegraph.Blend

type BlendFactor = indigo.scenegraph.BlendFactor
val BlendFactor: indigo.scenegraph.BlendFactor.type = indigo.scenegraph.BlendFactor

type SceneNode = indigo.scenegraph.SceneNode
val SceneNode: indigo.scenegraph.SceneNode.type = indigo.scenegraph.SceneNode

type EntityNode[T <: indigo.scenegraph.SceneNode]    = indigo.scenegraph.EntityNode[T]
type DependentNode[T <: indigo.scenegraph.SceneNode] = indigo.scenegraph.DependentNode[T]
type RenderNode[T <: indigo.scenegraph.SceneNode]    = indigo.scenegraph.RenderNode[T]

// Audio
type SceneAudio = indigo.scenegraph.SceneAudio
val SceneAudio: indigo.scenegraph.SceneAudio.type = indigo.scenegraph.SceneAudio

type PlaybackPolicy = indigo.core.audio.PlaybackPolicy
val PlaybackPolicy: indigo.core.audio.PlaybackPolicy.type = indigo.core.audio.PlaybackPolicy

type Track = indigo.core.audio.Track
val Track: indigo.core.audio.Track.type = indigo.core.audio.Track

type Volume = indigo.core.audio.Volume
val Volume: indigo.core.audio.Volume.type = indigo.core.audio.Volume

type PlaybackPattern = indigo.scenegraph.PlaybackPattern
val PlaybackPattern: indigo.scenegraph.PlaybackPattern.type = indigo.scenegraph.PlaybackPattern

type SceneAudioSource = indigo.scenegraph.SceneAudioSource
val SceneAudioSource: indigo.scenegraph.SceneAudioSource.type = indigo.scenegraph.SceneAudioSource

// Animation
type Animation = indigo.core.animation.Animation
val Animation: indigo.core.animation.Animation.type = indigo.core.animation.Animation

type Cycle = indigo.core.animation.Cycle
val Cycle: indigo.core.animation.Cycle.type = indigo.core.animation.Cycle

type CycleLabel = indigo.core.animation.CycleLabel
val CycleLabel: indigo.core.animation.CycleLabel.type = indigo.core.animation.CycleLabel

type Frame = indigo.core.animation.Frame
val Frame: indigo.core.animation.Frame.type = indigo.core.animation.Frame

type AnimationKey = indigo.core.animation.AnimationKey
val AnimationKey: indigo.core.animation.AnimationKey.type = indigo.core.animation.AnimationKey

type AnimationAction = indigo.core.animation.AnimationAction
val AnimationAction: indigo.core.animation.AnimationAction.type = indigo.core.animation.AnimationAction

// Timeline Animations
type Timeline[A] = indigo.core.animation.timeline.Timeline[A]
val Timeline: indigo.core.animation.timeline.Timeline.type = indigo.core.animation.timeline.Timeline

type TimelineWindow[A] = indigo.core.animation.timeline.TimeWindow[A]
val TimelineWindow: indigo.core.animation.timeline.TimeWindow.type = indigo.core.animation.timeline.TimeWindow

type TimeSlot[A] = indigo.core.animation.timeline.TimeSlot[A]
val TimeSlot: indigo.core.animation.timeline.TimeSlot.type = indigo.core.animation.timeline.TimeSlot

type TimelineAnimation[A] = indigo.core.animation.timeline.TimelineAnimation[A]
val TimelineAnimation: indigo.core.animation.timeline.TimelineAnimation.type =
  indigo.core.animation.timeline.TimelineAnimation

// Primitives
type BlankEntity = indigo.scenegraph.BlankEntity
val BlankEntity: indigo.scenegraph.BlankEntity.type = indigo.scenegraph.BlankEntity

type Shape[T <: indigo.scenegraph.Shape[?]] = indigo.scenegraph.Shape[T]
val Shape: indigo.scenegraph.Shape.type = indigo.scenegraph.Shape

type Sprite[M <: Material] = indigo.scenegraph.Sprite[M]
val Sprite: indigo.scenegraph.Sprite.type = indigo.scenegraph.Sprite

type Text[M <: Material] = indigo.scenegraph.Text[M]
val Text: indigo.scenegraph.Text.type = indigo.scenegraph.Text

type Graphic[M <: Material] = indigo.scenegraph.Graphic[M]
val Graphic: indigo.scenegraph.Graphic.type = indigo.scenegraph.Graphic

type Group = indigo.scenegraph.Group
val Group: indigo.scenegraph.Group.type = indigo.scenegraph.Group

type Clip[M <: Material] = indigo.scenegraph.Clip[M]
val Clip: indigo.scenegraph.Clip.type = indigo.scenegraph.Clip

type ClipSheet = indigo.scenegraph.ClipSheet
val ClipSheet: indigo.scenegraph.ClipSheet.type = indigo.scenegraph.ClipSheet

type ClipSheetArrangement = indigo.scenegraph.ClipSheetArrangement
val ClipSheetArrangement: indigo.scenegraph.ClipSheetArrangement.type = indigo.scenegraph.ClipSheetArrangement

type ClipPlayDirection = indigo.scenegraph.ClipPlayDirection
val ClipPlayDirection: indigo.scenegraph.ClipPlayDirection.type = indigo.scenegraph.ClipPlayDirection

type ClipPlayMode = indigo.scenegraph.ClipPlayMode
val ClipPlayMode: indigo.scenegraph.ClipPlayMode.type = indigo.scenegraph.ClipPlayMode

// Clones
type Cloneable = indigo.scenegraph.Cloneable

type CloneBlank = indigo.scenegraph.CloneBlank
val CloneBlank: indigo.scenegraph.CloneBlank.type = indigo.scenegraph.CloneBlank

type CloneId = indigo.scenegraph.CloneId
val CloneId: indigo.scenegraph.CloneId.type = indigo.scenegraph.CloneId

type CloneBatch = indigo.scenegraph.CloneBatch
val CloneBatch: indigo.scenegraph.CloneBatch.type = indigo.scenegraph.CloneBatch

type CloneBatchData = indigo.scenegraph.CloneBatchData
val CloneBatchData: indigo.scenegraph.CloneBatchData.type = indigo.scenegraph.CloneBatchData

type CloneTiles = indigo.scenegraph.CloneTiles
val CloneTiles: indigo.scenegraph.CloneTiles.type = indigo.scenegraph.CloneTiles

type CloneTileData = indigo.scenegraph.CloneTileData
val CloneTileData: indigo.scenegraph.CloneTileData.type = indigo.scenegraph.CloneTileData

type Mutants = indigo.scenegraph.Mutants
val Mutants: indigo.scenegraph.Mutants.type = indigo.scenegraph.Mutants

// Lights
type Light = indigo.scenegraph.Light

type PointLight = indigo.scenegraph.PointLight
val PointLight: indigo.scenegraph.PointLight.type = indigo.scenegraph.PointLight

type SpotLight = indigo.scenegraph.SpotLight
val SpotLight: indigo.scenegraph.SpotLight.type = indigo.scenegraph.SpotLight

type DirectionLight = indigo.scenegraph.DirectionLight
val DirectionLight: indigo.scenegraph.DirectionLight.type = indigo.scenegraph.DirectionLight

type AmbientLight = indigo.scenegraph.AmbientLight
val AmbientLight: indigo.scenegraph.AmbientLight.type = indigo.scenegraph.AmbientLight

type Falloff = indigo.scenegraph.Falloff
val Falloff: indigo.scenegraph.Falloff.type = indigo.scenegraph.Falloff

type Lens[A, B] = indigoengine.shared.lenses.Lens[A, B]
val Lens: indigoengine.shared.lenses.Lens.type = indigoengine.shared.lenses.Lens

// Geometry

type Bezier = indigo.core.geometry.Bezier
val Bezier: indigo.core.geometry.Bezier.type = indigo.core.geometry.Bezier

type BoundingBox = indigo.core.geometry.BoundingBox
val BoundingBox: indigo.core.geometry.BoundingBox.type = indigo.core.geometry.BoundingBox

type BoundingCircle = indigo.core.geometry.BoundingCircle
val BoundingCircle: indigo.core.geometry.BoundingCircle.type = indigo.core.geometry.BoundingCircle

type Line = indigo.core.geometry.Line
val Line: indigo.core.geometry.Line.type = indigo.core.geometry.Line

type LineSegment = indigo.core.geometry.LineSegment
val LineSegment: indigo.core.geometry.LineSegment.type = indigo.core.geometry.LineSegment

type Polygon = indigo.core.geometry.Polygon
val Polygon: indigo.core.geometry.Polygon.type = indigo.core.geometry.Polygon

type Vertex = indigo.core.geometry.Vertex
val Vertex: indigo.core.geometry.Vertex.type = indigo.core.geometry.Vertex

// Trees

type SpatialOps[S] = indigo.core.trees.SpatialOps[S]
val SpatialOps: indigo.core.trees.SpatialOps.type = indigo.core.trees.SpatialOps

type QuadTree[S, T] = indigo.core.trees.QuadTree[S, T]
val QuadTree: indigo.core.trees.QuadTree.type = indigo.core.trees.QuadTree

type QuadTreeValue[S, T] = indigo.core.trees.QuadTreeValue[S, T]
val QuadTreeValue: indigo.core.trees.QuadTreeValue.type = indigo.core.trees.QuadTreeValue
