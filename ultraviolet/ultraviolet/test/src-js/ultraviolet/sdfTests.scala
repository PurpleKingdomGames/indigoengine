package ultraviolet

import ultraviolet.syntax.*

// These tests are run as JS only due to differences between JVM and JS floating point precision
class sdfTests extends munit.FunSuite {

  test("A circle SDF") {

    val p = vec2(1.0f, 0.0f)

    val actual =
      ultraviolet.sdf.circle(p, 1.0f)

    val expected =
      0.0f

    assert(closeEnough(clue(actual), clue(expected)))
  }

  test("Box SDF acceptance test") {
    inline def fragment: Shader[Unit, Float] =
      Shader { _ =>

        import ultraviolet.sdf.*

        def proxy: (vec2, vec2) => Float = (p, b) => box(p, b)

        proxy(vec2(0.5f), vec2(1.5f))
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)

    assertEquals(
      actual,
      s"""
      |float def1(in vec2 _ptArg,in vec2 _hsArg){
      |  vec2 d=abs(_ptArg)-_hsArg;
      |  return length(max(d,0.0))+min(max(d.x,d.y),0.0);
      |}
      |float def0(in vec2 p,in vec2 b){
      |  return def1(p,b);
      |}
      |def0(vec2(0.5),vec2(1.5));
      |""".stripMargin.trim
    )
  }

  test("Circle SDF acceptance test") {
    inline def fragment: Shader[Unit, Float] =
      Shader { _ =>

        import ultraviolet.sdf.*

        def proxy: (vec2, Float) => Float = (p, r) => circle(p, r)

        proxy(vec2(0.5f), 1.5f)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)

    assertEquals(
      actual,
      s"""
      |float def1(in vec2 _ptArg,in float _rArg){
      |  return length(_ptArg)-_rArg;
      |}
      |float def0(in vec2 p,in float r){
      |  return def1(p,r);
      |}
      |def0(vec2(0.5),1.5);
      |""".stripMargin.trim
    )
  }

  test("Hexagon SDF acceptance test") {
    inline def fragment: Shader[Unit, Float] =
      Shader { _ =>

        import ultraviolet.sdf.*

        def proxy: (vec2, Float) => Float = (p, r) => hexagon(p, r)

        proxy(vec2(0.5f), 1.5f)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)

    assertEquals(
      actual,
      s"""
      |float def1(in vec2 _ptArg,in float _rArg){
      |  vec3 k=vec3(-0.8660253882408142,0.5,0.5773502588272095);
      |  vec2 pt=abs(_ptArg);
      |  pt=pt-((2.0*min(dot(k.xy,pt),0.0))*k.xy);
      |  pt=pt-(vec2(clamp(pt.x,(-k.z)*_rArg,k.z*_rArg),_rArg));
      |  return length(pt)*sign(pt.y);
      |}
      |float def0(in vec2 p,in float r){
      |  return def1(p,r);
      |}
      |def0(vec2(0.5),1.5);
      |""".stripMargin.trim
    )
  }

  test("Segment SDF acceptance test") {
    inline def fragment: Shader[Unit, Float] =
      Shader { _ =>

        import ultraviolet.sdf.*

        def proxy: (vec2, vec2, vec2) => Float = (p, a, b) => segment(p, a, b)

        proxy(vec2(0.5f), vec2(0.0f), vec2(1.0f))
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)

    assertEquals(
      actual,
      s"""
      |float def1(in vec2 _ptArg,in vec2 _aArg,in vec2 _bArg){
      |  vec2 pa=_ptArg-_aArg;
      |  vec2 ba=_bArg-_aArg;
      |  float h=clamp(dot(pa,ba)/dot(ba,ba),0.0,1.0);
      |  return length(pa-(ba*h));
      |}
      |float def0(in vec2 p,in vec2 a,in vec2 b){
      |  return def1(p,a,b);
      |}
      |def0(vec2(0.5),vec2(0.0),vec2(1.0));
      |""".stripMargin.trim
    )
  }

  test("Star with five points SDF acceptance test") {
    inline def fragment: Shader[Unit, Float] =
      Shader { _ =>

        import ultraviolet.sdf.*

        def proxy: (vec2, Float, Float) => Float = (p, r, rf) => star(p, r, rf)

        proxy(vec2(0.5f), 1.5f, 1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)

    assertEquals(
      actual,
      s"""
      |float def1(in vec2 _ptArg,in float _rArg,in float _irArg){
      |  vec2 k1=vec2(0.80901700258255,-0.5877852439880371);
      |  vec2 k2=vec2(-k1.x,k1.y);
      |  vec2 p2=vec2(abs(_ptArg.x),-_ptArg.y);
      |  p2=p2-((2.0*max(dot(k1,p2),0.0))*k1);
      |  p2=p2-((2.0*max(dot(k2,p2),0.0))*k2);
      |  p2=vec2(abs(p2.x),p2.y-_rArg);
      |  vec2 ba=(_irArg*(vec2(-k1.y,k1.x)))-vec2(0.0,1.0);
      |  float h=clamp(dot(p2,ba)/dot(ba,ba),0.0,_rArg);
      |  return (length(p2-(ba*h)))*(sign((p2.y*ba.x)-(p2.x*ba.y)));
      |}
      |float def0(in vec2 p,in float r,in float rf){
      |  return def1(p,r,rf);
      |}
      |def0(vec2(0.5),1.5,1.0);
      |""".stripMargin.trim
    )
  }

  test("Triangle SDF acceptance test") {
    inline def fragment: Shader[Unit, Float] =
      Shader { _ =>

        import ultraviolet.sdf.*

        def proxy: (vec2, Float) => Float = (p, r) => triangle(p, r)

        proxy(vec2(0.5f), 1.5f)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // println(DebugAST.toAST(fragment))

    assertEquals(
      actual,
      s"""
      |float def1(in vec2 _ptArg,in float _rArg){
      |  float k=sqrt(3.0);
      |  vec2 pt=vec2(abs(_ptArg.x)-_rArg,(-_ptArg.y)+(_rArg/k));
      |  if((pt.x+(k*pt.y))>0.0){
      |    pt=(vec2(pt.x-(k*pt.y),((-k)*pt.x)-pt.y))/2.0;
      |  }
      |  pt=vec2(pt.x-(clamp(pt.x,-_rArg,0.0)),pt.y);
      |  return (-length(pt))*sign(pt.y);
      |}
      |float def0(in vec2 p,in float r){
      |  return def1(p,r);
      |}
      |def0(vec2(0.5),1.5);
      |""".stripMargin.trim
    )
  }

  def closeEnough(a: Float, b: Float): Boolean =
    Math.abs(Math.abs(a) - Math.abs(b)) < 0.01f

}
