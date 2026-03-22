package indigo.platform.imaging

/** Platform-managed image composition service. Indigo's atlas builder uses this to copy source images into a single
  * larger texture buffer; the service hides the underlying canvas (or other platform mechanism) used to perform the
  * pixel copy.
  */
trait ImageService[SrcImageData, DstImageData]:

  def composeImage(width: Int, height: Int, blits: Seq[BlitInstruction[SrcImageData]]): DstImageData
