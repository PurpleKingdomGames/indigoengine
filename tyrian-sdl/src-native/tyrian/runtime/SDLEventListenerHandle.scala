package tyrian.runtime

opaque type SDLEventListenerHandle = String

object SDLEventListenerHandle:
  def apply(s: String): SDLEventListenerHandle                      = s
  extension (h: SDLEventListenerHandle) inline def asString: String = h
