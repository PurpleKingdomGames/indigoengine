package indigo.core.animation

import indigo.core.dice.Dice

opaque type AnimationKey = String

object AnimationKey:
  inline def apply(key: String): AnimationKey = key
  inline def fromDice(dice: Dice): AnimationKey =
    dice.rollAlphaNumeric

  extension (a: AnimationKey) inline def show: String = a
