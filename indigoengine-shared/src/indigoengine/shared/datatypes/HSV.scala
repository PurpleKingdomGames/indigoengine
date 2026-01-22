package indigoengine.shared.datatypes

/** Represents a color in HSV color space.
  */
final case class HSV(h: Double, s: Double, v: Double) derives CanEqual:

  /** Copy with a new hue component */
  def withHue(newHue: Double): HSV =
    this.copy(h = newHue)

  /** Copy with a new saturation component */
  def withSaturation(newSaturation: Double): HSV =
    this.copy(s = newSaturation)

  /** Copy with a new value component */
  def withValue(newValue: Double): HSV =
    this.copy(v = newValue)

  /** Rotate hue by degrees on color wheel (positive = clockwise) */
  def rotateHue(degrees: Degrees): HSV =
    this.copy(h = (Degrees(h) + degrees).wrap.toDouble)

  /** Brighten by amount (0.0-1.0), increasing value */
  def brighten(amount: Double): HSV =
    this.copy(v = Math.min(1.0, v + amount))

  /** Darken by amount (0.0-1.0), decreasing value */
  def darken(amount: Double): HSV =
    this.copy(v = Math.max(0.0, v - amount))

  /** Saturate by amount (0.0-1.0), increasing saturation */
  def saturate(amount: Double): HSV =
    this.copy(s = Math.min(1.0, s + amount))

  /** Desaturate by amount (0.0-1.0), decreasing saturation */
  def desaturate(amount: Double): HSV =
    this.copy(s = Math.max(0.0, s - amount))

  /** Convert to RGB color */
  def toRGB: RGB =
    toRGBA.toRGB

  /** Convert to RGBA color with full opacity */
  def toRGBA: RGBA =
    toHSVA.toRGBA

  /** Convert to RGBA color with given opacity */
  def toRGBA(opacity: Double): RGBA =
    toHSVA(opacity).toRGBA

  /** Convert to HSVA with full opacity */
  def toHSVA: HSVA =
    HSVA(h, s, v, 1.0)

  /** Convert to HSVA with given opacity */
  def toHSVA(opacity: Double): HSVA =
    HSVA(h, s, v, opacity)

object HSV:

  val Red: HSV     = HSV(0, 1.0, 1.0)
  val Green: HSV   = HSV(120, 1.0, 1.0)
  val Blue: HSV    = HSV(240, 1.0, 1.0)
  val Yellow: HSV  = HSV(60, 1.0, 1.0)
  val Magenta: HSV = HSV(300, 1.0, 1.0)
  val Cyan: HSV    = HSV(180, 1.0, 1.0)
  val White: HSV   = HSV(0, 0.0, 1.0)
  val Black: HSV   = HSV(0, 0.0, 0.0)

  /** Create HSV from RGB */
  private def fromRGBValues(r: Double, g: Double, b: Double): HSV =
    val cmax  = Math.max(r, Math.max(g, b))
    val cmin  = Math.min(r, Math.min(g, b))
    val delta = cmax - cmin
    val v     = cmax
    val s =
      if cmax == 0 then 0.0
      else delta / cmax
    val h =
      if delta == 0 then 0.0
      else if cmax == r then 60.0 * (((g - b) / delta) % 6)
      else if cmax == g then 60.0 * (((b - r) / delta) + 2)
      else 60.0 * (((r - g) / delta) + 4)
    HSV(if h < 0 then h + 360 else h, s, v)

  /** Create HSV from RGB */
  def fromRGB(rgb: RGB): HSV =
    fromRGBValues(rgb.r, rgb.g, rgb.b)

  /** Create HSV from RGBA */
  def fromRGBA(rgba: RGBA): HSV =
    fromRGBValues(rgba.r, rgba.g, rgba.b)
