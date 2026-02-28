package tyrian.extensions

opaque type ExtensionId = String

object ExtensionId:
  inline def apply(value: String): ExtensionId =
    value

  extension (b: ExtensionId) inline def show: String = b

  given CanEqual[ExtensionId, ExtensionId]                 = CanEqual.derived
  given CanEqual[Option[ExtensionId], Option[ExtensionId]] = CanEqual.derived
