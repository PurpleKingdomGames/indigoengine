package indigo.core.time

import indigoengine.shared.datatypes.Seconds

/** An instance of `GameTime` is present on every frame, and the values it holds do not change during that frame. This
  * allows for "synchronous" programming, where it is assumed that everything happens at the exact same time during the
  * current frame. The most commonly used fields (e.g. for animation) are the running time of the game and the time
  * delta since the last frame.
  */
final case class GameTime(running: Seconds, delta: Seconds) derives CanEqual

object GameTime:

  val zero: GameTime =
    GameTime(Seconds.zero, Seconds.zero)

  def is(running: Seconds): GameTime =
    GameTime(running, Seconds.zero)

  def withDelta(running: Seconds, delta: Seconds): GameTime =
    GameTime(running, delta)
