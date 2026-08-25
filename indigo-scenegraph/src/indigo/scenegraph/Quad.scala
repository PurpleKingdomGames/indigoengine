package indigo.scenegraph

import indigo.core.datatypes.Fill
import indigo.core.datatypes.Flip
import indigo.core.datatypes.Point
import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.Size
import indigo.core.datatypes.Vector2
import indigo.core.events.GlobalEvent
import indigo.scenegraph.materials.LightingModel
import indigo.shaders.ShaderData
import indigo.shaders.ShaderPrimitive
import indigo.shaders.StandardShaders
import indigo.shaders.Uniform
import indigo.shaders.UniformBlock
import indigo.shaders.UniformBlockName
import indigo.shaders.UniformDataHelpers
import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.Radians

/** `Quad`s are used when you want to draw a pixel accurate box, with or without individual rounded corners. Unlike
  * `Shape.Box`, `Quad` does NOT support a `Stroke` (..though you can simulate it with another `Quad`), but it is pixel
  * accurate[^1] (as long as it is not rotated), allows you to define individual corner radii, and use either solid
  * colors or gradient fills. An expected use-case would be drawing progress bars.
  *
  * [^1]: `Shape.Box` can suffer from floating point errors due to the SDF based drawing method common to all `Shape`
  * types.
  */
final case class Quad(
    dimensions: Rectangle,
    fill: Fill,
    corners: Corners,
    lighting: LightingModel,
    eventHandlerEnabled: Boolean,
    eventHandler: ((Quad, GlobalEvent)) => Option[GlobalEvent],
    rotation: Radians,
    scale: Vector2,
    ref: Point,
    flip: Flip
) extends EntityNode[Quad]
    with Cloneable
    with SpatialModifiers[Quad] derives CanEqual:

  lazy val position: Point = dimensions.position
  lazy val size: Size      = dimensions.size

  def withDimensions(newDimensions: Rectangle): Quad =
    this.copy(dimensions = newDimensions)

  def withFill(newFill: Fill): Quad =
    this.copy(fill = newFill)
  def modifyFill(modifier: Fill => Fill): Quad =
    this.copy(fill = modifier(fill))

  def withCorners(newCorners: Corners): Quad =
    this.copy(corners = newCorners)
  def modifyCorners(modifier: Corners => Corners): Quad =
    this.copy(corners = modifier(corners))

  def withLighting(newLighting: LightingModel): Quad =
    this.copy(lighting = newLighting)
  def modifyLighting(modifier: LightingModel => LightingModel): Quad =
    this.copy(lighting = modifier(lighting))

  def moveTo(pt: Point): Quad =
    this.copy(dimensions = dimensions.moveTo(pt))
  def moveTo(x: Int, y: Int): Quad =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): Quad =
    moveTo(newPosition)

  def moveBy(pt: Point): Quad =
    this.copy(dimensions = dimensions.moveBy(pt))
  def moveBy(x: Int, y: Int): Quad =
    moveBy(Point(x, y))

  def rotateTo(angle: Radians): Quad =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): Quad =
    rotateTo(rotation + angle)
  def withRotation(newRotation: Radians): Quad =
    rotateTo(newRotation)

  def scaleBy(amount: Vector2): Quad =
    this.copy(scale = scale * amount)
  def scaleBy(x: Double, y: Double): Quad =
    scaleBy(Vector2(x, y))
  def withScale(newScale: Vector2): Quad =
    this.copy(scale = newScale)

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Quad =
    this.copy(dimensions = dimensions.moveTo(newPosition), rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Quad =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def flipHorizontal(isFlipped: Boolean): Quad =
    this.copy(flip = flip.withHorizontalFlip(isFlipped))
  def flipVertical(isFlipped: Boolean): Quad =
    this.copy(flip = flip.withVerticalFlip(isFlipped))
  def withFlip(newFlip: Flip): Quad =
    this.copy(flip = newFlip)

  def withRef(newRef: Point): Quad =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): Quad =
    withRef(Point(x, y))

  def resizeTo(newSize: Size): Quad =
    this.copy(dimensions = dimensions.resize(newSize))
  def resizeTo(width: Int, height: Int): Quad =
    resizeTo(Size(width, height))
  def withSize(newSize: Size): Quad =
    resizeTo(newSize)

  def resizeBy(amount: Size): Quad =
    this.copy(dimensions = dimensions.resizeBy(amount))
  def resizeBy(width: Int, height: Int): Quad =
    resizeBy(Size(width, height))

  def withEventHandler(f: ((Quad, GlobalEvent)) => Option[GlobalEvent]): Quad =
    this.copy(eventHandler = f, eventHandlerEnabled = true)
  def onEvent(f: PartialFunction[(Quad, GlobalEvent), GlobalEvent]): Quad =
    withEventHandler(f.lift)
  def enableEvents: Quad =
    this.copy(eventHandlerEnabled = true)
  def disableEvents: Quad =
    this.copy(eventHandlerEnabled = false)

  private def fillType(fill: Fill): Float =
    fill match {
      case _: Fill.Color          => 0.0f
      case _: Fill.LinearGradient => 1.0f
      case _: Fill.RadialGradient => 2.0f
    }

  def toShaderData: ShaderData =
    val clampedCorners = corners.clampTo(size)

    val shapeUniformBlock =
      UniformBlock(
        UniformBlockName("IndigoQuadData"),
        // FILL_TYPE (float), 0.0 x3 (empty), CORNER_RADII (vec4)
        Batch(
          Uniform("Quad_DATA") -> ShaderPrimitive.rawBatch(
            Batch[Float](
              fillType(fill),
              0.0,
              0.0,
              0.0,
              clampedCorners.topLeft.toFloat,
              clampedCorners.topRight.toFloat,
              clampedCorners.bottomRight.toFloat,
              clampedCorners.bottomLeft.toFloat
            )
          )
        ) ++ UniformDataHelpers.fillToUniformData(fill, "QUAD")
      )

    lighting match {
      case LightingModel.Unlit =>
        ShaderData(
          StandardShaders.Quad.id,
          Batch(shapeUniformBlock)
        )

      case l: LightingModel.Lit =>
        l.toShaderData(StandardShaders.LitQuad.id, None, Batch(shapeUniformBlock))
    }

object Quad:

  def apply(dimensions: Rectangle, fill: Fill): Quad =
    Quad(
      dimensions,
      fill,
      Corners.zero,
      LightingModel.Unlit,
      false,
      Function.const(None),
      Radians.zero,
      Vector2.one,
      Point.zero,
      Flip.default
    )

  def apply(dimensions: Rectangle, fill: Fill, corners: Corners): Quad =
    Quad(
      dimensions,
      fill,
      corners,
      LightingModel.Unlit,
      false,
      Function.const(None),
      Radians.zero,
      Vector2.one,
      Point.zero,
      Flip.default
    )
