package indigo.platform.api.renderer

import indigo.platform.api.display.DisplayLayer
import indigo.platform.api.display.DisplayObject
import indigo.platform.api.display.DisplayObjectUniformData
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
