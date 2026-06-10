package tyrian.extensions

final case class RegisteredExtension[GraphicsContext, View](
    id: String,
    extension: Extension[GraphicsContext, View],
    isGraphical: Boolean
) derives CanEqual
