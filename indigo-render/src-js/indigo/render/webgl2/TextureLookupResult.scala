package indigo.render.webgl2

import indigo.render.pipeline.AtlasId
import org.scalajs.dom.WebGLTexture

final case class TextureLookupResult(name: AtlasId, texture: WebGLTexture) derives CanEqual
object TextureLookupResult:
  given CanEqual[Option[TextureLookupResult], Option[TextureLookupResult]] = CanEqual.derived
