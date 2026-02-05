package indigo.platform.api.renderer

import indigo.platform.api.display.DisplayLayer
import indigo.platform.api.display.DisplayObject
import indigo.platform.api.display.DisplayObjectUniformData
import indigo.scenegraph.Camera
import indigo.shaders.ShaderId

final class ProcessedSceneData(
    val layers: scalajs.js.Array[DisplayLayer],
    val cloneBlankDisplayObjects: scalajs.js.Dictionary[DisplayObject],
    val shaderId: ShaderId,
    val shaderUniformData: scalajs.js.Array[DisplayObjectUniformData],
    val camera: Option[Camera]
)
