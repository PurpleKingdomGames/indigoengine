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
      |float def0(in vec2 p,in float r){
      |  return length(p)-r;
      |}
      |def0(vec2(0.5),1.5);
      |""".stripMargin.trim
    )
  }

  test("Square SDF acceptance test") {
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
      |float def0(in vec2 p,in vec2 b){
      |  vec2 d=abs(p)-b;
      |  return length(max(d,0.0))+min(max(d.x,d.y),0.0);
      |}
      |def0(vec2(0.5),vec2(1.5));
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
      |float def0(in vec2 p,in float r,in float rf){
      |  vec2 k1=vec2(0.80901700258255,-0.5877852439880371);
      |  vec2 k2=vec2(-k1.x,k1.y);
      |  vec2 p2=vec2(abs(p.x),p.y);
      |  p2=p2-((2.0*max(dot(k1,p2),0.0))*k1);
      |  p2=p2-((2.0*max(dot(k2,p2),0.0))*k2);
      |  p2=vec2(abs(p2.x),p2.y-r);
      |  vec2 ba=(rf*(vec2(-k1.y,k1.x)))-vec2(0.0,1.0);
      |  float h=clamp(dot(p2,ba)/dot(ba,ba),0.0,r);
      |  return (length(p2-(ba*h)))*(sign((p2.y*ba.x)-(p2.x*ba.y)));
      |}
      |def0(vec2(0.5),1.5,1.0);
      |""".stripMargin.trim
    )
  }

  def closeEnough(a: Float, b: Float): Boolean =
    Math.abs(Math.abs(a) - Math.abs(b)) < 0.01f

}
