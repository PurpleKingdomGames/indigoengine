package indigo.platform.api.display

import indigo.scenegraph.Blend
import indigo.scenegraph.Camera
import indigo.scenegraph.LayerKey
import indigo.shaders.ShaderId
import indigoengine.shared.datatypes.RGBA

final case class DisplayLayer(
    layerKey: Option[LayerKey],
    entities: scalajs.js.Array[DisplayEntity],
    lightsData: scalajs.js.Array[Float],
    bgColor: RGBA,
    magnification: Option[Int],
    entityBlend: Blend,
    layerBlend: Blend,
    shaderId: ShaderId,
    shaderUniformData: scalajs.js.Array[DisplayObjectUniformData],
    camera: Option[Camera]
) derives CanEqual
