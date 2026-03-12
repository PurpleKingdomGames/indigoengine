package indigo.render.pipeline.displayprocessing.utils

import indigo.core.utils.QuickCache
import indigo.shaders.ShaderPrimitive
import indigo.shaders.Uniform
import indigoengine.shared.collections.Batch

import scala.annotation.tailrec

object PackUBOs:

  private val empty0: Batch[Float] = Batch[Float]()
  private val empty1: Batch[Float] = Batch[Float](0.0f)
  private val empty2: Batch[Float] = Batch[Float](0.0f, 0.0f)
  private val empty3: Batch[Float] = Batch[Float](0.0f, 0.0f, 0.0f)

  def expandTo4(arr: Batch[Float]): Batch[Float] =
    arr.length match {
      case 0 => arr
      case 1 => arr ++ empty3
      case 2 => arr ++ empty2
      case 3 => arr ++ empty1
      case 4 => arr
      case _ => arr
    }

  def packUBO(
      uniforms: Batch[(Uniform, ShaderPrimitive)],
      cacheKey: String,
      disableCache: Boolean
  )(using QuickCache[Batch[Float]]): Batch[Float] = {
    @tailrec
    def rec(
        remaining: Batch[ShaderPrimitive],
        current: Batch[Float],
        acc: Batch[Float]
    ): Batch[Float] =
      remaining match
        case us if us.isEmpty =>
          // println(s"done, expanded: ${current.toList} to ${expandTo4(current).toList}")
          // println(s"result: ${(acc ++ expandTo4(current)).toList}")
          acc ++ expandTo4(current)

        case us if current.length == 4 =>
          // println(s"current full, sub-result: ${(acc ++ current).toList}")
          rec(us, empty0, acc ++ current)

        case us if current.isEmpty && us.head.isArray =>
          // println(s"Found an array, current is empty, set current to: ${u.toArray.toList}")
          rec(us.tail, us.head.toBatch, acc)

        case us if current.length == 1 && us.head.length == 2 =>
          // println("Current value is float, must not straddle byte boundary when adding vec2")
          rec(us.tail, current ++ Batch(0.0f) ++ us.head.toBatch, acc)

        case us if current.length + us.head.length > 4 =>
          // println(s"doesn't fit, expanded: ${current.toList} to ${expandTo4(current).toList},  sub-result: ${(acc ++ expandTo4(current)).toList}")
          rec(us, empty0, acc ++ expandTo4(current))

        case us if us.head.isArray =>
          // println(s"fits but next value is array, expanded: ${current.toList} to ${expandTo4(current).toList},  sub-result: ${(acc ++ expandTo4(current)).toList}")
          rec(us, empty0, acc ++ current)

        case us =>
          // println(s"fits, current is now: ${(current ++ u.toArray).toList}")
          rec(us.tail, current ++ us.head.toBatch, acc)

    QuickCache(cacheKey, disableCache) {
      rec(uniforms.map(_._2), empty0, empty0)
    }
  }
