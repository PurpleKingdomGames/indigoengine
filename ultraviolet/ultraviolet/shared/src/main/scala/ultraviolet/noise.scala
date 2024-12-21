package ultraviolet

import ultraviolet.syntax.*

object noise:

  // Cellular noise

  inline private def _mod289Vec3(x: vec3): vec3 =
    x - (floor(x * (1.0f / 289.0f)) * 289.0f)

  inline private def _mod289Vec2(x: vec2): vec2 =
    x - (floor(x * (1.0f / 289.0f)) * 289.0f)

  inline private def _mod7(x: vec3): vec3 =
    x - (floor(x * (1.0f / 7.0f)) * 7.0f)

  inline private def _permute(x: vec3): vec3 =
    val mod289Vec3: vec3 => vec3 = value => _mod289Vec3(value)
    mod289Vec3((34.0f * x + 10.0f) * x)

  inline def cellular(P: vec2): vec2 = {
    val mod289Vec2: vec2 => vec2 = x => _mod289Vec2(x)
    val mod7: vec3 => vec3       = x => _mod7(x)
    val permute: vec3 => vec3    = x => _permute(x)

    val K      = 0.142857142857f
    val Ko     = 0.428571428571f
    val jitter = 1.0f

    val Pi: vec2 = mod289Vec2(floor(P))
    val Pf: vec2 = fract(P)

    val oi: vec3 = vec3(-1.0, 0.0, 1.0)
    val of: vec3 = vec3(-0.5, 0.5, 1.5)
    val px: vec3 = permute(Pi.x + oi)
    var p: vec3  = permute(px.x + Pi.y + oi)
    var ox: vec3 = fract(p * K) - Ko
    var oy: vec3 = mod7(floor(p * K)) * K - Ko
    var dx: vec3 = Pf.x + 0.5f + jitter * ox
    var dy: vec3 = Pf.y - of + jitter * oy
    var d1: vec3 = dx * dx + dy * dy

    p = permute(px.y + Pi.y + oi)
    ox = fract(p * K) - Ko
    oy = mod7(floor(p * K)) * K - Ko
    dx = Pf.x - 0.5f + jitter * ox
    dy = Pf.y - of + jitter * oy
    var d2: vec3 = dx * dx + dy * dy

    p = permute(px.z + Pi.y + oi)
    ox = fract(p * K) - Ko
    oy = mod7(floor(p * K)) * K - Ko
    dx = Pf.x - 1.5f + jitter * ox
    dy = Pf.y - of + jitter * oy
    val d3: vec3 = dx * dx + dy * dy

    val d1a: vec3 = min(d1, d2)
    d2 = max(d1, d2)
    d2 = min(d2, d3)
    d1 = min(d1a, d2)
    d2 = max(d1a, d2)

    val d1Flip1 = if (d1.x < d1.y) d1 else vec3(d1.y, d1.x, d1.z)
    val d1Flip2 =
      if (d1Flip1.x < d1Flip1.z) d1Flip1
      else vec3(d1Flip1.z, d1Flip1.y, d1Flip1.x)

    var d1Flip3: vec3 = vec3(d1Flip2.x, min(d1Flip2.yz, d2.yz))
    d1Flip3 = vec3(d1Flip3.x, min(d1Flip3.y, d1Flip3.z), d1Flip3.z)
    d1Flip3 = vec3(d1Flip3.x, min(d1Flip3.y, d2.x), d1Flip3.z)

    sqrt(d1Flip3.xy)
  }

  // Classic Perlin noise

  inline private def _mod289Vec4(x: vec4): vec4 =
    x - floor(x * (1.0f / 289.0f)) * 289.0f

  inline private def _permuteVec4(x: vec4): vec4 =
    val mod289Vec4: vec4 => vec4 = value => _mod289Vec4(value)
    mod289Vec4(((x * 34.0f) + 10.0f) * x)

  inline private def _taylorInvSqrt(r: vec4): vec4 =
    1.79284291400159f - 0.85373472095314f * r

  inline private def _fade(t: vec2): vec2 =
    t * t * t * (t * (t * 6.0f - 15.0f) + 10.0f)

  inline def perlin(P: vec2): Float =
    val mod289Vec4: vec4 => vec4    = x => _mod289Vec4(x)
    val permuteVec4: vec4 => vec4   = x => _permuteVec4(x)
    val taylorInvSqrt: vec4 => vec4 = x => _taylorInvSqrt(x)
    val fade: vec2 => vec2          = x => _fade(x)

    var Pi: vec4 = floor(P.xyxy) + vec4(0.0, 0.0, 1.0, 1.0)
    val Pf: vec4 = fract(P.xyxy) - vec4(0.0, 0.0, 1.0, 1.0)
    Pi = mod289Vec4(Pi)

    val ix: vec4 = Pi.xzxz
    val iy: vec4 = Pi.yyww
    val fx: vec4 = Pf.xzxz
    val fy: vec4 = Pf.yyww

    val i: vec4 = permuteVec4(permuteVec4(ix) + iy)

    var gx: vec4 = fract(i * (1.0f / 41.0f)) * 2.0f - 1.0f
    val gy: vec4 = abs(gx) - 0.5f
    val tx: vec4 = floor(gx + 0.5f)
    gx = gx - tx

    var g00: vec2 = vec2(gx.x, gy.x)
    var g10: vec2 = vec2(gx.y, gy.y)
    var g01: vec2 = vec2(gx.z, gy.z)
    var g11: vec2 = vec2(gx.w, gy.w)

    val norm: vec4 =
      taylorInvSqrt(vec4(dot(g00, g00), dot(g01, g01), dot(g10, g10), dot(g11, g11)))
    g00 *= norm.x
    g01 *= norm.y
    g10 *= norm.z
    g11 *= norm.w

    val n00: Float = dot(g00, vec2(fx.x, fy.x))
    val n10: Float = dot(g10, vec2(fx.y, fy.y))
    val n01: Float = dot(g01, vec2(fx.z, fy.z))
    val n11: Float = dot(g11, vec2(fx.w, fy.w))

    val fade_xy: vec2 = fade(Pf.xy)
    val n_x: vec2     = mix(vec2(n00, n01), vec2(n10, n11), fade_xy.x)
    val n_xy: Float   = mix(n_x.x, n_x.y, fade_xy.y)

    2.3f * n_xy

  // Gradient noise

  inline private def _hash(x: vec2): vec2 =
    val k = vec2(0.3183099f, 0.3678794f)
    val y = x * k + k.yx
    -1.0f + 2.0f * fract(16.0f * k * fract(y.x * y.y * (y.x + y.y)))

  inline def gradient(p: vec2): vec3 =
    val hash: vec2 => vec2 =
      p => _hash(p)

    val i: vec2  = floor(p)
    val f: vec2  = fract(p)
    val u: vec2  = f * f * (3.0f - 2.0f * f)
    val du: vec2 = 6.0f * f * (1.0f - f)
    val ga: vec2 = hash(i + vec2(0.0f, 0.0f))
    val gb: vec2 = hash(i + vec2(1.0f, 0.0f))
    val gc: vec2 = hash(i + vec2(0.0f, 1.0f))
    val gd: vec2 = hash(i + vec2(1.0f, 1.0f))

    val va: Float = dot(ga, f - vec2(0.0f, 0.0f))
    val vb: Float = dot(gb, f - vec2(1.0f, 0.0f))
    val vc: Float = dot(gc, f - vec2(0.0f, 1.0f))
    val vd: Float = dot(gd, f - vec2(1.0f, 1.0f))

    vec3(
      va + u.x * (vb - va) + u.y * (vc - va) + u.x * u.y * (va - vb - vc + vd),
      ga + u.x * (gb - ga) + u.y * (gc - ga) + u.x * u.y * (ga - gb - gc + gd) +
        du * (u.yx * (va - vb - vc + vd) + vec2(vb, vc) - va)
    )

  // Simplex noise

  inline def simplex(v: vec2): Float =
    val mod289Vec2: vec2 => vec2 = x => _mod289Vec2(x)
    val permute: vec3 => vec3    = x => _permute(x)

    val C: vec4 = vec4(
      0.211324865405187,
      0.366025403784439,
      -0.577350269189626,
      0.024390243902439
    )

    var i: vec2  = floor(v + dot(v, C.yy))
    val x0: vec2 = v - i + dot(i, C.xx)

    val i1: vec2  = if x0.x > x0.y then vec2(1.0, 0.0) else vec2(0.0, 1.0)
    var x12: vec4 = x0.xyxy + C.xxzz
    x12 = vec4(x12.xy - i1, x12.zw)

    i = mod289Vec2(i)
    val p: vec3 = permute(permute(i.y + vec3(0.0f, i1.y, 1.0f)) + i.x + vec3(0.0f, i1.x, 1.0f))

    var m: vec3 = max(0.5f - vec3(dot(x0, x0), dot(x12.xy, x12.xy), dot(x12.zw, x12.zw)), 0.0f)
    m = m * m
    m = m * m

    val x: vec3  = 2.0f * fract(p * C.www) - 1.0f
    val h: vec3  = abs(x) - 0.5f
    val ox: vec3 = floor(x + 0.5f)
    val a0: vec3 = x - ox

    m *= 1.79284291400159f - 0.85373472095314f * (a0 * a0 + h * h)

    val g: vec3 =
      vec3(
        a0.x * x0.x + h.x * x0.y,
        a0.yz * x12.xz + h.yz * x12.yw
      )

    130.0f * dot(m, g)

  // White noise
  inline def white(p: vec2): vec3 =
    var a: vec3 = fract(p.xyx * vec3(123.34f, 234.34f, 345.65f))
    a = a + dot(a, a + 34.45f)
    fract(vec3(a.x * a.y, a.y * a.z, a.z * a.x))
