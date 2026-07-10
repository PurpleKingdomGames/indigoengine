package indigo.core.render

/** Represents the amount to magnify / scale the pixels by. Automatically clamped to a range of 1 to 16, typical useage
  * is <= x4.
  */
opaque type Magnification = Int

object Magnification:

  private val _min: Int = 1
  private val _max: Int = 16

  lazy val x1: Magnification =
    Magnification(_min)
  lazy val x2: Magnification =
    Magnification(2)
  lazy val x3: Magnification =
    Magnification(3)
  lazy val x4: Magnification =
    Magnification(4)
  lazy val default: Magnification =
    Magnification.x1

  lazy val Max: Magnification =
    Magnification(_max)
  lazy val Min: Magnification =
    Magnification.x1

  def apply(value: Int): Magnification =
    clampToRange(value)

  private def clampToRange(value: Int): Magnification =
    Math.min(_max, Math.max(_min, value))

  extension (m: Magnification)
    def increase: Magnification = clampToRange(m.toInt + 1)
    def decrease: Magnification = clampToRange(m.toInt - 1)
    def toInt: Int              = m
