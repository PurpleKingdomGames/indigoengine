package indigo.render

import indigoengine.shared.datatypes.RGBA

final class RendererConfig(
    val clearColor: RGBA,
    val maxBatchSize: Int,
    val transparentBackground: Boolean
)
