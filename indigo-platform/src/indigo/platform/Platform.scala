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
trait Platform[Ctx]:

  /** Initialise the platform.
    */
  def initialise(
      context: Ctx,
      shaders: Set[RawShaderCode],
      assetCollection: AssetCollection
  ): Outcome[(Renderer[Ctx], AssetMapping)]

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
