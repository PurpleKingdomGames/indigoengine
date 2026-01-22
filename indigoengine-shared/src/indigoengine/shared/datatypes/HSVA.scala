package indigoengine.shared.datatypes

/** Represents a color in HSVA color space with alpha channel.
  */
final case class HSVA(h: Double, s: Double, v: Double, a: Double) derives CanEqual:

  /** Copy with a new hue component */
  def withHue(newHue: Double): HSVA =
    this.copy(h = newHue)

  /** Copy with a new saturation component */
  def withSaturation(newSaturation: Double): HSVA =
    this.copy(s = newSaturation)

  /** Copy with a new value component */
  def withValue(newValue: Double): HSVA =
    this.copy(v = newValue)

  /** Copy with a new alpha component */
  def withAlpha(newAlpha: Double): HSVA =
    this.copy(a = newAlpha)

  /** Return the color with full opacity */
  def makeOpaque: HSVA =
    this.copy(a = 1.0)

  /** Return the color fully transparent */
  def makeTransparent: HSVA =
    this.copy(a = 0.0)

  /** Rotate hue by degrees on color wheel (positive = clockwise) */
  def rotateHue(degrees: Degrees): HSVA =
    this.copy(h = (Degrees(h) + degrees).wrap.toDouble)

  /** Brighten by amount (0.0-1.0), increasing value */
  def brighten(amount: Double): HSVA =
    this.copy(v = Math.min(1.0, v + amount))

  /** Darken by amount (0.0-1.0), decreasing value */
  def darken(amount: Double): HSVA =
    this.copy(v = Math.max(0.0, v - amount))

  /** Saturate by amount (0.0-1.0), increasing saturation */
  def saturate(amount: Double): HSVA =
    this.copy(s = Math.min(1.0, s + amount))

  /** Desaturate by amount (0.0-1.0), decreasing saturation */
  def desaturate(amount: Double): HSVA =
    this.copy(s = Math.max(0.0, s - amount))

  /** Convert to HSV (drops alpha) */
  def toHSV: HSV =
    HSV(h, s, v)

  /** Convert to RGB (drops alpha) */
  def toRGB: RGB =
    toRGBA.toRGB

  /** Convert to RGBA */
  def toRGBA: RGBA =
    val c      = v * s
    val hPrime = ((h % 360) + 360) % 360 / 60.0
    val x      = c * (1.0 - Math.abs((hPrime % 2) - 1.0))
    val m      = v - c
    val (r1, g1, b1) =
      if hPrime < 1 then (c, x, 0.0)
      else if hPrime < 2 then (x, c, 0.0)
      else if hPrime < 3 then (0.0, c, x)
      else if hPrime < 4 then (0.0, x, c)
      else if hPrime < 5 then (x, 0.0, c)
      else (c, 0.0, x)
    RGBA(r1 + m, g1 + m, b1 + m, a)

object HSVA:

  val Red: HSVA     = HSVA(0, 1.0, 1.0, 1.0)
  val Green: HSVA   = HSVA(120, 1.0, 1.0, 1.0)
  val Blue: HSVA    = HSVA(240, 1.0, 1.0, 1.0)
  val Yellow: HSVA  = HSVA(60, 1.0, 1.0, 1.0)
  val Magenta: HSVA = HSVA(300, 1.0, 1.0, 1.0)
  val Cyan: HSVA    = HSVA(180, 1.0, 1.0, 1.0)
  val White: HSVA   = HSVA(0, 0.0, 1.0, 1.0)
  val Black: HSVA   = HSVA(0, 0.0, 0.0, 1.0)
  val Zero: HSVA    = HSVA(0, 0.0, 0.0, 0.0)

  /** Create an opaque HSVA color */
  def apply(h: Double, s: Double, v: Double): HSVA =
    HSVA(h, s, v, 1.0)

  /** Create HSVA from RGB with full opacity */
  def fromRGB(rgb: RGB): HSVA =
    val hsv = HSV.fromRGB(rgb)
    HSVA(hsv.h, hsv.s, hsv.v, 1.0)

  /** Create HSVA from RGBA */
  def fromRGBA(rgba: RGBA): HSVA =
    val hsv = HSV.fromRGB(rgba.toRGB)
    HSVA(hsv.h, hsv.s, hsv.v, rgba.a)
