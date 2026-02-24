package indigoengine.shared

import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.NonEmptyBatch
import indigoengine.shared.collections.NonEmptyList
import indigoengine.shared.datatypes.*

object syntax:

  extension [Value](values: Vector[Value]) def toBatch: Batch[Value]     = Batch.fromVector(values)
  extension [Value](values: List[Value]) def toBatch: Batch[Value]       = Batch.fromList(values)
  extension [Value](values: Set[Value]) def toBatch: Batch[Value]        = Batch.fromSet(values)
  extension [Value](values: Seq[Value]) def toBatch: Batch[Value]        = Batch.fromSeq(values)
  extension [Value](values: IndexedSeq[Value]) def toBatch: Batch[Value] = Batch.fromIndexedSeq(values)
  extension [Value](values: Iterator[Value]) def toBatch: Batch[Value]   = Batch.fromIterator(values)

  export syntaxshared.*
