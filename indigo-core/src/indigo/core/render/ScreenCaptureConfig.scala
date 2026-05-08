package indigo.core.render

import indigo.core.datatypes.ImageType
import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.Vector2

/** Configuration for a screen capture
  *
  * @param name
  *   The optional name of the capture
  * @param croppingRect
  *   The rectangle to crop the capture to
  * @param scale
  *   The scale to apply to the capture
  * @param imageType
  *   The type of image to capture
  */
final case class ScreenCaptureConfig(
    name: Option[String],
    croppingRect: Option[Rectangle],
    scale: Option[Vector2],
    imageType: ImageType
) {

  /** Set the name of the capture
    *
    * @param name
    * @return
    */
  def withName(name: String): ScreenCaptureConfig =
    this.copy(name = Option(name))

  /** Set the cropping rectangle of the capture
    *
    * @param rect
    * @return
    */
  def withCrop(rect: Rectangle): ScreenCaptureConfig =
    this.copy(croppingRect = Option(rect))

  /** Set the scale of the capture
    *
    * @param scale
    * @return
    */
  def withScale(scale: Double): ScreenCaptureConfig =
    withScale(Vector2(scale, scale))

  /** Set the scale of the capture
    *
    * @param scale
    * @return
    */
  def withScale(scale: Vector2): ScreenCaptureConfig =
    this.copy(scale = Option(scale))

  /** Set the image type of the capture
    *
    * @param imageType
    * @return
    */
  def withImageType(imageType: ImageType): ScreenCaptureConfig =
    this.copy(imageType = imageType)
}

object ScreenCaptureConfig {

  /** Default configuration
    */
  val default: ScreenCaptureConfig =
    ScreenCaptureConfig(None, None, None, ImageType.WEBP)

  /** Create a configuration with a name
    *
    * @param name
    * @return
    */
  def apply(name: String): ScreenCaptureConfig =
    ScreenCaptureConfig(Some(name), None, None, ImageType.WEBP)

  /** Create a configuration with a name and cropping rectangle
    *
    * @param name
    * @param croppingRect
    * @return
    */
  def apply(name: String, croppingRect: Rectangle): ScreenCaptureConfig =
    ScreenCaptureConfig(Some(name), Some(croppingRect), None, ImageType.WEBP)

  /** Create a configuration with a name and scale
    *
    * @param name
    * @param scale
    * @return
    */
  def apply(name: String, scale: Double): ScreenCaptureConfig =
    ScreenCaptureConfig(Some(name), None, Some(Vector2(scale, scale)), ImageType.WEBP)

  /** Create a configuration with a name and scale
    *
    * @param name
    * @param scale
    * @return
    */
  def apply(name: String, scale: Vector2): ScreenCaptureConfig =
    ScreenCaptureConfig(Some(name), None, Some(scale), ImageType.WEBP)

  /** Create a configuration with a name and image type
    *
    * @param name
    * @param imageType
    * @return
    */
  def apply(name: String, imageType: ImageType): ScreenCaptureConfig =
    ScreenCaptureConfig(Some(name), None, None, imageType)

  /** Create a configuration with a cropping rectangle
    *
    * @param croppingRect
    * @return
    */
  def apply(croppingRect: Rectangle): ScreenCaptureConfig =
    ScreenCaptureConfig(None, Some(croppingRect), None, ImageType.WEBP)

  /** Create a configuration with a scale
    *
    * @param scale
    * @return
    */
  def apply(scale: Double): ScreenCaptureConfig =
    ScreenCaptureConfig(None, None, Some(Vector2(scale, scale)), ImageType.WEBP)

  /** Create a configuration with a scale
    *
    * @param scale
    * @return
    */
  def apply(scale: Vector2): ScreenCaptureConfig =
    ScreenCaptureConfig(None, None, Some(scale), ImageType.WEBP)

  /** Create a configuration with an image type
    *
    * @param imageType
    * @return
    */
  def apply(imageType: ImageType): ScreenCaptureConfig =
    ScreenCaptureConfig(None, None, None, imageType)
}
