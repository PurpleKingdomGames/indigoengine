package indigo.platform.assets

import indigo.core.datatypes.PowerOfTwo

final case class TextureMap(size: PowerOfTwo, textureCoords: List[TextureAndCoords]) derives CanEqual
