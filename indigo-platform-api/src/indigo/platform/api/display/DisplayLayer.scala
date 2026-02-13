package indigo.platform.api.display

import indigo.scenegraph.Blend
import indigo.scenegraph.Camera
import indigo.scenegraph.LayerKey
import indigo.shaders.ShaderId
import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.RGBA

final case class DisplayLayer(
    layerKey: Option[LayerKey],
    entities: Batch[DisplayEntity],
    lightsData: Batch[Float],
    bgColor: RGBA,
    magnification: Option[Int],
    entityBlend: Blend,
    layerBlend: Blend,
    shaderId: ShaderId,
    shaderUniformData: Batch[DisplayObjectUniformData],
    camera: Option[Camera]
) derives CanEqual
