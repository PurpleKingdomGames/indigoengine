package indigo.internal.services

import indigo.platform.assets.TempImageData
import indigo.platform.imaging.BlitInstruction
import indigo.platform.imaging.ImageService

object NativeImageService:

  def apply(): ImageService[TempImageData, Array[Byte]] =
    new ImageService[TempImageData, Array[Byte]]:
      def composeImage(width: Int, height: Int, blits: Seq[BlitInstruction[TempImageData]]): Array[Byte] =
        // There are no assets yet, so should never be called for this PoC.
        Array.empty[Byte]
