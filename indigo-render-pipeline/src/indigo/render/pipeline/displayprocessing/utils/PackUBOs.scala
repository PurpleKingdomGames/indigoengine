package indigo.render.pipeline.displayprocessing.utils

import indigo.core.utils.QuickCache
import indigo.shaders.ShaderPrimitive
import indigo.shaders.Uniform
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.mutable

object PackUBOs:

  private val buf: mutable.Batch[Float] = mutable.Batch.empty

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while"))
  def packUBO(
      uniforms: Batch[(Uniform, ShaderPrimitive)],
      cacheKey: String,
      disableCache: Boolean
  )(using QuickCache[Batch[Float]]): Batch[Float] =
    QuickCache(cacheKey, disableCache) {
      val prims = uniforms.map(_._2)
      buf.clear()
      var rowUsed = 0
      var i       = 0

      while i < prims.length do
        val p = prims(i)
        i += 1

        if rowUsed == 4 then rowUsed = 0 // Row complete, reset

        // Arrays are pre-packed; pad partial row and append directly
        if p.isArray then
          if rowUsed > 0 then
            while rowUsed < 4 do
              buf.append(0.0f)
              rowUsed += 1
            rowUsed = 0
          appendBatch(buf, p.toBatch)
        else
          val primLen = p.length

          // std140: vec2 must not straddle a 16-byte boundary
          if rowUsed == 1 && primLen == 2 then
            buf.append(0.0f)
            rowUsed = 2

          // Primitive doesn't fit in current row
          if rowUsed + primLen > 4 then
            // Pad remaining row; rowUsed == 0 handles large primitives like mat4
            if rowUsed > 0 then
              while rowUsed < 4 do
                buf.append(0.0f)
                rowUsed += 1
            appendBatch(buf, p.toBatch)
            rowUsed = primLen % 4
          else
            appendBatch(buf, p.toBatch)
            rowUsed += primLen

      // Pad final partial row
      if rowUsed > 0 && rowUsed < 4 then
        while rowUsed < 4 do
          buf.append(0.0f)
          rowUsed += 1

      buf.toBatch
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while"))
  private def appendBatch(buf: mutable.Batch[Float], b: Batch[Float]): Unit =
    var j = 0
    while j < b.length do
      buf.append(b(j))
      j += 1
