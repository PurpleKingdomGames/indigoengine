package indigo

import indigo.shaders.UniformDataHelpers

import scala.annotation.targetName

object syntax:

  export indigoengine.shared.syntax.*

  extension (d: Double)
    def toVolume: Volume = Volume(d)
    def volume: Volume   = Volume(d)
    def toZoom: Zoom     = Zoom(d)
    def zoom: Zoom       = Zoom(d)

  extension (i: Int)
    def toFPS: FPS = FPS(i)
    def fps: FPS   = FPS(i)

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
    def toScene: SceneName           = SceneName(s)
    def toShaderId: ShaderId         = ShaderId(s)

  extension (t: (Double, Double)) def toVector2: Vector2 = Vector2(t._1, t._2)

  extension (t: (Double, Double, Double)) def toVector3: Vector3 = Vector3(t._1, t._2, t._3)

  extension (t: (Double, Double, Double, Double)) def toVector4: Vector4 = Vector4(t._1, t._2, t._3, t._4)

  extension (t: (Int, Int))
    def toPoint: Point = Point(t._1, t._2)
    def toSize: Size   = Size(t._1, t._2)

  extension [A](values: Option[A]) def toOutcome(error: => Throwable): Outcome[A] = Outcome.fromOption(values, error)

  extension [A](b: Batch[Outcome[A]]) def sequence: Outcome[Batch[A]]                 = Outcome.sequenceBatch(b)
  extension [A](b: NonEmptyBatch[Outcome[A]]) def sequence: Outcome[NonEmptyBatch[A]] = Outcome.sequenceNonEmptyBatch(b)
  extension [A](l: List[Outcome[A]]) def sequence: Outcome[List[A]]                   = Outcome.sequenceList(l)
  extension [A](l: NonEmptyList[Outcome[A]]) def sequence: Outcome[NonEmptyList[A]]   = Outcome.sequenceNonEmptyList(l)

  extension (s: Size) def toGameViewport: GameViewport = GameViewport(s)

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
    // TODO: Remove?
    // extension (a: scalajs.js.Array[Float])
    //   def toUVArray: ultraviolet.syntax.array[Singleton & Int, Float] =
    //     ultraviolet.syntax.array(a.toArray)

  end shaders
end syntax
