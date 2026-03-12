package indigo.render.pipeline.sceneprocessing.utils

import indigo.core.utils.QuickCache
import indigo.render.pipeline.datatypes.DisplayObjectUniformData
import indigo.render.pipeline.displayprocessing.utils.PackUBOs
import indigo.shaders.ShaderData
import indigoengine.shared.collections.Batch

object MergeUniformData:

  def mergeShaderToUniformData(
      shaderData: ShaderData
  )(using QuickCache[Batch[Float]]): Batch[DisplayObjectUniformData] =
    shaderData.uniformBlocks.map { ub =>
      DisplayObjectUniformData(
        uniformHash = ub.uniformHash,
        blockName = ub.blockName.toString,
        data = PackUBOs.packUBO(ub.uniforms, ub.uniformHash, false)
      )
    }
