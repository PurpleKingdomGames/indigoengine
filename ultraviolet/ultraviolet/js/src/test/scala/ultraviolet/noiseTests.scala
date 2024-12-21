package ultraviolet

import ultraviolet.syntax.*

// These tests are run as JS only due to differences between JVM and JS floating point precision
class noiseTests extends munit.FunSuite {

  test("Cellular noise") {
    inline def fragment: Shader[Unit, vec2] =
      Shader { _ =>

        import ultraviolet.noise.*

        def proxy: vec2 => vec2 = p => cellular(p)

        proxy(vec2(0.0f))
      }

    val actual =
      fragment.toGLSL[WebGL2](false).toOutput.code

    // println(DebugAST.toAST(fragment))
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec2 def1(in vec2 x){
      |  return x-((floor(x*0.0034602077212184668))*289.0);
      |}
      |vec3 def2(in vec3 x){
      |  return x-((floor(x*0.1428571492433548))*7.0);
      |}
      |vec3 def4(in vec3 value){
      |  return value-((floor(value*0.0034602077212184668))*289.0);
      |}
      |vec3 def3(in vec3 x){
      |  return def4(((34.0*x)+10.0)*x);
      |}
      |vec2 def0(in vec2 p){
      |  float K=0.1428571492433548;
      |  float Ko=0.4285714328289032;
      |  float jitter=1.0;
      |  vec2 Pi=def1(floor(p));
      |  vec2 Pf=fract(p);
      |  vec3 oi=vec3(-1.0,0.0,1.0);
      |  vec3 of=vec3(-0.5,0.5,1.5);
      |  vec3 px=def3(Pi.x+oi);
      |  vec3 p=def3((px.x+Pi.y)+oi);
      |  vec3 ox=(fract(p*K))-Ko;
      |  vec3 oy=((def2(floor(p*K)))*K)-Ko;
      |  vec3 dx=(Pf.x+0.5)+(jitter*ox);
      |  vec3 dy=(Pf.y-of)+(jitter*oy);
      |  vec3 d1=(dx*dx)+(dy*dy);
      |  p=def3((px.y+Pi.y)+oi);
      |  ox=(fract(p*K))-Ko;
      |  oy=((def2(floor(p*K)))*K)-Ko;
      |  dx=(Pf.x-0.5)+(jitter*ox);
      |  dy=(Pf.y-of)+(jitter*oy);
      |  vec3 d2=(dx*dx)+(dy*dy);
      |  p=def3((px.z+Pi.y)+oi);
      |  ox=(fract(p*K))-Ko;
      |  oy=((def2(floor(p*K)))*K)-Ko;
      |  dx=(Pf.x-1.5)+(jitter*ox);
      |  dy=(Pf.y-of)+(jitter*oy);
      |  vec3 d3=(dx*dx)+(dy*dy);
      |  vec3 d1a=min(d1,d2);
      |  d2=max(d1,d2);
      |  d2=min(d2,d3);
      |  d1=min(d1a,d2);
      |  d2=max(d1a,d2);
      |  vec3 d1Flip1;
      |  if(d1.x<d1.y){
      |    d1Flip1=d1;
      |  }else{
      |    d1Flip1=vec3(d1.y,d1.x,d1.z);
      |  }
      |  vec3 d1Flip2;
      |  if(d1Flip1.x<d1Flip1.z){
      |    d1Flip2=d1Flip1;
      |  }else{
      |    d1Flip2=vec3(d1Flip1.z,d1Flip1.y,d1Flip1.x);
      |  }
      |  vec3 d1Flip3=vec3(d1Flip2.x,min(d1Flip2.yz,d2.yz));
      |  d1Flip3=vec3(d1Flip3.x,min(d1Flip3.y,d1Flip3.z),d1Flip3.z);
      |  d1Flip3=vec3(d1Flip3.x,min(d1Flip3.y,d2.x),d1Flip3.z);
      |  return sqrt(d1Flip3.xy);
      |}
      |def0(vec2(0.0));
      |""".stripMargin.trim
    )
  }

  test("Classic Perlin noise") {
    inline def fragment: Shader[Unit, Float] =
      Shader { _ =>

        import ultraviolet.noise.*

        def proxy: vec2 => Float = p => perlin(p)

        proxy(vec2(0.0f))
      }

    val actual =
      fragment.toGLSL[WebGL2](false).toOutput.code

    // println(DebugAST.toAST(fragment))
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec4 def1(in vec4 x){
      |  return x-((floor(x*0.0034602077212184668))*289.0);
      |}
      |vec4 def3(in vec4 value){
      |  return value-((floor(value*0.0034602077212184668))*289.0);
      |}
      |vec4 def2(in vec4 x){
      |  return def3(((x*34.0)+10.0)*x);
      |}
      |vec4 def4(in vec4 x){
      |  return 1.7928428649902344-(0.8537347316741943*x);
      |}
      |vec2 def5(in vec2 x){
      |  return ((x*x)*x)*((x*((x*6.0)-15.0))+10.0);
      |}
      |float def0(in vec2 p){
      |  vec4 Pi=floor(p.xyxy)+vec4(0.0,0.0,1.0,1.0);
      |  vec4 Pf=fract(p.xyxy)-vec4(0.0,0.0,1.0,1.0);
      |  Pi=def3(Pi);
      |  vec4 ix=Pi.xzxz;
      |  vec4 iy=Pi.yyww;
      |  vec4 fx=Pf.xzxz;
      |  vec4 fy=Pf.yyww;
      |  vec4 i=def2(def2(ix)+iy);
      |  vec4 gx=((fract(i*0.024390242993831635))*2.0)-1.0;
      |  vec4 gy=abs(gx)-0.5;
      |  vec4 tx=floor(gx+0.5);
      |  gx=gx-tx;
      |  vec2 g00=vec2(gx.x,gy.x);
      |  vec2 g10=vec2(gx.y,gy.y);
      |  vec2 g01=vec2(gx.z,gy.z);
      |  vec2 g11=vec2(gx.w,gy.w);
      |  vec4 norm=def4(vec4(dot(g00,g00),dot(g01,g01),dot(g10,g10),dot(g11,g11)));
      |  g00=g00*norm.x;
      |  g01=g01*norm.y;
      |  g10=g10*norm.z;
      |  g11=g11*norm.w;
      |  float n00=dot(g00,vec2(fx.x,fy.x));
      |  float n10=dot(g10,vec2(fx.y,fy.y));
      |  float n01=dot(g01,vec2(fx.z,fy.z));
      |  float n11=dot(g11,vec2(fx.w,fy.w));
      |  vec2 fade_xy=def5(Pf.xy);
      |  vec2 n_x=mix(vec2(n00,n01),vec2(n10,n11),fade_xy.x);
      |  float n_xy=mix(n_x.x,n_x.y,fade_xy.y);
      |  return 2.299999952316284*n_xy;
      |}
      |def0(vec2(0.0));
      |""".stripMargin.trim
    )
  }

  test("Gradient noise") {
    inline def fragment: Shader[Unit, vec3] =
      Shader { _ =>

        import ultraviolet.noise.*

        def proxy: vec2 => vec3 = p => gradient(p)

        proxy(vec2(0.0f))
      }

    val actual =
      fragment.toGLSL[WebGL2](false).toOutput.code

    // println(DebugAST.toAST(fragment))
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec2 def1(in vec2 p){
      |  vec2 k=vec2(0.3183099031448364,0.36787939071655273);
      |  vec2 y=(p*k)+k.yx;
      |  return (-1.0)+(2.0*(fract((16.0*k)*(fract((y.x*y.y)*(y.x+y.y))))));
      |}
      |vec3 def0(in vec2 p){
      |  vec2 i=floor(p);
      |  vec2 f=fract(p);
      |  vec2 u=(f*f)*(3.0-(2.0*f));
      |  vec2 du=(6.0*f)*(1.0-f);
      |  vec2 ga=def1(i+vec2(0.0,0.0));
      |  vec2 gb=def1(i+vec2(1.0,0.0));
      |  vec2 gc=def1(i+vec2(0.0,1.0));
      |  vec2 gd=def1(i+vec2(1.0,1.0));
      |  float va=dot(ga,f-vec2(0.0,0.0));
      |  float vb=dot(gb,f-vec2(1.0,0.0));
      |  float vc=dot(gc,f-vec2(0.0,1.0));
      |  float vd=dot(gd,f-vec2(1.0,1.0));
      |  return vec3((va+(u.x*(vb-va)))+(u.y*(vc-va))+(u.x*u.y)*(((va-vb)-vc)+vd),(ga+(u.x*(gb-ga)))+(u.y*(gc-ga))+(u.x*u.y)*(((ga-gb)-gc)+gd)+(du*((u.yx*(((va-vb)-vc)+vd))+vec2(vb,vc)-va)));
      |}
      |def0(vec2(0.0));
      |""".stripMargin.trim
    )
  }

  test("Simplex noise") {
    inline def fragment: Shader[Unit, Float] =
      Shader { _ =>

        import ultraviolet.noise.*

        def proxy: vec2 => Float = p => simplex(p)

        proxy(vec2(0.0f))
      }

    val actual =
      fragment.toGLSL[WebGL2](false).toOutput.code

    // println(DebugAST.toAST(fragment))
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec2 def1(in vec2 x){
      |  return x-((floor(x*0.0034602077212184668))*289.0);
      |}
      |vec3 def3(in vec3 value){
      |  return value-((floor(value*0.0034602077212184668))*289.0);
      |}
      |vec3 def2(in vec3 x){
      |  return def3(((34.0*x)+10.0)*x);
      |}
      |float def0(in vec2 p){
      |  vec4 C=vec4(0.21132487058639526,0.3660254180431366,-0.5773502588272095,0.024390242993831635);
      |  vec2 i=floor(p+dot(p,C.yy));
      |  vec2 x0=(p-i)+dot(i,C.xx);
      |  vec2 i1;
      |  if(x0.x>x0.y){
      |    i1=vec2(1.0,0.0);
      |  }else{
      |    i1=vec2(0.0,1.0);
      |  }
      |  vec4 x12=x0.xyxy+C.xxzz;
      |  x12=vec4(x12.xy-i1,x12.zw);
      |  i=def1(i);
      |  vec3 p=def2(((def2(i.y+vec3(0.0,i1.y,1.0)))+i.x)+vec3(0.0,i1.x,1.0));
      |  vec3 m=max(0.5-vec3(dot(x0,x0),dot(x12.xy,x12.xy),dot(x12.zw,x12.zw)),0.0);
      |  m=m*m;
      |  m=m*m;
      |  vec3 x=(2.0*(fract(p*C.www)))-1.0;
      |  vec3 h=abs(x)-0.5;
      |  vec3 ox=floor(x+0.5);
      |  vec3 a0=x-ox;
      |  m=m*(1.7928428649902344-(0.8537347316741943*(a0*a0)+(h*h)));
      |  vec3 g=vec3((a0.x*x0.x)+(h.x*x0.y),(a0.yz*x12.xz)+(h.yz*x12.yw));
      |  return 130.0*dot(m,g);
      |}
      |def0(vec2(0.0));
      |""".stripMargin.trim
    )
  }

  test("White noise") {
    inline def fragment: Shader[Unit, vec3] =
      Shader { _ =>

        import ultraviolet.noise.*

        def proxy: vec2 => vec3 = p => white(p)

        proxy(vec2(0.0f))
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // println(DebugAST.toAST(fragment))

    assertEquals(
      actual,
      s"""
      |vec3 def0(in vec2 p){
      |  vec3 a=fract(p.xyx*vec3(123.33999633789062,234.33999633789062,345.6499938964844));
      |  a=a+(dot(a,a+34.45000076293945));
      |  return fract(vec3(a.x*a.y,a.y*a.z,a.z*a.x));
      |}
      |def0(vec2(0.0));
      |""".stripMargin.trim
    )
  }

}
