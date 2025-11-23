package indigoengine.shared

import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.NonEmptyBatch
import indigoengine.shared.collections.NonEmptyList

object syntax:

  def ==: = Batch.==:
  def :== = Batch.:==

  extension [Value](values: Option[Value])
    def toBatch: Batch[Value] = Batch.fromOption(values)
    // def toOutcome[Event](error: => Throwable): Outcome[Value, Event] = Outcome.fromOption(values, error)

  // extension [Value, Event](b: Batch[Outcome[Value, Event]])
  //   def sequence: Outcome[Batch[Value], Event] = Outcome.sequenceBatch(b)
  // extension [Value, Event](l: List[Outcome[Value, Event]])
  //   def sequence: Outcome[List[Value], Event]                                   = Outcome.sequenceList(l)
  extension [Value](b: Batch[Option[Value]]) def sequence: Option[Batch[Value]] = Batch.sequenceOption(b)
  extension [Value](l: List[Option[Value]]) def sequence: Option[List[Value]]   = Batch.sequenceListOption(l)

  // extension [Value, Event](b: NonEmptyBatch[Outcome[Value, Event]])
  //   def sequence: Outcome[NonEmptyBatch[Value], Event] = Outcome.sequenceNonEmptyBatch(b)
  // extension [Value, Event](l: NonEmptyList[Outcome[Value, Event]])
  //   def sequence: Outcome[NonEmptyList[Value], Event] = Outcome.sequenceNonEmptyList(l)

  extension [Value](b: NonEmptyBatch[Option[Value]])
    def sequence: Option[NonEmptyBatch[Value]] = NonEmptyBatch.sequenceOption(b)
  extension [Value](l: NonEmptyList[Option[Value]])
    def sequence: Option[NonEmptyList[Value]] = NonEmptyList.sequenceOption(l)

  extension [Value](values: scalajs.js.Array[Value]) def toBatch: Batch[Value] = Batch.fromJSArray(values)
  extension [Value](values: Array[Value]) def toBatch: Batch[Value]            = Batch.fromArray(values)
  extension [Value](values: List[Value]) def toBatch: Batch[Value]             = Batch.fromList(values)
  extension [Value](values: Set[Value]) def toBatch: Batch[Value]              = Batch.fromSet(values)
  extension [Value](values: Seq[Value]) def toBatch: Batch[Value]              = Batch.fromSeq(values)
  extension [Value](values: IndexedSeq[Value]) def toBatch: Batch[Value]       = Batch.fromIndexedSeq(values)
  extension [Value](values: Iterator[Value]) def toBatch: Batch[Value]         = Batch.fromIterator(values)
  extension [K, V](values: Map[K, V]) def toBatch: Batch[(K, V)]               = Batch.fromMap(values)
  extension (values: Range) def toBatch: Batch[Int]                            = Batch.fromRange(values)
