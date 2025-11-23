package indigo.shared.platform

import indigo.shared.config.RenderingTechnology
import indigoengine.shared.datatypes.RGBA

final class RendererConfig(
    val renderingTechnology: RenderingTechnology,
    val clearColor: RGBA,
    val magnification: Int,
    val maxBatchSize: Int,
    val antiAliasing: Boolean,
    val premultipliedAlpha: Boolean,
    val transparentBackground: Boolean
)
