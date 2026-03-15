package indigo.render.pipeline.datatypes

import indigo.render.pipeline.datatypes.DisplayLayer
import indigo.render.pipeline.datatypes.DisplayObject
import indigo.render.pipeline.datatypes.DisplayObjectUniformData
import indigo.scenegraph.Camera
import indigo.shaders.ShaderId
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.KVP

final class ProcessedSceneData(
    val layers: Batch[DisplayLayer],
    val cloneBlankDisplayObjects: KVP[DisplayObject],
    val shaderId: ShaderId,
    val shaderUniformData: Batch[DisplayObjectUniformData],
    val camera: Option[Camera]
)
