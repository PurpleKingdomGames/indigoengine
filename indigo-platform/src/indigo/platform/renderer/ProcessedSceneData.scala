package indigo.platform.renderer

import indigo.platform.display.DisplayLayer
import indigo.platform.display.DisplayObject
import indigo.platform.display.DisplayObjectUniformData
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
