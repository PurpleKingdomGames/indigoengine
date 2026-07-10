package indigoextras.subsystems

import indigo.core.Outcome
import indigo.core.assets.AssetName
import indigo.core.datatypes.Fill
import indigo.core.datatypes.FontKey
import indigo.core.datatypes.LayerKey
import indigo.core.datatypes.Point
import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.Size
import indigo.core.events.FrameTick
import indigo.core.events.GlobalEvent
import indigo.core.time.FPS
import indigo.scenegraph.Layer
import indigo.scenegraph.SceneUpdateFragment
import indigo.scenegraph.Shape
import indigo.scenegraph.Text
import indigo.scenegraph.materials.Material
import indigo.shared.Context
import indigo.shared.subsystems.SubSystem
import indigo.shared.subsystems.SubSystemContext
import indigo.shared.subsystems.SubSystemId
import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.RGBA
import indigoengine.shared.datatypes.Seconds

final case class FPSCounter[Model](
    id: SubSystemId,
    place: (Context, Size) => Point,
    targetFPS: Option[FPS],
    thresholds: Batch[FPSThreshold],
    layerKey: LayerKey,
    fontKey: FontKey,
    fontAsset: AssetName
) extends SubSystem[Model]:
  type EventType      = GlobalEvent
  type SubSystemModel = FPSCounterState
  type ReferenceData  = Unit

  private val idealFps: Int = targetFPS.getOrElse(FPS.`60`).toInt
  private val defaultThresholds: Batch[FPSThreshold] =
    Batch(
      FPSThreshold(0, RGBA.Red),
      FPSThreshold(idealFps / 2, RGBA.Yellow),
      FPSThreshold(idealFps - (idealFps * 0.05).toInt, RGBA.Green)
    )
  private val decideNextFps: Int => Int =
    targetFPS match
      case None =>
        frameCountSinceInterval => frameCountSinceInterval + 1

      case Some(target) =>
        frameCountSinceInterval => Math.min(target.toInt, frameCountSinceInterval + 1)

  def withPlaceFunction(
      place: (Context, Size) => Point
  ): FPSCounter[Model] =
    this.copy(place = place)

  def withThresholds(thresholds: Batch[FPSThreshold]): FPSCounter[Model] =
    this.copy(thresholds = thresholds)
  def addThreshold(threshold: FPSThreshold): FPSCounter[Model] =
    this.copy(thresholds = thresholds :+ threshold)
  def addThreshold(value: Int, color: RGBA): FPSCounter[Model] =
    addThreshold(FPSThreshold(value, color))

  def moveTo(position: Point): FPSCounter[Model] =
    withPlaceFunction(place = (_, _) => position)
  def moveTo(x: Int, y: Int): FPSCounter[Model] =
    moveTo(Point(x, y))

  def placeAt(location: (Context, Size) => Point): FPSCounter[Model] =
    withPlaceFunction(place = location)

  def withTargetFPS(targetFPS: FPS): FPSCounter[Model] =
    this.copy(targetFPS = Option(targetFPS))
  def clearTargetFPS: FPSCounter[Model] =
    this.copy(targetFPS = None)

  def withLayerKey(layerKey: LayerKey): FPSCounter[Model] =
    this.copy(layerKey = layerKey)

  def eventFilter: GlobalEvent => Option[EventType] = {
    case FrameTick          => Some(FrameTick)
    case e: FPSCounter.Move => Some(e)
    case _                  => None
  }

  def reference(model: Model): ReferenceData =
    ()

  def initialModel: Outcome[SubSystemModel] =
    Outcome(FPSCounterState.initial(place))

  private val textInstance: Text[Material.ImageEffects] =
    Text(
      "",
      fontKey,
      Material.ImageEffects(fontAsset)
    )

  def update(
      context: SubSystemContext[ReferenceData],
      model: FPSCounterState
  ): GlobalEvent => Outcome[FPSCounterState] = {
    case FrameTick =>
      val bounds: Rectangle =
        if model.bounds.size == Size.zero then
          context.services.bounds
            .find(textInstance.withText(formatText(idealFps.toString)))
            .getOrElse(Rectangle.zero)
            .expand(2)
        else model.bounds

      val boxSize =
        ({ (s: Size) =>
          Size(
            if s.width  % 2 == 0 then s.width else s.width + 1,
            if s.height % 2 == 0 then s.height else s.height + 1
          )
        })(bounds.size)

      if (context.frame.time.running >= (model.lastInterval + Seconds(1)))
        Outcome(
          model.copy(
            bounds = Rectangle(model.placeFunction(context.toContext, boxSize), boxSize),
            fps = decideNextFps(model.frameCountSinceInterval),
            lastInterval = context.frame.time.running,
            frameCountSinceInterval = 0
          )
        )
      else Outcome(model.copy(frameCountSinceInterval = model.frameCountSinceInterval + 1))

    case FPSCounter.Move(to) =>
      Outcome(model.copy(placeFunction = (_, _) => to))

    case _ =>
      Outcome(model)
  }

  def present(context: SubSystemContext[ReferenceData], model: FPSCounterState): Outcome[SceneUpdateFragment] =
    val text: Text[Material.ImageEffects] =
      textInstance
        .withText(formatText(model.fps.toString))
        .moveTo(model.bounds.position + 2)
        .modifyMaterial(_.withTint(pickTint(model.fps)))

    val bg: Shape.Box =
      Shape
        .Box(model.bounds, Fill.Color(RGBA.Black.withAlpha(0.5)))

    Outcome(
      SceneUpdateFragment(
        layerKey -> Layer(bg, text)
      )
    )

  private def formatText(fps: String): String =
    s"""FPS $fps"""

  private def pickTint(fps: Int): RGBA =
    (if thresholds.isEmpty then defaultThresholds else thresholds)
      .filter(_.met(fps))
      .maxByOption(_.threshold)
      .map(_.color)
      .getOrElse(RGBA.Silver)

object FPSCounter:

  val DefaultId: SubSystemId = SubSystemId("[indigo_FPSCounter_subsystem]")

  private val defaultPlaceFunction: (Context, Size) => Point =
    (_, _) => Point(0, 0)

  def apply[Model](fontKey: FontKey, fontAsset: AssetName, layerKey: LayerKey): FPSCounter[Model] =
    FPSCounter(DefaultId, defaultPlaceFunction, None, Batch.empty, layerKey, fontKey, fontAsset)

  def apply[Model](fontKey: FontKey, fontAsset: AssetName, layerKey: LayerKey, targetFPS: FPS): FPSCounter[Model] =
    FPSCounter(DefaultId, defaultPlaceFunction, Option(targetFPS), Batch.empty, layerKey, fontKey, fontAsset)

  def apply[Model](
      id: SubSystemId,
      position: Point,
      fontKey: FontKey,
      fontAsset: AssetName,
      layerKey: LayerKey
  ): FPSCounter[Model] =
    FPSCounter(id, (_, _) => position, None, Batch.empty, layerKey, fontKey, fontAsset)

  def apply[Model](
      id: SubSystemId,
      fontKey: FontKey,
      fontAsset: AssetName,
      layerKey: LayerKey,
      targetFPS: FPS
  ): FPSCounter[Model] =
    FPSCounter(id, defaultPlaceFunction, Option(targetFPS), Batch.empty, layerKey, fontKey, fontAsset)

  def apply[Model](
      id: SubSystemId,
      fontKey: FontKey,
      fontAsset: AssetName,
      layerKey: LayerKey
  ): FPSCounter[Model] =
    FPSCounter(id, defaultPlaceFunction, None, Batch.empty, layerKey, fontKey, fontAsset)

  final case class Move(to: Point) extends GlobalEvent

final case class FPSCounterState(
    placeFunction: (Context, Size) => Point,
    bounds: Rectangle,
    fps: Int,
    lastInterval: Seconds,
    frameCountSinceInterval: Int
)
object FPSCounterState:
  def initial(place: (Context, Size) => Point): FPSCounterState =
    FPSCounterState(place, Rectangle.zero, 0, Seconds.zero, 0)

final case class FPSThreshold(threshold: Int, color: RGBA):
  def met(value: Int): Boolean =
    value >= threshold
