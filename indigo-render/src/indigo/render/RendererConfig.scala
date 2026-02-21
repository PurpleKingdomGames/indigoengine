package indigo.render

import indigoengine.shared.datatypes.RGBA

final class RendererConfig(
    val clearColor: RGBA,
    val magnification: Int,
    val maxBatchSize: Int,
    val antiAliasing: Boolean,
    val premultipliedAlpha: Boolean,
    val transparentBackground: Boolean
)
