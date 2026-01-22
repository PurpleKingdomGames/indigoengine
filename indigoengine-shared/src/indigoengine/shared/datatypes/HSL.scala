package indigoengine.shared.datatypes

/** Represents a color in HSL color space.
  */
final case class HSL(h: Double, s: Double, l: Double) derives CanEqual:

  /** Copy with a new hue component */
  def withHue(newHue: Double): HSL =
    this.copy(h = newHue)

  /** Copy with a new saturation component */
  def withSaturation(newSaturation: Double): HSL =
    this.copy(s = newSaturation)

  /** Copy with a new lightness component */
  def withLightness(newLightness: Double): HSL =
    this.copy(l = newLightness)

  /** Rotate hue by degrees on color wheel (positive = clockwise) */
  def rotateHue(degrees: Degrees): HSL =
    this.copy(h = (Degrees(h) + degrees).wrap.toDouble)

  /** Lighten by amount (0.0-1.0), increasing lightness */
  def lighten(amount: Double): HSL =
    this.copy(l = Math.min(1.0, l + amount))

  /** Darken by amount (0.0-1.0), decreasing lightness */
  def darken(amount: Double): HSL =
    this.copy(l = Math.max(0.0, l - amount))

  /** Saturate by amount (0.0-1.0), increasing saturation */
  def saturate(amount: Double): HSL =
    this.copy(s = Math.min(1.0, s + amount))

  /** Desaturate by amount (0.0-1.0), decreasing saturation */
  def desaturate(amount: Double): HSL =
    this.copy(s = Math.max(0.0, s - amount))

  /** Convert to RGB color */
  def toRGB: RGB =
    toRGBA.toRGB

  /** Convert to RGBA color with full opacity */
  def toRGBA: RGBA =
    toHSLA.toRGBA

  /** Convert to RGBA color with given opacity */
  def toRGBA(opacity: Double): RGBA =
    toHSLA(opacity).toRGBA

  /** Convert to HSLA with full opacity */
  def toHSLA: HSLA =
    HSLA(h, s, l, 1.0)

  /** Convert to HSLA with given opacity */
  def toHSLA(opacity: Double): HSLA =
    HSLA(h, s, l, opacity)

  /** CSS hsl string */
  def toCSSValue: String =
    s"hsl($h, ${s * 100}%, ${l * 100}%)"

object HSL:

  val Red: HSL     = HSL(0, 1.0, 0.5)
  val Green: HSL   = HSL(120, 1.0, 0.5)
  val Blue: HSL    = HSL(240, 1.0, 0.5)
  val Yellow: HSL  = HSL(60, 1.0, 0.5)
  val Magenta: HSL = HSL(300, 1.0, 0.5)
  val Cyan: HSL    = HSL(180, 1.0, 0.5)
  val White: HSL   = HSL(0, 0.0, 1.0)
  val Black: HSL   = HSL(0, 0.0, 0.0)

  /** Create HSL from RGB */
  private def fromRGBValues(r: Double, g: Double, b: Double): HSL =
    val cmax  = Math.max(r, Math.max(g, b))
    val cmin  = Math.min(r, Math.min(g, b))
    val delta = cmax - cmin
    val l     = (cmax + cmin) / 2.0
    val s =
      if delta == 0 then 0.0
      else delta / (1.0 - Math.abs(2.0 * l - 1.0))
    val h =
      if delta == 0 then 0.0
      else if cmax == r then 60.0 * (((g - b) / delta) % 6)
      else if cmax == g then 60.0 * (((b - r) / delta) + 2)
      else 60.0 * (((r - g) / delta) + 4)
    HSL(if h < 0 then h + 360 else h, s, l)

  /** Create HSL from RGB */
  def fromRGB(rgb: RGB): HSL =
    fromRGBValues(rgb.r, rgb.g, rgb.b)

  /** Create HSLA from RGBA */
  def fromRGBA(rgba: RGBA): HSL =
    fromRGBValues(rgba.r, rgba.g, rgba.b)
