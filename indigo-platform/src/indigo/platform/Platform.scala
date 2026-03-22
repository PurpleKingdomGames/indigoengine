package indigo.platform

import indigo.core.Outcome
import indigo.core.events.GlobalEvent
import indigo.platform.assets.AssetCollection
import indigo.render.Renderer
import indigo.render.pipeline.assets.AssetMapping
import indigo.shaders.RawShaderCode
import indigoengine.shared.collections.Batch

/** Platform abstraction for game engine runtime services.
  */
trait Platform:

  /** Initialise the platform.
    */
  def initialise(
      shaders: Set[RawShaderCode],
      assetCollection: AssetCollection
  ): Outcome[(Renderer, AssetMapping)]

  // /** Request the next animation frame
  //   *
  //   * @param loop
  //   *   The callback to invoke on the next frame, receiving the timestamp
  //   */
  // def tick(loop: Double => Unit): Unit

  // /** Schedule a delayed execution
  //   *
  //   * @param amount
  //   *   The delay in milliseconds
  //   * @param thunk
  //   *   The callback to invoke after the delay
  //   */
  // def delay(amount: Double, thunk: () => Unit): Unit

  /** Shutdown the platform and release resources */
  def kill(): Unit

  /** Push an event to the global event stream
    *
    * @param event
    *   The event to push
    */
  def pushGlobalEvent(event: GlobalEvent): Unit

  /** Register an event callback that will be called for all events this frame. There is only one global callback. */
  def registerEventCallback(cb: GlobalEvent => Unit): Unit

  /** Clear the event callback. */
  def clearEventCallback(): Unit

  /** Collect all pending events from the global event stream
    *
    * @return
    *   A batch of pending events
    */
  def collectEvents: Batch[GlobalEvent]
