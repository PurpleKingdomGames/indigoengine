package indigo.platform.imaging

final case class BlitInstruction[Image](
    source: Image,
    x: Int,
    y: Int,
    width: Int,
    height: Int
) derives CanEqual
