package indigo.platform.api

import indigo.core.events.GlobalEvent
import indigoengine.shared.collections.Batch

/** Platform abstraction for game engine runtime services.
  *
  * This trait defines the core platform capabilities required by the game engine:
  *   - Animation frame timing (tick)
  *   - Delayed execution (delay)
  *   - Lifecycle management (kill)
  *   - Event handling (pushGlobalEvent, collectEvents)
  */
trait Platform:
  /** Request the next animation frame
    *
    * @param loop
    *   The callback to invoke on the next frame, receiving the timestamp
    */
  def tick(loop: Double => Unit): Unit

  /** Schedule a delayed execution
    *
    * @param amount
    *   The delay in milliseconds
    * @param thunk
    *   The callback to invoke after the delay
    */
  def delay(amount: Double, thunk: () => Unit): Unit

  /** Shutdown the platform and release resources */
  def kill(): Unit

  /** Push an event to the global event stream
    *
    * @param event
    *   The event to push
    */
  def pushGlobalEvent(event: GlobalEvent): Unit

  /** Collect all pending events from the global event stream
    *
    * @return
    *   A batch of pending events
    */
  def collectEvents: Batch[GlobalEvent]

/** Platform abstraction for fullscreen management */
trait PlatformFullScreen:
  /** Toggle fullscreen mode */
  def toggleFullScreen(): Unit

  /** Enter fullscreen mode */
  def enterFullScreen(): Unit

  /** Exit fullscreen mode */
  def exitFullScreen(): Unit
