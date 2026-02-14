package indigoengine.shared.datatypes

/** Represents a color in HSLA color space with alpha channel.
  */
final case class HSLA(h: Double, s: Double, l: Double, a: Double) derives CanEqual:

  /** Copy with a new hue component */
  def withHue(newHue: Double): HSLA =
    this.copy(h = newHue)

  /** Copy with a new saturation component */
  def withSaturation(newSaturation: Double): HSLA =
    this.copy(s = newSaturation)

  /** Copy with a new lightness component */
  def withLightness(newLightness: Double): HSLA =
    this.copy(l = newLightness)

  /** Copy with a new alpha component */
  def withAlpha(newAlpha: Double): HSLA =
    this.copy(a = newAlpha)

  /** Return the color with full opacity */
  def makeOpaque: HSLA =
    this.copy(a = 1.0)

  /** Return the color fully transparent */
  def makeTransparent: HSLA =
    this.copy(a = 0.0)

  /** Rotate hue by degrees on color wheel (positive = clockwise) */
  def rotateHue(degrees: Degrees): HSLA =
    this.copy(h = (Degrees(h) + degrees).wrap.toDouble)

  /** Lighten by amount (0.0-1.0), increasing lightness */
  def lighten(amount: Double): HSLA =
    this.copy(l = Math.min(1.0, l + amount))

  /** Darken by amount (0.0-1.0), decreasing lightness */
  def darken(amount: Double): HSLA =
    this.copy(l = Math.max(0.0, l - amount))

  /** Saturate by amount (0.0-1.0), increasing saturation */
  def saturate(amount: Double): HSLA =
    this.copy(s = Math.min(1.0, s + amount))

  /** Desaturate by amount (0.0-1.0), decreasing saturation */
  def desaturate(amount: Double): HSLA =
    this.copy(s = Math.max(0.0, s - amount))

  /** Convert to HSL (drops alpha) */
  def toHSL: HSL =
    HSL(h, s, l)

  /** Convert to RGB (drops alpha) */
  def toRGB: RGB =
    toRGBA.toRGB

  /** Convert to RGBA */
  def toRGBA: RGBA =
    val c      = (1.0 - Math.abs(2.0 * l - 1.0)) * s
    val hPrime = ((h % 360) + 360) % 360 / 60.0
    val x      = c * (1.0 - Math.abs((hPrime % 2) - 1.0))
    val m      = l - c / 2.0
    val (r1, g1, b1) =
      if hPrime < 1 then (c, x, 0.0)
      else if hPrime < 2 then (x, c, 0.0)
      else if hPrime < 3 then (0.0, c, x)
      else if hPrime < 4 then (0.0, x, c)
      else if hPrime < 5 then (x, 0.0, c)
      else (c, 0.0, x)
    RGBA(r1 + m, g1 + m, b1 + m, a)

  /** CSS hsla string */
  def toCSSValue: String =
    def format(d: Double): String =
      Math.round(d).toString()
    s"hsla(${format(h)}, ${format(s * 100)}%, ${format(l * 100)}%, ${format(a * 100)}%)"

object HSLA:

  val Red: HSLA     = HSLA(0, 1.0, 0.5, 1.0)
  val Green: HSLA   = HSLA(120, 1.0, 0.5, 1.0)
  val Blue: HSLA    = HSLA(240, 1.0, 0.5, 1.0)
  val Yellow: HSLA  = HSLA(60, 1.0, 0.5, 1.0)
  val Magenta: HSLA = HSLA(300, 1.0, 0.5, 1.0)
  val Cyan: HSLA    = HSLA(180, 1.0, 0.5, 1.0)
  val White: HSLA   = HSLA(0, 0.0, 1.0, 1.0)
  val Black: HSLA   = HSLA(0, 0.0, 0.0, 1.0)
  val Zero: HSLA    = HSLA(0, 0.0, 0.0, 0.0)

  /** Create an opaque HSLA color */
  def apply(h: Double, s: Double, l: Double): HSLA =
    HSLA(h, s, l, 1.0)

  /** Create HSLA from RGB with full opacity */
  def fromRGB(rgb: RGB): HSLA =
    val hsl = HSL.fromRGB(rgb)
    HSLA(hsl.h, hsl.s, hsl.l, 1.0)

  /** Create HSLA from RGBA */
  def fromRGBA(rgba: RGBA): HSLA =
    val hsl = HSL.fromRGB(rgba.toRGB)
    HSLA(hsl.h, hsl.s, hsl.l, rgba.a)
