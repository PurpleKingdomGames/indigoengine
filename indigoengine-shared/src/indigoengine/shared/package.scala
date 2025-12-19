package indigoengine.shared

object aliases:

  // Time

  type Millis = indigoengine.shared.datatypes.Millis
  val Millis: indigoengine.shared.datatypes.Millis.type = indigoengine.shared.datatypes.Millis

  type Seconds = indigoengine.shared.datatypes.Seconds
  val Seconds: indigoengine.shared.datatypes.Seconds.type = indigoengine.shared.datatypes.Seconds

  // Collections

  type Batch[A] = indigoengine.shared.collections.Batch[A]
  val Batch: indigoengine.shared.collections.Batch.type = indigoengine.shared.collections.Batch

  type NonEmptyBatch[A] = indigoengine.shared.collections.NonEmptyBatch[A]
  val NonEmptyBatch: indigoengine.shared.collections.NonEmptyBatch.type = indigoengine.shared.collections.NonEmptyBatch

  type NonEmptyList[A] = indigoengine.shared.collections.NonEmptyList[A]
  val NonEmptyList: indigoengine.shared.collections.NonEmptyList.type = indigoengine.shared.collections.NonEmptyList

  // Datatypes

  type Degrees = indigoengine.shared.datatypes.Degrees
  val Degrees: indigoengine.shared.datatypes.Degrees.type = indigoengine.shared.datatypes.Degrees

  type Radians = indigoengine.shared.datatypes.Radians
  val Radians: indigoengine.shared.datatypes.Radians.type = indigoengine.shared.datatypes.Radians

  type RGB = indigoengine.shared.datatypes.RGB
  val RGB: indigoengine.shared.datatypes.RGB.type = indigoengine.shared.datatypes.RGB

  type RGBA = indigoengine.shared.datatypes.RGBA
  val RGBA: indigoengine.shared.datatypes.RGBA.type = indigoengine.shared.datatypes.RGBA

  // Optics

  type Lens[A, B] = indigoengine.shared.optics.Lens[A, B]
  val Lens: indigoengine.shared.optics.Lens.type = indigoengine.shared.optics.Lens

  type Iso[A, B] = indigoengine.shared.optics.Iso[A, B]
  val Iso: indigoengine.shared.optics.Iso.type = indigoengine.shared.optics.Iso
