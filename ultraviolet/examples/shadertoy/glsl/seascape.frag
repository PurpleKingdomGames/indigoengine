const int NUM_STEPS=8;
const float PI=3.141592;
const float EPSILON=0.001;
#define EPSILON_NRM 0.1/iResolution.x
const int ITER_GEOMETRY=3;
const int ITER_FRAGMENT=5;
const float SEA_HEIGHT=0.6;
const float SEA_CHOPPY=4.0;
const float SEA_SPEED=0.8;
const float SEA_FREQ=0.16;
const vec3 SEA_BASE=vec3(0.0,0.09,0.18);
const vec3 SEA_WATER_COLOR=vec3(0.8,0.9,0.6)*0.6;
#define SEA_TIME 1.0+(iTime*SEA_SPEED)
const mat2 octave_m=mat2(1.6,1.2,-1.2,1.6);
mat3 fromEuler(in vec3 ang){
  vec2 a1=vec2(sin(ang.x),cos(ang.x));
  vec2 a2=vec2(sin(ang.y),cos(ang.y));
  vec2 a3=vec2(sin(ang.z),cos(ang.z));
  mat3 m;
  m[0]=(vec3((a1.y*a3.y)+((a1.x*a2.x)*a3.x),((a1.y*a2.x)*a3.x)+(a3.y*a1.x),(-a2.y)*a3.x));
  m[1]=(vec3((-a2.y)*a1.x,a1.y*a2.y,a2.x));
  m[2]=(vec3(((a3.y*a1.x)*a2.x)+(a1.y*a3.x),(a1.x*a3.x)-((a1.y*a3.y)*a2.x),a2.y*a3.y));
  return m;
}
float hash(in vec2 p){
  float h=dot(p,vec2(127.1,311.7));
  return fract(sin(h)*43758.547);
}
float noise(in vec2 p){
  vec2 i=floor(p);
  vec2 f=fract(p);
  vec2 u=(f*f)*(3.0-(2.0*f));
  return (-1.0)+(2.0*(mix(mix(hash(i+vec2(0.0,0.0)),hash(i+vec2(1.0,0.0)),u.x),mix(hash(i+vec2(0.0,1.0)),hash(i+vec2(1.0,1.0)),u.x),u.y)));
}
float diffuse(in vec3 n,in vec3 l,in float p){
  return pow((dot(n,l)*0.4)+0.6,p);
}
float specular(in vec3 n,in vec3 l,in vec3 e,in float s){
  float nrm=(s+8.0)/(PI*8.0);
  return pow(max(dot(reflect(e,n),l),0.0),s)*nrm;
}
vec3 getSkyColor(in vec3 c){
  vec3 e=vec3(c.x,((max(c.y,0.0)*0.8)+0.2)*0.18,c.z);
  return (vec3(pow(1.0-e.y,2.0),1.0-e.y,0.6+((1.0-e.y)*0.4)))*1.1;
}
float sea_octave(in vec2 uv,in float choppy){
  float n=noise(uv);
  vec2 uv2=vec2(uv.x+n,uv.y+n);
  vec2 wv=1.0-abs(sin(uv2));
  vec2 swv=abs(cos(uv2));
  wv=mix(wv,swv,wv);
  return pow(1.0-(pow(wv.x*wv.y,0.65)),choppy);
}
float map(in vec3 p){
  float freq=SEA_FREQ;
  float amp=SEA_HEIGHT;
  float choppy=SEA_CHOPPY;
  vec2 uv=p.xz;
  uv=vec2(uv.x*0.75,uv.y);
  float d=0.0;
  float h=0.0;
  for(int val0=0;val0<ITER_GEOMETRY;val0=val0+1){
    d=sea_octave((uv+SEA_TIME)*freq,choppy);
    d=d+(sea_octave((uv-SEA_TIME)*freq,choppy));
    h=h+(d*amp);
    uv=uv*octave_m;
    freq=freq*1.9;
    amp=amp*0.22;
    choppy=mix(choppy,1.0,0.2);
  }
  return p.y-h;
}
vec3 getSeaColor(in vec3 p,in vec3 n,in vec3 l,in vec3 eye,in vec3 dist){
  float fresnel=clamp(1.0-(dot(n,-eye)),0.0,1.0);
  fresnel=pow(fresnel,3.0)*0.5;
  vec3 reflected=getSkyColor(reflect(eye,n));
  vec3 refracted=SEA_BASE+((diffuse(n,l,80.0)*SEA_WATER_COLOR)*0.12);
  vec3 color=mix(refracted,reflected,fresnel);
  float atten=max(1.0-(dot(dist,dist)*0.001),0.0);
  color=color+(((SEA_WATER_COLOR*(p.y-SEA_HEIGHT))*0.18)*atten);
  color=color+vec3(specular(n,l,eye,60.0));
  return color;
}
vec3 getNormal(in vec3 p,in float eps){
  vec3 n;
  n=vec3(n.x,map(p),n.z);
  n=vec3((map(vec3(p.x+eps,p.y,p.z)))-n.y,n.y,n.z);
  n=vec3(n.x,n.y,(map(vec3(p.x,p.y,p.z+eps)))-n.y);
  n=vec3(n.x,eps,n.z);
  return normalize(n);
}
vec3 pOut=vec3(0.0);
float heightMapTracing(in vec3 ori,in vec3 dir){
  float tm=0.0;
  float tx=1000.0;
  float hx=map(ori+(dir*tx));
  float res=0.0;
  if(hx>0.0){
    pOut=ori+(dir*tx);
    res=tx;
  }else{
    float hm=map(ori+(dir*tm));
    float tmid=0.0;
    for(int val1=0;val1<NUM_STEPS;val1=val1+1){
      tmid=mix(tm,tx,hm/(hm-hx));
      pOut=ori+(dir*tmid);
      float hmid=map(pOut);
      if(hmid<0.0){
        tx=tmid;
        hx=hmid;
      }else{
        tm=tmid;
        hm=hmid;
      }
    }
    res=tmid;
  }
  return res;
}
vec3 getPixel(in vec2 coord,in float time){
  vec2 uv=coord/iResolution.xy;
  uv=(uv*2.0)-1.0;
  uv=vec2(uv.x*(iResolution.x/iResolution.y),uv.y);
  vec3 ang=vec3((sin(time*3.0))*0.1,(sin(time)*0.2)+0.3,time);
  vec3 ori=vec3(0.0,3.5,time*5.0);
  vec3 dir=normalize(vec3(uv.xy,-2.0));
  dir=vec3(dir.xy,dir.z+(length(uv)*0.14));
  dir=normalize(dir)*fromEuler(ang);
  heightMapTracing(ori,dir);
  vec3 dist=pOut-ori;
  vec3 n=getNormal(pOut,dot(dist,dist)*EPSILON_NRM);
  vec3 light=normalize(vec3(0.0,1.0,0.8));
  return mix(getSkyColor(dir),getSeaColor(pOut,n,light,dir,dist),pow(smoothstep(0.0,-0.02,dir.y),0.2));
}
void mainImage(out vec4 fragColor,in vec2 fragCoord){
  float time=(iTime*0.3)+(iMouse.x*0.01);
  vec3 color=getPixel(fragCoord,time);
  fragColor=vec4(pow(color,vec3(0.65)),1.0);
}