

import indigo.*

final case class CustomEntity(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    depth: Depth,
    shader: ShaderData
) extends EntityNode[CustomEntity]:
  val flip: Flip                    = Flip.default
  val position: Point               = Point(x, y)
  val size: Size                    = Size(width, height)
  val ref: Point                    = Point.zero
  val rotation: Radians             = Radians.zero
  val scale: Vector2                = Vector2.one
  lazy val toShaderData: ShaderData = shader

  def withDepth(newDepth: Depth): CustomEntity =
    this.copy(depth = newDepth)

  val eventHandlerEnabled: Boolean = false
  def eventHandler: ((CustomEntity, GlobalEvent)) => Option[GlobalEvent] =
    Function.const(None)
