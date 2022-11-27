package ultraviolet.shadertoyexamples

import ultraviolet.shadertoy.*
import ultraviolet.syntax.*

@SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
object Seascape:

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

  val imageShader = image.toGLSL[ShaderToy].code

  val imageExpected: String =
    """
    |const int NUM_STEPS=8;
    |const float PI=3.141592025756836;
    |const float EPSILON=0.0010000000474974513;
    |#define EPSILON_NRM 0.10000000149011612/iResolution.x
    |const int ITER_GEOMETRY=3;
    |const int ITER_FRAGMENT=5;
    |const float SEA_HEIGHT=0.6000000238418579;
    |const float SEA_CHOPPY=4.0;
    |const float SEA_SPEED=0.800000011920929;
    |const float SEA_FREQ=0.1599999964237213;
    |const vec3 SEA_BASE=vec3(0.0,0.09000000357627869,0.18000000715255737);
    |const vec3 SEA_WATER_COLOR=vec3(0.800000011920929,0.8999999761581421,0.6000000238418579)*0.6000000238418579;
    |#define SEA_TIME 1.0+(iTime*SEA_SPEED)
    |const mat2 octave_m=mat2(1.600000023841858,1.2000000476837158,-1.2000000476837158,1.600000023841858);
    |mat3 fromEuler(in vec3 ang){
    |  vec2 a1=vec2(sin(ang.x),cos(ang.x));
    |  vec2 a2=vec2(sin(ang.y),cos(ang.y));
    |  vec2 a3=vec2(sin(ang.z),cos(ang.z));
    |  mat3 m;
    |  m[0]=(vec3((a1.y*a3.y)+((a1.x*a2.x)*a3.x),((a1.y*a2.x)*a3.x)+(a3.y*a1.x),(-a2.y)*a3.x));
    |  m[1]=(vec3((-a2.y)*a1.x,a1.y*a2.y,a2.x));
    |  m[2]=(vec3(((a3.y*a1.x)*a2.x)+(a1.y*a3.x),(a1.x*a3.x)-((a1.y*a3.y)*a2.x),a2.y*a3.y));
    |  return m;
    |}
    |float hash(in vec2 p){
    |  float h=dot(p,vec2(127.0999984741211,311.70001220703125));
    |  return fract(sin(h)*43758.546875);
    |}
    |float noise(in vec2 p){
    |  vec2 i=floor(p);
    |  vec2 f=fract(p);
    |  vec2 u=(f*f)*(3.0-(2.0*f));
    |  return (-1.0)+(2.0*(mix(mix(hash(i+vec2(0.0,0.0)),hash(i+vec2(1.0,0.0)),u.x),mix(hash(i+vec2(0.0,1.0)),hash(i+vec2(1.0,1.0)),u.x),u.y)));
    |}
    |float diffuse(in vec3 n,in vec3 l,in float p){
    |  return pow((dot(n,l)*0.4000000059604645)+0.6000000238418579,p);
    |}
    |float specular(in vec3 n,in vec3 l,in vec3 e,in float s){
    |  float nrm=(s+8.0)/(PI*8.0);
    |  return pow(max(dot(reflect(e,n),l),0.0),s)*nrm;
    |}
    |vec3 getSkyColor(in vec3 c){
    |  vec3 e=vec3(c.x,((max(c.y,0.0)*0.800000011920929)+0.20000000298023224)*0.18000000715255737,c.z);
    |  return (vec3(pow(1.0-e.y,2.0),1.0-e.y,0.6000000238418579+((1.0-e.y)*0.4000000059604645)))*1.100000023841858;
    |}
    |float sea_octave(in vec2 uv,in float choppy){
    |  float n=noise(uv);
    |  vec2 uv2=vec2(uv.x+n,uv.y+n);
    |  vec2 wv=1.0-abs(sin(uv2));
    |  vec2 swv=abs(cos(uv2));
    |  wv=mix(wv,swv,wv);
    |  return pow(1.0-(pow(wv.x*wv.y,0.6499999761581421)),choppy);
    |}
    |float map(in vec3 p){
    |  float freq=SEA_FREQ;
    |  float amp=SEA_HEIGHT;
    |  float choppy=SEA_CHOPPY;
    |  vec2 uv=p.xz;
    |  uv=vec2(uv.x*0.75,uv.y);
    |  float d=0.0;
    |  float h=0.0;
    |  for(int val0=0;val0<ITER_GEOMETRY;val0=val0+1){
    |    d=sea_octave((uv+SEA_TIME)*freq,choppy);
    |    d=d+(sea_octave((uv-SEA_TIME)*freq,choppy));
    |    h=h+(d*amp);
    |    uv=uv*octave_m;
    |    freq=freq*1.899999976158142;
    |    amp=amp*0.2199999988079071;
    |    choppy=mix(choppy,1.0,0.20000000298023224);
    |  }
    |  return p.y-h;
    |}
    |vec3 getSeaColor(in vec3 p,in vec3 n,in vec3 l,in vec3 eye,in vec3 dist){
    |  float fresnel=clamp(1.0-(dot(n,-eye)),0.0,1.0);
    |  fresnel=pow(fresnel,3.0)*0.5;
    |  vec3 reflected=getSkyColor(reflect(eye,n));
    |  vec3 refracted=SEA_BASE+((diffuse(n,l,80.0)*SEA_WATER_COLOR)*0.11999999731779099);
    |  vec3 color=mix(refracted,reflected,fresnel);
    |  float atten=max(1.0-(dot(dist,dist)*0.0010000000474974513),0.0);
    |  color=color+(((SEA_WATER_COLOR*(p.y-SEA_HEIGHT))*0.18000000715255737)*atten);
    |  color=color+vec3(specular(n,l,eye,60.0));
    |  return color;
    |}
    |vec3 getNormal(in vec3 p,in float eps){
    |  vec3 n;
    |  n=vec3(n.x,map(p),n.z);
    |  n=vec3((map(vec3(p.x+eps,p.y,p.z)))-n.y,n.y,n.z);
    |  n=vec3(n.x,n.y,(map(vec3(p.x,p.y,p.z+eps)))-n.y);
    |  n=vec3(n.x,eps,n.z);
    |  return normalize(n);
    |}
    |vec3 pOut=vec3(0.0);
    |float heightMapTracing(in vec3 ori,in vec3 dir){
    |  float tm=0.0;
    |  float tx=1000.0;
    |  float hx=map(ori+(dir*tx));
    |  float res=0.0;
    |  if(hx>0.0){
    |    pOut=ori+(dir*tx);
    |    res=tx;
    |  }else{
    |    float hm=map(ori+(dir*tm));
    |    float tmid=0.0;
    |    for(int val1=0;val1<NUM_STEPS;val1=val1+1){
    |      tmid=mix(tm,tx,hm/(hm-hx));
    |      pOut=ori+(dir*tmid);
    |      float hmid=map(pOut);
    |      if(hmid<0.0){
    |        tx=tmid;
    |        hx=hmid;
    |      }else{
    |        tm=tmid;
    |        hm=hmid;
    |      }
    |    }
    |    res=tmid;
    |  }
    |  return res;
    |}
    |vec3 getPixel(in vec2 coord,in float time){
    |  vec2 uv=coord/iResolution.xy;
    |  uv=(uv*2.0)-1.0;
    |  uv=vec2(uv.x*(iResolution.x/iResolution.y),uv.y);
    |  vec3 ang=vec3((sin(time*3.0))*0.10000000149011612,(sin(time)*0.20000000298023224)+0.30000001192092896,time);
    |  vec3 ori=vec3(0.0,3.5,time*5.0);
    |  vec3 dir=normalize(vec3(uv.xy,-2.0));
    |  dir=vec3(dir.xy,dir.z+(length(uv)*0.14000000059604645));
    |  dir=normalize(dir)*fromEuler(ang);
    |  heightMapTracing(ori,dir);
    |  vec3 dist=pOut-ori;
    |  vec3 n=getNormal(pOut,dot(dist,dist)*EPSILON_NRM);
    |  vec3 light=normalize(vec3(0.0,1.0,0.800000011920929));
    |  return mix(getSkyColor(dir),getSeaColor(pOut,n,light,dir,dist),pow(smoothstep(0.0,-0.019999999552965164,dir.y),0.20000000298023224));
    |}
    |void mainImage(out vec4 fragColor,in vec2 fragCoord){
    |  float time=(iTime*0.30000001192092896)+(iMouse.x*0.009999999776482582);
    |  vec3 color=getPixel(fragCoord,time);
    |  fragColor=vec4(pow(color,vec3(0.6499999761581421)),1.0);
    |}
    |""".stripMargin.trim

end Seascape

/*
Original - https://www.shadertoy.com/view/Ms2SD1

/*
 * "Seascape" by Alexander Alekseev aka TDM - 2014
 * License Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * Contact: tdmaav@gmail.com
 */

const int NUM_STEPS = 8
const float PI	 	= 3.141592
const float EPSILON	= 1e-3
#define EPSILON_NRM (0.1 / iResolution.x)
#define AA

// sea
const int ITER_GEOMETRY = 3
const int ITER_FRAGMENT = 5
const float SEA_HEIGHT = 0.6
const float SEA_CHOPPY = 4.0
const float SEA_SPEED = 0.8
const float SEA_FREQ = 0.16
const vec3 SEA_BASE = vec3(0.0,0.09,0.18)
const vec3 SEA_WATER_COLOR = vec3(0.8,0.9,0.6)*0.6
#define SEA_TIME (1.0 + iTime * SEA_SPEED)
const mat2 octave_m = mat2(1.6,1.2,-1.2,1.6)

// math
mat3 fromEuler(vec3 ang) {
	vec2 a1 = vec2(sin(ang.x),cos(ang.x))
    vec2 a2 = vec2(sin(ang.y),cos(ang.y))
    vec2 a3 = vec2(sin(ang.z),cos(ang.z))
    mat3 m
    m[0] = vec3(a1.y*a3.y+a1.x*a2.x*a3.x,a1.y*a2.x*a3.x+a3.y*a1.x,-a2.y*a3.x)
	m[1] = vec3(-a2.y*a1.x,a1.y*a2.y,a2.x)
	m[2] = vec3(a3.y*a1.x*a2.x+a1.y*a3.x,a1.x*a3.x-a1.y*a3.y*a2.x,a2.y*a3.y)
	return m
}
float hash( vec2 p ) {
	float h = dot(p,vec2(127.1,311.7))
    return fract(sin(h)*43758.5453123)
}
float noise( in vec2 p ) {
    vec2 i = floor( p )
    vec2 f = fract( p )
	vec2 u = f*f*(3.0-2.0*f)
    return -1.0+2.0*mix( mix( hash( i + vec2(0.0,0.0) ),
                     hash( i + vec2(1.0,0.0) ), u.x),
                mix( hash( i + vec2(0.0,1.0) ),
                     hash( i + vec2(1.0,1.0) ), u.x), u.y)
}

// lighting
float diffuse(vec3 n,vec3 l,float p) {
    return pow(dot(n,l) * 0.4 + 0.6,p)
}
float specular(vec3 n,vec3 l,vec3 e,float s) {
    float nrm = (s + 8.0) / (PI * 8.0)
    return pow(max(dot(reflect(e,n),l),0.0),s) * nrm
}

// sky
vec3 getSkyColor(vec3 e) {
    e.y = (max(e.y,0.0)*0.8+0.2)*0.8
    return vec3(pow(1.0-e.y,2.0), 1.0-e.y, 0.6+(1.0-e.y)*0.4) * 1.1
}

// sea
float sea_octave(vec2 uv, float choppy) {
    uv += noise(uv)
    vec2 wv = 1.0-abs(sin(uv))
    vec2 swv = abs(cos(uv))
    wv = mix(wv,swv,wv)
    return pow(1.0-pow(wv.x * wv.y,0.65),choppy)
}

float map(vec3 p) {
    float freq = SEA_FREQ
    float amp = SEA_HEIGHT
    float choppy = SEA_CHOPPY
    vec2 uv = p.xz uv.x *= 0.75

    float d, h = 0.0
    for(int i = 0 i < ITER_GEOMETRY i++) {
    	d = sea_octave((uv+SEA_TIME)*freq,choppy)
    	d += sea_octave((uv-SEA_TIME)*freq,choppy)
        h += d * amp
    	uv *= octave_m freq *= 1.9 amp *= 0.22
        choppy = mix(choppy,1.0,0.2)
    }
    return p.y - h
}

float map_detailed(vec3 p) {
    float freq = SEA_FREQ
    float amp = SEA_HEIGHT
    float choppy = SEA_CHOPPY
    vec2 uv = p.xz uv.x *= 0.75

    float d, h = 0.0
    for(int i = 0 i < ITER_FRAGMENT i++) {
    	d = sea_octave((uv+SEA_TIME)*freq,choppy)
    	d += sea_octave((uv-SEA_TIME)*freq,choppy)
        h += d * amp
    	uv *= octave_m freq *= 1.9 amp *= 0.22
        choppy = mix(choppy,1.0,0.2)
    }
    return p.y - h
}

vec3 getSeaColor(vec3 p, vec3 n, vec3 l, vec3 eye, vec3 dist) {
    float fresnel = clamp(1.0 - dot(n,-eye), 0.0, 1.0)
    fresnel = pow(fresnel,3.0) * 0.5

    vec3 reflected = getSkyColor(reflect(eye,n))
    vec3 refracted = SEA_BASE + diffuse(n,l,80.0) * SEA_WATER_COLOR * 0.12

    vec3 color = mix(refracted,reflected,fresnel)

    float atten = max(1.0 - dot(dist,dist) * 0.001, 0.0)
    color += SEA_WATER_COLOR * (p.y - SEA_HEIGHT) * 0.18 * atten

    color += vec3(specular(n,l,eye,60.0))

    return color
}

// tracing
vec3 getNormal(vec3 p, float eps) {
    vec3 n
    n.y = map_detailed(p)
    n.x = map_detailed(vec3(p.x+eps,p.y,p.z)) - n.y
    n.z = map_detailed(vec3(p.x,p.y,p.z+eps)) - n.y
    n.y = eps
    return normalize(n)
}

float heightMapTracing(vec3 ori, vec3 dir, out vec3 p) {
    float tm = 0.0
    float tx = 1000.0
    float hx = map(ori + dir * tx)
    if(hx > 0.0) {
        p = ori + dir * tx
        return tx
    }
    float hm = map(ori + dir * tm)
    float tmid = 0.0
    for(int i = 0 i < NUM_STEPS i++) {
        tmid = mix(tm,tx, hm/(hm-hx))
        p = ori + dir * tmid
    	float hmid = map(p)
		if(hmid < 0.0) {
        	tx = tmid
            hx = hmid
        } else {
            tm = tmid
            hm = hmid
        }
    }
    return tmid
}

vec3 getPixel(in vec2 coord, float time) {
    vec2 uv = coord / iResolution.xy
    uv = uv * 2.0 - 1.0
    uv.x *= iResolution.x / iResolution.y

    // ray
    vec3 ang = vec3(sin(time*3.0)*0.1,sin(time)*0.2+0.3,time)
    vec3 ori = vec3(0.0,3.5,time*5.0)
    vec3 dir = normalize(vec3(uv.xy,-2.0)) dir.z += length(uv) * 0.14
    dir = normalize(dir) * fromEuler(ang)

    // tracing
    vec3 p
    heightMapTracing(ori,dir,p)
    vec3 dist = p - ori
    vec3 n = getNormal(p, dot(dist,dist) * EPSILON_NRM)
    vec3 light = normalize(vec3(0.0,1.0,0.8))

    // color
    return mix(
        getSkyColor(dir),
        getSeaColor(p,n,light,dir,dist),
    	pow(smoothstep(0.0,-0.02,dir.y),0.2))
}

// main
void mainImage( out vec4 fragColor, in vec2 fragCoord ) {
    float time = iTime * 0.3 + iMouse.x*0.01

#ifdef AA
    vec3 color = vec3(0.0)
    for(int i = -1 i <= 1 i++) {
        for(int j = -1 j <= 1 j++) {
        	vec2 uv = fragCoord+vec2(i,j)/3.0
    		color += getPixel(uv, time)
        }
    }
    color /= 9.0
#else
    vec3 color = getPixel(fragCoord, time)
#endif

    // post
	fragColor = vec4(pow(color,vec3(0.65)), 1.0)
}
 */
