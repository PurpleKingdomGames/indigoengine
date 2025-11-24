package indigo.shared.platform

import indigo.scenegraph.Camera
import indigo.shaders.ShaderId
import indigo.shared.display.DisplayLayer
import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayObjectUniformData

final class ProcessedSceneData(
    val layers: scalajs.js.Array[DisplayLayer],
    val cloneBlankDisplayObjects: scalajs.js.Dictionary[DisplayObject],
    val shaderId: ShaderId,
    val shaderUniformData: scalajs.js.Array[DisplayObjectUniformData],
    val camera: Option[Camera]
)
