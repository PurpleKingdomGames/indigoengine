//> using scala "3.2.1"
//> using lib "io.indigoengine::ultraviolet:0.1.0-SNAPSHOT"
//> using lib "com.lihaoyi::os-lib:0.8.1"

import ultraviolet.shadertoy.*
import ultraviolet.syntax.*
import os.*

object ShaderToy extends App:

  inline def image =
    Shader[ShaderToyEnv, Unit] { env =>
      @const val NUM_STEPS: Int      = 8
      @const val PI: Float           = 3.141592f
      @const val EPSILON: Float      = 1e-3f
      @define val EPSILON_NRM: Float = 0.1f / env.iResolution.x

      // sea
      @const val ITER_GEOMETRY: Int    = 3
      @const val ITER_FRAGMENT: Int    = 5
      @const val SEA_HEIGHT: Float     = 0.6f
      @const val SEA_CHOPPY: Float     = 4.0f
      @const val SEA_SPEED: Float      = 0.8f
      @const val SEA_FREQ: Float       = 0.16f
      @const val SEA_BASE: vec3        = vec3(0.0f, 0.09f, 0.18f)
      @const val SEA_WATER_COLOR: vec3 = vec3(0.8f, 0.9f, 0.6f) * 0.6f
      @define val SEA_TIME: Float      = 1.0f + env.iTime * SEA_SPEED
      @const val octave_m: mat2        = mat2(1.6f, 1.2f, -1.2f, 1.6f)

      // math
      def fromEuler(ang: vec3): mat3 =
        val a1: vec2 = vec2(sin(ang.x), cos(ang.x))
        val a2: vec2 = vec2(sin(ang.y), cos(ang.y))
        val a3: vec2 = vec2(sin(ang.z), cos(ang.z))
        val m: mat3  = null
        m(0) = vec3(a1.y * a3.y + a1.x * a2.x * a3.x, a1.y * a2.x * a3.x + a3.y * a1.x, -a2.y * a3.x)
        m(1) = vec3(-a2.y * a1.x, a1.y * a2.y, a2.x)
        m(2) = vec3(a3.y * a1.x * a2.x + a1.y * a3.x, a1.x * a3.x - a1.y * a3.y * a2.x, a2.y * a3.y)
        m

      def hash(p: vec2): Float =
        val h: Float = dot(p, vec2(127.1, 311.7))
        fract(sin(h) * 43758.5453123f)

      def noise(p: vec2): Float =
        val i: vec2 = floor(p)
        val f: vec2 = fract(p)
        val u: vec2 = f * f * (3.0f - 2.0f * f)

        -1.0f + 2.0f *
          mix(
            mix(hash(i + vec2(0.0f, 0.0f)), hash(i + vec2(1.0f, 0.0f)), u.x),
            mix(hash(i + vec2(0.0f, 1.0f)), hash(i + vec2(1.0f, 1.0f)), u.x),
            u.y
          )

      // lighting
      def diffuse(n: vec3, l: vec3, p: Float): Float =
        pow(dot(n, l) * 0.4f + 0.6f, p)

      def specular(n: vec3, l: vec3, e: vec3, s: Float): Float =
        val nrm: Float = (s + 8.0f) / (PI * 8.0f)
        pow(max(dot(reflect(e, n), l), 0.0f), s) * nrm

      // sky
      def getSkyColor(c: vec3): vec3 =
        val e = vec3(
          c.x,
          (max(c.y, 0.0f) * 0.8f + 0.2f) * 0.18f,
          c.z
        )
        vec3(pow(1.0f - e.y, 2.0f), 1.0f - e.y, 0.6f + (1.0f - e.y) * 0.4f) * 1.1f

      // sea
      def sea_octave(uv: vec2, choppy: Float): Float =
        val n         = noise(uv)
        val uv2       = vec2(uv.x + n, uv.y + n)
        var wv: vec2  = 1.0f - abs(sin(uv2))
        val swv: vec2 = abs(cos(uv2))
        wv = mix(wv, swv, wv)
        pow(1.0f - pow(wv.x * wv.y, 0.65f), choppy)

      def map(p: vec3): Float = {
        var freq: Float   = SEA_FREQ
        var amp: Float    = SEA_HEIGHT
        var choppy: Float = SEA_CHOPPY
        var uv: vec2      = p.xz
        uv = vec2(uv.x * 0.75f, uv.y)

        var d: Float = 0.0f
        var h        = 0.0f
        cfor(0, _ < ITER_GEOMETRY, _ + 1) { _ =>
          d = sea_octave((uv + SEA_TIME) * freq, choppy)
          d += sea_octave((uv - SEA_TIME) * freq, choppy)
          h += d * amp
          uv = uv * octave_m
          freq *= 1.9f
          amp *= 0.22f
          choppy = mix(choppy, 1.0f, 0.2f)
        }

        p.y - h
      }

      def getSeaColor(p: vec3, n: vec3, l: vec3, eye: vec3, dist: vec3): vec3 =
        var fresnel: Float = clamp(1.0f - dot(n, -eye), 0.0f, 1.0f)
        fresnel = pow(fresnel, 3.0f) * 0.5f

        val reflected: vec3 = getSkyColor(reflect(eye, n))
        val refracted: vec3 = SEA_BASE + diffuse(n, l, 80.0f) * SEA_WATER_COLOR * 0.12f

        var color: vec3 = mix(refracted, reflected, fresnel)

        val atten: Float = max(1.0f - dot(dist, dist) * 0.001f, 0.0f)
        color += SEA_WATER_COLOR * (p.y - SEA_HEIGHT) * 0.18f * atten

        color += vec3(specular(n, l, eye, 60.0f))

        color

      // tracing
      def getNormal(p: vec3, eps: Float): vec3 =
        var n: vec3 = null
        n = vec3(n.x, map(p), n.z)
        n = vec3(map(vec3(p.x + eps, p.y, p.z)) - n.y, n.y, n.z)
        n = vec3(n.x, n.y, map(vec3(p.x, p.y, p.z + eps)) - n.y)
        n = vec3(n.x, eps, n.z)
        normalize(n)

      var pOut: vec3 = vec3(0.0)
      def heightMapTracing(ori: vec3, dir: vec3): Float =
        var tm: Float = 0.0f
        var tx: Float = 1000.0f
        var hx: Float = map(ori + dir * tx)

        var res = 0.0f
        if (hx > 0.0f) {
          pOut = ori + dir * tx
          res = tx
        } else {
          var hm: Float   = map(ori + dir * tm)
          var tmid: Float = 0.0f

          cfor(0, _ < NUM_STEPS, _ + 1) { _ =>
            tmid = mix(tm, tx, hm / (hm - hx))
            pOut = ori + dir * tmid
            val hmid: Float = map(pOut)
            if (hmid < 0.0f) {
              tx = tmid
              hx = hmid
            } else {
              tm = tmid
              hm = hmid
            }
          }

          res = tmid
        }
        res

      def getPixel(coord: vec2, time: Float): vec3 = {
        var uv: vec2 = coord / env.iResolution.xy
        uv = uv * 2.0f - 1.0f
        uv = vec2(uv.x * (env.iResolution.x / env.iResolution.y), uv.y)

        // ray
        val ang: vec3 = vec3(sin(time * 3.0f) * 0.1f, sin(time) * 0.2f + 0.3f, time)
        val ori: vec3 = vec3(0.0f, 3.5f, time * 5.0f)
        var dir: vec3 = normalize(vec3(uv.xy, -2.0f))
        dir = vec3(dir.xy, dir.z + (length(uv) * 0.14f))
        dir = normalize(dir) * fromEuler(ang)

        // tracing
        heightMapTracing(ori, dir)
        val dist: vec3  = pOut - ori
        val n: vec3     = getNormal(pOut, dot(dist, dist) * EPSILON_NRM)
        val light: vec3 = normalize(vec3(0.0f, 1.0f, 0.8f))

        // color
        mix(
          getSkyColor(dir),
          getSeaColor(pOut, n, light, dir, dist),
          pow(smoothstep(0.0f, -0.02f, dir.y), 0.2f)
        )
      }

      // main
      def mainImage(fragColor: vec4, fragCoord: vec2): vec4 =
        val time: Float = env.iTime * 0.3f + env.iMouse.x * 0.01f

        val color: vec3 = getPixel(fragCoord, time)

        vec4(pow(color, vec3(0.65f)), 1.0f)
    }

  os.makeDir.all(os.pwd / "glsl")
  os.write.over(os.pwd / "glsl" / "seascape.frag", image.toGLSL[ShaderToy].toOutput.code)
