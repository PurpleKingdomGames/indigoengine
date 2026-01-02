package tyrian.next

// --- Collections ---
// TODO: should use exported aliases from indigoengine package.

type Batch[A] = indigoengine.shared.collections.Batch[A]
val Batch: indigoengine.shared.collections.Batch.type = indigoengine.shared.collections.Batch

type NonEmptyBatch[A] = indigoengine.shared.collections.NonEmptyBatch[A]
val NonEmptyBatch: indigoengine.shared.collections.NonEmptyBatch.type = indigoengine.shared.collections.NonEmptyBatch

type NonEmptyList[A] = indigoengine.shared.collections.NonEmptyList[A]
val NonEmptyList: indigoengine.shared.collections.NonEmptyList.type = indigoengine.shared.collections.NonEmptyList

