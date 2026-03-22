package indigo.platform.assets

import indigo.core.datatypes.Point

final case class TextureAndCoords(imageRef: ImageRef, coords: Point) derives CanEqual
