package indigo.render.pipeline.displayprocessing.utils

import indigo.core.utils.QuickCache
import indigo.render.pipeline.datatypes.DisplayObjectUniformData
import indigo.shaders.ShaderData
import indigoengine.shared.collections.Batch

object ConversionHelpers:

  def toDisplayObjectUniformData(shaderData: ShaderData)(using
      QuickCache[Batch[Float]]
  ): Batch[DisplayObjectUniformData] =
    shaderData.uniformBlocks.map { ub =>
      DisplayObjectUniformData(
        uniformHash = ub.uniformHash,
        blockName = ub.blockName.toString,
        data = PackUBOs.packUBO(ub.uniforms, ub.uniformHash, false)
      )
    }
