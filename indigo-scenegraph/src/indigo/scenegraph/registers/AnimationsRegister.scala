package indigo.scenegraph.registers

import indigo.core.animation.Animation
import indigo.core.animation.AnimationAction
import indigo.core.animation.AnimationKey
import indigo.core.animation.AnimationMemento
import indigo.core.animation.AnimationRef
import indigo.core.datatypes.BindingKey
import indigo.core.time.GameTime
import indigoengine.shared.collections.Batch

final class AnimationsRegister:

  private val animationRegistry: scalajs.js.Dictionary[AnimationRef]   = scalajs.js.Dictionary.empty
  private val animationStates: scalajs.js.Dictionary[AnimationMemento] = scalajs.js.Dictionary.empty

  def kill(): Unit =
    animationRegistry.clear()
    animationStates.clear()
    ()

  def register(animation: Animation): Unit =
    animationRegistry.update(animation.animationKey.toString, AnimationRef.fromAnimation(animation))

  def findByAnimationKey(animationKey: AnimationKey): Option[AnimationRef] =
    animationRegistry.get(animationKey.toString)

  def findMementoByBindingKey(key: BindingKey): Option[AnimationMemento] =
    animationStates.get(key.toString)

  def fetchAnimationForSprite(
      gameTime: GameTime,
      bindingKey: BindingKey,
      animationKey: AnimationKey,
      animationActions: Batch[AnimationAction]
  ): Option[AnimationRef] =
    fetchAnimationInLastState(bindingKey, animationKey).map { anim =>
      val newAnim = anim.runActions(animationActions, gameTime)

      animationStates.update(bindingKey.toString, newAnim.saveMemento(bindingKey))

      newAnim
    }

  def fetchAnimationInLastState(bindingKey: BindingKey, animationKey: AnimationKey): Option[AnimationRef] =
    findByAnimationKey(animationKey)
      .map { anim =>
        findMementoByBindingKey(bindingKey)
          .map(m => anim.applyMemento(m))
          .getOrElse(anim)
      }
