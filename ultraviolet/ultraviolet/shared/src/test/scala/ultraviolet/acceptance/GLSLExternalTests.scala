package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLExternalTests extends munit.FunSuite {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  case class FragEnv(UV: vec2, var COLOR: vec4)

  test("Inlined external def") {

    inline def alpha: Float = 1.0f

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        vec4(1.0f, 1.0f, 0.0f, alpha)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)
    // println(ShaderMacros.toAST(fragment))

    assert(clue(actual) == clue("vec4(1.0,1.0,0.0,1.0);"))
  }

  test("Inlined external non-primitive (as def)") {

    inline def fn2: vec2 = vec2(0.0f, 1.0f)

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        vec4(1.0f, 1.0f, fn2)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    assert(clue(actual) == clue("vec4(1.0,1.0,vec2(0.0,1.0));"))
  }

  test("Inlined external function") {
    // The argument here will be ignored and inlined. Inlines are weird.
    inline def fn1(v: Float): vec2 =
      vec2(v)

    inline def fn2: Float => vec2 =
      alpha => vec2(0.0f, alpha)

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { env =>
        vec4(fn1(1.0f), fn2(1.0f))
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec2 def0(in float alpha){
      |  return vec2(0.0,alpha);
      |}
      |vec4(vec2(1.0),def0(1.0));
      |""".stripMargin.trim
    )
  }

  test("Inlined external function N args") {

    // The argument here will be ignored and inlined. Inlines are weird.
    inline def fn1(red: Float, green: Float): vec2 =
      vec2(red, green)

    // Is treated like a function
    inline def fn2: (Float, Float) => vec2 =
      (blue, alpha) => vec2(blue, alpha)

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { env =>
        vec4(fn1(1.0f, 0.25f), fn2(0.5f, 1.0f))
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    assertEquals(
      actual,
      s"""
      |vec2 def0(in float blue,in float alpha){
      |  return vec2(blue,alpha);
      |}
      |vec4(vec2(1.0,0.25),def0(0.5,1.0));
      |""".stripMargin.trim
    )
  }

  test("should correctly render tile code") {

    inline def fragment =
      Shader {
        import TileAndStretch.*

        val uv: vec2          = vec2(1.0)
        val channelPos: vec2  = vec2(2.0)
        val channelSize: vec2 = vec2(3.0)
        val entitySize: vec2  = vec2(4.0)
        val textureSize: vec2 = vec2(5.0)

        tiledUVs(
          uv,          // env.UV,
          channelPos,  // env.CHANNEL_0_POSITION,
          channelSize, // env.CHANNEL_0_SIZE,
          entitySize,  // env.SIZE,
          textureSize  // env.TEXTURE_SIZE
        )
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec2 uv=vec2(1.0);
      |vec2 channelPos=vec2(2.0);
      |vec2 channelSize=vec2(3.0);
      |vec2 entitySize=vec2(4.0);
      |vec2 textureSize=vec2(5.0);
      |channelPos+((fract(uv*(entitySize/textureSize)))*channelSize);
      |""".stripMargin.trim
    )

  }

  test("should correctly render stretch code") {

    inline def fragment =
      Shader {
        import TileAndStretch.*

        val uv: vec2          = vec2(1.0)
        val channelPos: vec2  = vec2(2.0)
        val channelSize: vec2 = vec2(3.0)

        stretchedUVs(
          uv,         // env.UV,
          channelPos, // env.CHANNEL_0_POSITION,
          channelSize // env.CHANNEL_0_SIZE,
        )
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec2 uv=vec2(1.0);
      |vec2 channelPos=vec2(2.0);
      |vec2 channelSize=vec2(3.0);
      |channelPos+(uv*channelSize);
      |""".stripMargin.trim
    )

  }

  test("should correctly render tile and stretch code (def)") {

    inline def fragment =
      Shader {
        import TileAndStretch.*

        val fillType: Int              = 0
        val fallback: vec4             = vec4(1.0)
        val srcChannel: sampler2D.type = sampler2D
        val channelPos: vec2           = vec2(2.0)
        val channelSize: vec2          = vec2(3.0)
        val uv: vec2                   = vec2(4.0)
        val entitySize: vec2           = vec2(5.0)
        val textureSize: vec2          = vec2(6.0)

        tileAndStretchChannelDef(
          fillType,    // env.FILLTYPE.toInt,
          fallback,    // env.CHANNEL_0,
          srcChannel,  // env.SRC_CHANNEL,
          channelPos,  // env.CHANNEL_0_POSITION,
          channelSize, // env.CHANNEL_0_SIZE,
          uv,          // env.UV,
          entitySize,  // env.SIZE,
          textureSize  // env.TEXTURE_SIZE
        )
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |int fillType=0;
      |vec4 fallback=vec4(1.0);
      |sampler2D srcChannel=sampler2D;
      |vec2 channelPos=vec2(2.0);
      |vec2 channelSize=vec2(3.0);
      |vec2 uv=vec2(4.0);
      |vec2 entitySize=vec2(5.0);
      |vec2 textureSize=vec2(6.0);
      |switch(fillType){
      |  case 1:
      |    texture(srcChannel,channelPos+(uv*channelSize));
      |    break;
      |  case 2:
      |    texture(srcChannel,channelPos+((fract(uv*(entitySize/textureSize)))*channelSize));
      |    break;
      |  default:
      |    fallback;
      |    break;
      |}
      |""".stripMargin.trim
    )

  }

  test("should correctly render tile and stretch code (fn)") {

    inline def fragment =
      Shader {
        import TileAndStretch.*

        val fillType: Int              = 0
        val fallback: vec4             = vec4(1.0)
        val srcChannel: sampler2D.type = sampler2D
        val channelPos: vec2           = vec2(2.0)
        val channelSize: vec2          = vec2(3.0)
        val uv: vec2                   = vec2(4.0)
        val entitySize: vec2           = vec2(5.0)
        val textureSize: vec2          = vec2(6.0)

        tileAndStretchChannelFn(
          fillType,    // env.FILLTYPE.toInt,
          fallback,    // env.CHANNEL_0,
          srcChannel,  // env.SRC_CHANNEL,
          channelPos,  // env.CHANNEL_0_POSITION,
          channelSize, // env.CHANNEL_0_SIZE,
          uv,          // env.UV,
          entitySize,  // env.SIZE,
          textureSize  // env.TEXTURE_SIZE
        )
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec2 def0(in int _fillType,in vec4 _fallback,in sampler2D _srcChannel,in vec2 _channelPos,in vec2 _channelSize,in vec2 _uv,in vec2 _entitySize){
      |  return switch(_fillType){
      |    case 1:
      |      texture(_srcChannel,_channelPos+(_uv*_channelSize));
      |      break;
      |    case 2:
      |      texture(_srcChannel,_channelPos+((fract(_uv*(_entitySize/_textureSize)))*_channelSize));
      |      break;
      |    default:
      |      _fallback;
      |      break;
      |  }
      |}
      |int fillType=0;
      |vec4 fallback=vec4(1.0);
      |sampler2D srcChannel=sampler2D;
      |vec2 channelPos=vec2(2.0);
      |vec2 channelSize=vec2(3.0);
      |vec2 uv=vec2(4.0);
      |vec2 entitySize=vec2(5.0);
      |vec2 textureSize=vec2(6.0);
      |def0(fillType,fallback,srcChannel,channelPos,channelSize,uv,entitySize,textureSize);
      |""".stripMargin.trim
    )

  }

}

object TileAndStretch:

  inline def stretchedUVs(uv: vec2, channelPos: vec2, channelSize: vec2): vec2 =
    channelPos + uv * channelSize

  inline def tiledUVs(uv: vec2, channelPos: vec2, channelSize: vec2, entitySize: vec2, textureSize: vec2): vec2 =
    channelPos + (fract(uv * (entitySize / textureSize)) * channelSize)

  inline def tileAndStretchChannelDef(
      _fillType: Int,
      _fallback: vec4,
      _srcChannel: sampler2D.type,
      _channelPos: vec2,
      _channelSize: vec2,
      _uv: vec2,
      _entitySize: vec2,
      _textureSize: vec2
  ): vec4 =
    _fillType match
      case 1 =>
        texture2D(
          _srcChannel,
          stretchedUVs(_uv, _channelPos, _channelSize)
        )

      case 2 =>
        texture2D(
          _srcChannel,
          tiledUVs(_uv, _channelPos, _channelSize, _entitySize, _textureSize)
        )

      case _ =>
        _fallback

  inline def tileAndStretchChannelFn =
    (
        _fillType: Int,
        _fallback: vec4,
        _srcChannel: sampler2D.type,
        _channelPos: vec2,
        _channelSize: vec2,
        _uv: vec2,
        _entitySize: vec2,
        _textureSize: vec2
    ) =>
      _fillType match
        case 1 =>
          texture2D(
            _srcChannel,
            stretchedUVs(_uv, _channelPos, _channelSize)
          )

        case 2 =>
          texture2D(
            _srcChannel,
            tiledUVs(_uv, _channelPos, _channelSize, _entitySize, _textureSize)
          )

        case _ =>
          _fallback
