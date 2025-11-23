package indigo.shared.display

import indigo.shared.scenegraph.Blend
import indigo.shared.scenegraph.Camera
import indigo.shared.scenegraph.LayerKey
import indigo.shared.shader.ShaderId
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
