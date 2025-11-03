package ultraviolet

import ultraviolet.syntax.*

object sdf:

  /** Determines the distance from a point to edge of a box centered at the origin.
    */
  inline private def _box(point: vec2, halfSize: vec2): Float =
    val d = abs(point) - halfSize
    length(max(d, 0.0f)) + min(max(d.x, d.y), 0.0f)

  inline def box(point: vec2, halfSize: vec2): Float =
    // Delegate to reduce the chance of argument name collisions
    val proxy: (vec2, vec2) => Float = (_ptArg, _hsArg) => _box(_ptArg, _hsArg)
    proxy(point, halfSize)

  /** Determines the distance from a point to edge of a circle centered at the origin.
    */
  inline private def _circle(point: vec2, radius: Float): Float =
    length(point) - radius

  inline def circle(point: vec2, radius: Float): Float =
    // Delegate to reduce the chance of argument name collisions
    val proxy: (vec2, Float) => Float = (_ptArg, _rArg) => _circle(_ptArg, _rArg)
    proxy(point, radius)

  /** Determines the distance from a point to edge of a hexagon centered at the origin.
    */
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  inline private def _hexagon(point: vec2, radius: Float): Float =
    val k  = vec3(-0.866025404f, 0.5f, 0.577350269f)
    var pt = abs(point)
    pt = pt - (2.0f * min(dot(k.xy, pt), 0.0f) * k.xy)
    pt = pt - (vec2(clamp(pt.x, -k.z * radius, k.z * radius), radius))
    length(pt) * sign(pt.y)

  inline def hexagon(point: vec2, radius: Float): Float =
    // Delegate to reduce the chance of argument name collisions
    val proxy: (vec2, Float) => Float = (_ptArg, _rArg) => _hexagon(_ptArg, _rArg)
    proxy(point, radius)

  /** Determines the distance from a point to edge of a segment centered at the origin.
    */
  inline private def _segment(point: vec2, a: vec2, b: vec2): Float =
    val pa = point - a
    val ba = b - a
    val h  = clamp(dot(pa, ba) / dot(ba, ba), 0.0f, 1.0f)
    length(pa - ba * h)

  inline def segment(point: vec2, a: vec2, b: vec2): Float =
    // Delegate to reduce the chance of argument name collisions
    val proxy: (vec2, vec2, vec2) => Float = (_ptArg, _aArg, _bArg) => _segment(_ptArg, _aArg, _bArg)
    proxy(point, a, b)

  /** Determines the distance from a point to edge of a five pointed star centered at the origin.
    */
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  inline private def _star(point: vec2, radius: Float, innerRadius: Float): Float =
    val k1: vec2 = vec2(0.809016994375f, -0.587785252292f)
    val k2: vec2 = vec2(-k1.x, k1.y)
    var p2       = vec2(abs(point.x), -point.y)

    p2 = p2 - 2.0f * max(dot(k1, p2), 0.0f) * k1
    p2 = p2 - 2.0f * max(dot(k2, p2), 0.0f) * k2
    p2 = vec2(abs(p2.x), p2.y - radius)

    val ba: vec2 = innerRadius * vec2(-k1.y, k1.x) - vec2(0.0f, 1.0f)
    val h: Float = clamp(dot(p2, ba) / dot(ba, ba), 0.0f, radius)

    length(p2 - ba * h) * sign(p2.y * ba.x - p2.x * ba.y)

  inline def star(point: vec2, radius: Float, innerRadius: Float): Float =
    // Delegate to reduce the chance of argument name collisions
    val proxy: (vec2, Float, Float) => Float = (_ptArg, _rArg, _irArg) => _star(_ptArg, _rArg, _irArg)
    proxy(point, radius, innerRadius)

  /** Determines the distance from a point to edge of an equilateral triangle centered at the origin.
    */
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  inline private def _triangle(point: vec2, radius: Float): Float =
    val k  = sqrt(3.0f)
    var pt = vec2(abs(point.x) - radius, -point.y + radius / k)
    if (pt.x + k * pt.y > 0.0f)
      pt = vec2(pt.x - k * pt.y, -k * pt.x - pt.y) / 2.0f
    pt = vec2(pt.x - clamp(pt.x, -radius, 0.0f), pt.y)
    -length(pt) * sign(pt.y)

  inline def triangle(point: vec2, radius: Float): Float =
    // Delegate to reduce the chance of argument name collisions
    val proxy: (vec2, Float) => Float = (_ptArg, _rArg) => _triangle(_ptArg, _rArg)
    proxy(point, radius)
