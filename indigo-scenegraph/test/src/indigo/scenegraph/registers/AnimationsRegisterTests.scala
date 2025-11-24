package indigo.scenegraph.registers

import indigo.core.animation.Animation
import indigo.core.animation.AnimationAction.ChangeCycle
import indigo.core.animation.AnimationAction.Play
import indigo.core.animation.AnimationKey
import indigo.core.animation.AnimationMemento
import indigo.core.animation.Cycle
import indigo.core.animation.CycleLabel
import indigo.core.animation.Frame
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.NonEmptyBatch
import indigo.core.datatypes.BindingKey
import indigo.core.datatypes.Point
import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.Size
import indigo.core.time.GameTime
import indigoengine.shared.datatypes.Millis
import indigoengine.shared.datatypes.Seconds

import scala.annotation.nowarn

@nowarn("msg=unused")
class AnimationsRegisterTests extends munit.FunSuite {

  given CanEqual[Option[AnimationMemento], Option[AnimationMemento]] = CanEqual.derived

  test("can look up an animation by key") {
    val register = new AnimationsRegister()
    register.register(AnimationSample.animation)

    val actual = register.findByAnimationKey(AnimationSample.key)

    assertEquals(actual.isDefined, true)
    assertEquals(actual.get.animationKey, AnimationSample.key)

    assertEquals(register.findByAnimationKey(AnimationKey("nope")).isEmpty, true)
  }

  test("can look up a memento by key") {

    val bindingKey = BindingKey("sprite 1")

    val register = new AnimationsRegister()
    register.register(AnimationSample.animation)

    assertEquals(register.findMementoByBindingKey(bindingKey).isEmpty, true)

    register.fetchAnimationForSprite(
      GameTime.is(Seconds(0)),
      bindingKey,
      AnimationSample.key,
      Batch.empty
    )

    val actual = register.findMementoByBindingKey(bindingKey)

    assertEquals(actual.isDefined, true)
    assertEquals(actual.get.bindingKey, bindingKey)
    assertEquals(actual.get.currentCycleLabel, AnimationSample.cycleLabel1)
    assertEquals(actual.get.currentCycleMemento.playheadPosition, 0)
    assertEquals(actual.get.currentCycleMemento.lastFrameAdvance, Millis.zero)
  }

  test("can apply and store animations mementos") {

    val bindingKey = BindingKey("sprite 1")

    // Fetch default animation
    val register = new AnimationsRegister()
    register.register(AnimationSample.animation)

    // ------------
    // Round 1 - control, do nothing.
    val updatedAnim1 = register.fetchAnimationForSprite(
      GameTime.is(Seconds(0)),
      bindingKey,
      AnimationSample.key,
      Batch.empty
    )

    assertEquals(updatedAnim1.isDefined, true)
    assertEquals(updatedAnim1.get.animationKey, AnimationSample.key)
    assertEquals(updatedAnim1.get.currentCycle.label, AnimationSample.cycleLabel1)
    assertEquals(updatedAnim1.get.currentCycle.playheadPosition, 0)
    assertEquals(updatedAnim1.get.currentCycle.lastFrameAdvance, Millis.zero)

    val memento1 = register.findMementoByBindingKey(bindingKey)

    assertEquals(memento1.isDefined, true)
    assertEquals(memento1.get.bindingKey, bindingKey)
    assertEquals(memento1.get.currentCycleLabel, AnimationSample.cycleLabel1)
    assertEquals(memento1.get.currentCycleMemento.playheadPosition, 0)
    assertEquals(memento1.get.currentCycleMemento.lastFrameAdvance, Millis.zero)
    // ------------

    // ------------
    // Round 2
    val updatedAnim2 = register.fetchAnimationForSprite(
      GameTime.is(Millis(100).toSeconds),
      bindingKey,
      AnimationSample.key,
      Batch(ChangeCycle(AnimationSample.cycleLabel2), Play)
    )

    assertEquals(updatedAnim2.isDefined, true)
    assertEquals(updatedAnim2.get.animationKey, AnimationSample.key)
    assertEquals(updatedAnim2.get.currentCycle.label, AnimationSample.cycleLabel2)
    assertEquals(updatedAnim2.get.currentCycle.playheadPosition, 1)
    assertEquals(updatedAnim2.get.currentCycle.lastFrameAdvance, Millis(100))

    val memento2 = register.findMementoByBindingKey(bindingKey)

    assertEquals(memento2.isDefined, true)
    assertEquals(memento2.get.bindingKey, bindingKey)
    assertEquals(memento2.get.currentCycleLabel, AnimationSample.cycleLabel2)
    assertEquals(memento2.get.currentCycleMemento.playheadPosition, 1)
    assertEquals(memento2.get.currentCycleMemento.lastFrameAdvance, Millis(100))
    // ------------

    // ------------
    // Round 3
    val updatedAnim3 = register.fetchAnimationForSprite(
      GameTime.is(Millis(200).toSeconds),
      bindingKey,
      AnimationSample.key,
      Batch(Play)
    )

    assertEquals(updatedAnim3.isDefined, true)
    assertEquals(updatedAnim3.get.animationKey, AnimationSample.key)
    assertEquals(updatedAnim3.get.currentCycle.label, AnimationSample.cycleLabel2)
    assertEquals(updatedAnim3.get.currentCycle.playheadPosition, 2)
    assertEquals(updatedAnim3.get.currentCycle.lastFrameAdvance, Millis(200))

    val memento3 = register.findMementoByBindingKey(bindingKey)

    assertEquals(memento3.isDefined, true)
    assertEquals(memento3.get.bindingKey, bindingKey)
    assertEquals(memento3.get.currentCycleLabel, AnimationSample.cycleLabel2)
    assertEquals(memento3.get.currentCycleMemento.playheadPosition, 2)
    assertEquals(memento3.get.currentCycleMemento.lastFrameAdvance, Millis(200))
    // ------------

    // ------------
    // Round 4
    val updatedAnim4 = register.fetchAnimationForSprite(
      GameTime.is(Millis(400).toSeconds),
      bindingKey,
      AnimationSample.key,
      Batch(Play)
    )

    assertEquals(updatedAnim4.isDefined, true)
    assertEquals(updatedAnim4.get.animationKey, AnimationSample.key)
    assertEquals(updatedAnim4.get.currentCycle.label, AnimationSample.cycleLabel2)
    assertEquals(updatedAnim4.get.currentCycle.playheadPosition, 1)
    assertEquals(updatedAnim4.get.currentCycle.lastFrameAdvance, Millis(400))

    val memento4 = register.findMementoByBindingKey(bindingKey)

    assertEquals(memento4.isDefined, true)
    assertEquals(memento4.get.bindingKey, bindingKey)
    assertEquals(memento4.get.currentCycleLabel, AnimationSample.cycleLabel2)
    assertEquals(memento4.get.currentCycleMemento.playheadPosition, 1)
    assertEquals(memento4.get.currentCycleMemento.lastFrameAdvance, Millis(400))
    // ------------

    assertEquals(memento1 == memento2, false)
    assertEquals(memento2 == memento3, false)
    assertEquals(memento3 == memento4, false)

  }

}

object AnimationSample {

  val frame1: Frame =
    Frame(Rectangle(Point(0, 0), Size(10, 10)), Millis(100))

  val frame2: Frame =
    Frame(Rectangle(0, 0, 20, 10), Millis(100))

  val frame3: Frame =
    Frame(Rectangle(0, 0, 30, 10), Millis(100))

  val frame4: Frame =
    Frame(Rectangle(0, 0, 40, 10), Millis(100))

  val frame5: Frame =
    Frame(Rectangle(0, 0, 50, 10), Millis(100))

  val frame6: Frame =
    Frame(Rectangle(0, 0, 60, 10), Millis(100))

  val cycleLabel1: CycleLabel =
    CycleLabel("cycle 1")

  val cycleLabel2: CycleLabel =
    CycleLabel("cycle 2")

  val cycle1: Cycle =
    Cycle.create(cycleLabel1.toString, NonEmptyBatch(frame1, frame2, frame3))

  val cycle2: Cycle =
    Cycle.create(cycleLabel2.toString, NonEmptyBatch(frame4, frame5, frame6))

  val cycles: NonEmptyBatch[Cycle] =
    NonEmptyBatch(cycle1, cycle2)

  val key: AnimationKey =
    AnimationKey("test anim")

  val animation: Animation =
    Animation(
      key,
      cycles.head.label,
      cycles
    )

}
