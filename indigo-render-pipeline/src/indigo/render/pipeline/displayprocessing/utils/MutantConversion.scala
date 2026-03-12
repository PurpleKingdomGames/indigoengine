package indigo.render.pipeline.displayprocessing.utils

import indigo.core.utils.QuickCache
import indigo.render.pipeline.datatypes.DisplayMutants
import indigo.render.pipeline.datatypes.DisplayObjectUniformData
import indigo.render.pipeline.displayprocessing.utils.*
import indigo.scenegraph.Mutants
import indigo.shaders.UniformBlock
import indigoengine.shared.collections.Batch

object MutantConversion:

  def mutantsToDisplayEntities(mutants: Mutants)(using
      QuickCache[Batch[Float]]
  ): DisplayMutants =
    val uniformDataConvert: Batch[UniformBlock] => Batch[DisplayObjectUniformData] = uniformBlocks =>
      uniformBlocks.map { ub =>
        DisplayObjectUniformData(
          uniformHash = ub.uniformHash,
          blockName = ub.blockName.toString,
          data = PackUBOs.packUBO(ub.uniforms, ub.uniformHash, false)
        )
      }

    new DisplayMutants(
      id = mutants.id,
      cloneData = mutants.uniformBlocks.map(uniformDataConvert)
    )
