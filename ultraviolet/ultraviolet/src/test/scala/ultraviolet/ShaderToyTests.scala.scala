package ultraviolet

import ultraviolet.syntax.*

class ShaderToyTests extends munit.FunSuite {

  test("Able to fully define the default shadertoy example") {

    inline def fragment =
      Shader {
        import ultraviolet.predef.shadertoy.*

        // Normalized pixel coordinates (from 0 to 1)
        val uv: vec2 = fragCoord / iResolution.xy

        // Time varying pixel color
        val col: vec3 = 0.5f + 0.5f * cos(iTime + uv.xyx + vec3(0.0f, 2.0f, 4.0f))

        // Output to screen
        fragColor = vec4(col, 1.0f)
      }

    val actual =
      fragment.toGLSL

    // DebugAST.toAST(fragment)
    // println(actual)

    val expected: String =
      """
      |void mainImage(out vec4 fragColor, in vec2 fragCoord){
      |  vec2 uv=(fragCoord)/(iResolution.xy);vec3 col=(0.5)+((0.5)*(cos(((iTime)+(uv.xyx))+(vec3(0.0,2.0,4.0)))));fragColor=vec4(col,1.0);
      |}
      |""".stripMargin.trim

    assertEquals(actual, expected)

  }

}

/*
uniform vec3      iResolution;           // viewport resolution (in pixels)
uniform float     iTime;                 // shader playback time (in seconds)
uniform float     iTimeDelta;            // render time (in seconds)
uniform float     iFrameRate;            // shader frame rate
uniform int       iFrame;                // shader playback frame
uniform float     iChannelTime[4];       // channel playback time (in seconds)
uniform vec3      iChannelResolution[4]; // channel resolution (in pixels)
uniform vec4      iMouse;                // mouse pixel coords. xy: current (if MLB down), zw: click
uniform samplerXX iChannel0..3;          // input channel. XX = 2D/Cube
uniform vec4      iDate;                 // (year, month, day, time in seconds)
uniform float     iSampleRate;           // sound sample rate (i.e., 44100)

---​

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = fragCoord/iResolution.xy;

    // Time varying pixel color
    vec3 col = 0.5 + 0.5*cos(iTime+uv.xyx+vec3(0,2,4));

    // Output to screen
    fragColor = vec4(col,1.0);
}

---

const int TRACTING_NUM = 100;
const int SEARCH_DEPTH = 28;
const float nearPlane = .001;
const float farPlane = 1000.0;

const vec3 lightPos = vec3(2,2,4);
const vec3 cameraPos = vec3(0,.5,3);

#define PI 3.1415926535

#define MAT_LAMBERTIAN 0
#define MAT_METALLIC 1
#define MAT_DIELECTRIC 2

uint m_u = uint(521288629);
uint m_v = uint(362436069);
uint GetUintCore(inout uint u, inout uint v){
	v = uint(36969) * (v & uint(65535)) + (v >> 16);
	u = uint(18000) * (u & uint(65535)) + (u >> 16);
	return (v << 16) + u;
}
float GetUniformCore(inout uint u, inout uint v){
	uint z = GetUintCore(u, v);

	return float(z) / float(uint(4294967295));
}
float GetUniform(){
	return GetUniformCore(m_u, m_v);
}
float rand(){
	return GetUniform();
}

mat3 rotateY(float theta) {
    float c = cos(theta);
    float s = sin(theta);
    return mat3(
        vec3(c, 0, s),
        vec3(0, 1, 0),
        vec3(-s, 0, c)
    );
}

vec3 random_in_unit_sphere(){
	vec3 p;

	float theta = rand() * 2.0 * PI;
	float phi   = rand() * PI;
	p.y = cos(phi);
	p.x = sin(phi) * cos(theta);
	p.z = sin(phi) * sin(theta);

	return p;
}

bool irefract(vec3 v, vec3 n, float ni_over_nt, out vec3 refracted){
	vec3 uv = normalize(v);
	float dt = dot(uv, n);
	float discriminant = 1.0 - ni_over_nt * ni_over_nt * (1.0 - dt * dt);
	if (discriminant > 0.0){
		refracted = ni_over_nt * (uv - n * dt) - n * sqrt(discriminant);
		return true;
	}
	return false;
}

float schlick(float cosine, float ior){
	float r0 = (1.- ior) / (1.+ ior);
	r0 = r0 * r0;
	return r0 + (1.- r0) * pow((1.- cosine), 5.);
}

vec3 getbackground(vec3 pos){
    return texture(iChannel0,pos).rgb;
}

struct Lambertian{
	vec3 albedo;
};

Lambertian lambertians[4];

Lambertian createLambertian(vec3 color){
    Lambertian lambertian;

    lambertian.albedo = color;

    return lambertian;
}

struct Metallic{
    vec3 albedo;
    float roughness;
};

Metallic metallics[4];

Metallic createMetallic(vec3 color,float rough){
    Metallic metallic;

    metallic.albedo = color;
    metallic.roughness = rough;

    return metallic;
}

struct Dielectric{
    vec3 albedo;
    float rate;
};

Dielectric dielectrics[4];

Dielectric createDielectric(vec3 color,float rate){
    Dielectric dielectric;

    dielectric.albedo = color;
    dielectric.rate = rate;

    return dielectric;
}

struct Ray{
    vec3 origin;
    vec3 direction;
};

struct Sphere{
    float radius;
    vec3 position;

    int materialPtr;
    int materialType;
};

struct World{
    int objnum;
    Sphere sps[10];
};

struct HitRecord{
    float t;
    vec3 position;
    vec3 normal;

    int materialPtr;
    int materialType;
};

Sphere createSphere(vec3 pos,float r,int ptr,int type){
    Sphere sphere;
    sphere.position = pos;
    sphere.radius = r;

    sphere.materialPtr = ptr;
    sphere.materialType = type;
    return sphere;
}

void hitlambertian(Ray ray,HitRecord hit,Lambertian lambertian,out Ray scatray,out vec3 color){
    color = lambertian.albedo;

    scatray.origin = hit.position;
    scatray.direction = hit.normal + random_in_unit_sphere();
}

void hitmetallic(Ray ray,HitRecord hit,Metallic metallic,out Ray scatray,out vec3 color){
    color = metallic.albedo;

    scatray.origin = hit.position;
    scatray.direction = reflect(ray.direction,hit.normal) + metallic.roughness * random_in_unit_sphere();
}

void hitdielectric(Ray ray,HitRecord hit,Dielectric dielectric,out Ray scatray,out vec3 color){
    color = dielectric.albedo;

    vec3 outward_normal;
	float ni_over_nt;
	float cosine;
	if(dot(ray.direction, hit.normal) > 0.0){
		outward_normal = -hit.normal;
		ni_over_nt = dielectric.rate;
		cosine = dot(ray.direction, hit.normal) / length(ray.direction);
	}
	else{
		outward_normal = hit.normal;
		ni_over_nt = 1.0 / dielectric.rate;
		cosine = -dot(ray.direction, hit.normal) / length(ray.direction);
	}

    float reflect_prob;
	vec3 refracted;
	if(irefract(ray.direction, outward_normal, ni_over_nt, refracted)){
		reflect_prob = schlick(cosine, dielectric.rate);
	}
	else{
		reflect_prob = 1.0;
	}

	if(rand() < reflect_prob){
		scatray = Ray(hit.position,refracted);
	}
	else{
		scatray = Ray(hit.position,refracted);
	}
}

bool HitSphere(Ray ray,float near,float far,Sphere sp,inout HitRecord hit){
    vec3 oc = ray.origin - sp.position;

	float a = dot(ray.direction, ray.direction);
	float b = 2.0 * dot(oc, ray.direction);
	float c = dot(oc, oc) - sp.radius * sp.radius;

	float delta = b * b - 4. * a * c;

    if(delta > 0.){
        float temp = (-b - sqrt(delta)) / (2.0 * a);  // 最近的点
        if(near < temp && temp < far){
            hit.t = temp;
            hit.position = ray.origin + temp * ray.direction;
            hit.normal = normalize((hit.position - sp.position) / sp.radius);

            hit.materialPtr = sp.materialPtr;
            hit.materialType = sp.materialType;

            return true;
        }

        temp = (-b + sqrt(delta)) / (2.0 * a);  // 最远的点
        if(near < temp && temp < far){
            hit.t = temp;
            hit.position = ray.origin + temp * ray.direction;
            hit.normal = normalize((hit.position - sp.position) / sp.radius);

            hit.materialPtr = sp.materialPtr;
            hit.materialType = sp.materialType;

            return true;
        }
    }

    return false;
}

World getScene(){
    World world;
    world.objnum = 4;

    lambertians[0] = createLambertian(vec3(0.1, 0.7, 0.7));
    lambertians[1] = createLambertian(vec3(0.5, 0.5, 0.5));

    metallics[0] = createMetallic(vec3(0.8, 0.8, 0.8), 0.3);

    dielectrics[0] = createDielectric(vec3(1.0, 1.0, 1.0), 1.5);

    world.sps[0] = createSphere(vec3(0.0, 0.0, -1.0), 0.5,0,MAT_LAMBERTIAN);
    world.sps[1] = createSphere(vec3(0.0, -100.5, -1.0), 100.0,1,MAT_LAMBERTIAN);
    world.sps[2] = createSphere(vec3(1.0, 0.0, -1.0), 0.5,0,MAT_METALLIC);
    world.sps[3] = createSphere(vec3(-1.0, 0.0, -1.0), 0.5,0,MAT_DIELECTRIC);

    return world;
}


bool HitWorld(World world,Ray ray,float near,float far,inout HitRecord hit){
    HitRecord temphit;
    bool hit_anything = false;
    float hit_t_max = far;

    for(int i=0;i<world.objnum;i++){
        if(HitSphere(ray,near,hit_t_max,world.sps[i],temphit)){
            hit = temphit;
            hit_anything = true;
            hit_t_max = hit.t;
        }
    }

    return hit_anything;
}

vec3 raymarch(Ray ray,float near,float far,World world,int iter){
    HitRecord record;

    vec3 bgcolor = vec3(1);
    vec3 sumcolor = vec3(1);

    while(iter > 0){
        iter--;
        if(HitWorld(world,ray,near,far,record)){
            Ray scatterray;
            vec3 attenuation;
            if(record.materialType == MAT_LAMBERTIAN){
                hitlambertian(ray,record,lambertians[record.materialPtr],scatterray,attenuation);
            }else if(record.materialType == MAT_METALLIC){
                hitmetallic(ray,record,metallics[record.materialPtr],scatterray,attenuation);
            }else if(record.materialType == MAT_DIELECTRIC){
                hitdielectric(ray,record,dielectrics[record.materialPtr],scatterray,attenuation);
            }

            ray = scatterray;
            sumcolor *= attenuation;
        }else{
            bgcolor = getbackground(ray.direction);
            break;
        }
    }

    return sumcolor * bgcolor;
}

vec3 GammaCorrection(vec3 c){
	return pow(c, vec3(1.0 / 2.2));
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    World world = getScene();

    // Time varying pixel color
    mat3 rotaY = rotateY(iTime);

    vec3 col = vec3(0.);
    for(int i=0;i<TRACTING_NUM;i++){
        vec2 uv = ((fragCoord + vec2(rand(),rand())) - .5 * iResolution.xy) / iResolution.y;

        Ray ray;
        ray.origin = cameraPos * rotaY;
        ray.direction = vec3(uv,-1) * rotaY;

        col+=raymarch(ray,nearPlane,farPlane,world,SEARCH_DEPTH);
    }
    col /= float(TRACTING_NUM);

    col = GammaCorrection(col);

    // Output to screen
    fragColor = vec4(col,1.0);
}
 */
