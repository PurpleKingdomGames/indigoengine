package indigo.next.scenes

opaque type SceneName = String

object SceneName:

  inline def apply(sceneName: String): SceneName = sceneName

  extension (sn: SceneName) inline def asString: String = sn

  given CanEqual[SceneName, SceneName]                 = CanEqual.derived
  given CanEqual[Option[SceneName], Option[SceneName]] = CanEqual.derived
