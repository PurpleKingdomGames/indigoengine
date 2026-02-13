package indigoengine.shared

import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.NonEmptyBatch
import indigoengine.shared.collections.NonEmptyList
import indigoengine.shared.datatypes.*

object syntaxshared:

  def ==: = Batch.==:
  def :== = Batch.:==

  extension [Value](values: Option[Value]) def toBatch: Batch[Value] = Batch.fromOption(values)

  extension [Value](b: Batch[Option[Value]]) def sequence: Option[Batch[Value]] = Batch.sequenceOption(b)
  extension [Value](l: List[Option[Value]]) def sequence: Option[List[Value]]   = Batch.sequenceListOption(l)

  extension [Value](b: NonEmptyBatch[Option[Value]])
    def sequence: Option[NonEmptyBatch[Value]] = NonEmptyBatch.sequenceOption(b)
  extension [Value](l: NonEmptyList[Option[Value]])
    def sequence: Option[NonEmptyList[Value]] = NonEmptyList.sequenceOption(l)

  extension [K, V](values: Map[K, V]) def toBatch: Batch[(K, V)] = Batch.fromMap(values)
  extension (values: Range) def toBatch: Batch[Int]              = Batch.fromRange(values)

  extension (d: Double)
    def toRadians: Radians = Radians(d)
    def radians: Radians   = Radians(d)
    def toSeconds: Seconds = Seconds(d)
    def second: Seconds    = Seconds(d)
    def seconds: Seconds   = Seconds(d)

  extension (i: Int)
    def toMillis: Millis = Millis(i)
    def millis: Millis   = Millis(i)

  extension (l: Long)
    def toMillis: Millis = Millis(l)
    def millis: Millis   = Millis(l)

  extension (t: (Double, Double, Double)) def toRGB: RGB = RGB(t._1, t._2, t._3)

  extension (t: (Double, Double, Double, Double)) def toRGBA: RGBA = RGBA(t._1, t._2, t._3, t._4)
