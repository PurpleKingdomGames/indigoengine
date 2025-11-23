package indigo.shared.animation

import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.NonEmptyBatch
import indigoengine.shared.datatypes.Millis

final case class Animation(
    animationKey: AnimationKey,
    currentCycleLabel: CycleLabel,
    cycles: NonEmptyBatch[Cycle]
) derives CanEqual {

  def addCycle(cycle: Cycle): Animation =
    this.copy(cycles = cycle :: cycles)

  def withAnimationKey(animationKey: AnimationKey): Animation =
    this.copy(animationKey = animationKey)

}

object Animation {

  def apply(
      animationKey: AnimationKey,
      frameOne: Frame,
      frames: Batch[Frame]
  ): Animation =
    Animation(
      animationKey,
      CycleLabel("default"),
      NonEmptyBatch(Cycle(CycleLabel("default"), NonEmptyBatch(frameOne, frames), 0, Millis.zero))
    )

  def apply(
      animationKey: AnimationKey,
      frameOne: Frame,
      frames: Frame*
  ): Animation =
    Animation(animationKey, frameOne, Batch.fromSeq(frames))

  def create(animationKey: AnimationKey, cycle: Cycle): Animation =
    apply(animationKey, cycle.label, NonEmptyBatch(cycle))

}
