package ultraviolet

import ultraviolet.syntax.*

object sdf:

  /** Determines the distance from a point to edge of a circle centered at the origin.
    */
  inline def circle(point: vec2, radius: Float): Float =
    length(point) - radius

  /** Determines the distance from a point to edge of a box centered at the origin.
    */
  inline def box(point: vec2, halfSize: vec2): Float =
    val d = abs(point) - halfSize
    length(max(d, 0.0f)) + min(max(d.x, d.y), 0.0f)

  /** Determines the distance from a point to edge of a five pointed star centered at the origin.
    */
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  inline def star(point: vec2, radius: Float, innerRadius: Float): Float =
    val k1: vec2 = vec2(0.809016994375f, -0.587785252292f)
    val k2: vec2 = vec2(-k1.x, k1.y)
    var p2       = vec2(abs(point.x), point.y)

    p2 = p2 - 2.0f * max(dot(k1, p2), 0.0f) * k1
    p2 = p2 - 2.0f * max(dot(k2, p2), 0.0f) * k2
    p2 = vec2(abs(p2.x), p2.y - radius)

    val ba: vec2 = innerRadius * vec2(-k1.y, k1.x) - vec2(0.0f, 1.0f)
    val h: Float = clamp(dot(p2, ba) / dot(ba, ba), 0.0f, radius)

    length(p2 - ba * h) * sign(p2.y * ba.x - p2.x * ba.y)
