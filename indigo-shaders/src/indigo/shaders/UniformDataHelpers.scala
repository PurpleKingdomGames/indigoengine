package indigo.shaders

import indigo.core.datatypes.Fill
import indigoengine.shared.collections.Batch

object UniformDataHelpers:

  // GRADIENT_FROM_TO (vec4), GRADIENT_FROM_COLOR (vec4), GRADIENT_TO_COLOR (vec4),
  def fillToUniformData(fill: Fill, prefix: String): Batch[(Uniform, ShaderPrimitive)] =
    fill match
      case Fill.Color(color) =>
        Batch(
          Uniform(prefix + "_GRADIENT") -> ShaderPrimitive.rawJSArray(
            scalajs.js.Array(
              0.0f,
              0.0f,
              0.0f,
              0.0f,
              color.r.toFloat,
              color.g.toFloat,
              color.b.toFloat,
              color.a.toFloat,
              color.r.toFloat,
              color.g.toFloat,
              color.b.toFloat,
              color.a.toFloat
            )
          )
        )

      case Fill.LinearGradient(fromPoint, fromColor, toPoint, toColor) =>
        Batch(
          Uniform(prefix + "_GRADIENT") -> ShaderPrimitive.rawJSArray(
            scalajs.js.Array(
              fromPoint.x.toFloat,
              fromPoint.y.toFloat,
              toPoint.x.toFloat,
              toPoint.y.toFloat,
              fromColor.r.toFloat,
              fromColor.g.toFloat,
              fromColor.b.toFloat,
              fromColor.a.toFloat,
              toColor.r.toFloat,
              toColor.g.toFloat,
              toColor.b.toFloat,
              toColor.a.toFloat
            )
          )
        )

      case Fill.RadialGradient(fromPoint, fromColor, toPoint, toColor) =>
        Batch(
          Uniform(prefix + "_GRADIENT") -> ShaderPrimitive.rawJSArray(
            scalajs.js.Array(
              fromPoint.x.toFloat,
              fromPoint.y.toFloat,
              toPoint.x.toFloat,
              toPoint.y.toFloat,
              fromColor.r.toFloat,
              fromColor.g.toFloat,
              fromColor.b.toFloat,
              fromColor.a.toFloat,
              toColor.r.toFloat,
              toColor.g.toFloat,
              toColor.b.toFloat,
              toColor.a.toFloat
            )
          )
        )
