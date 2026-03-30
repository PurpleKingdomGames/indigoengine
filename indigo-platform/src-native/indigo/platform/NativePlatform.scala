package indigo.platform

import indigo.core.events.GlobalEvent
import indigoengine.shared.collections.Batch

/** No-op Native Platform implementation stub.
  *
  * This is a placeholder implementation to validate that the platform abstraction architecture compiles against native
  * platforms. All methods are no-ops and this should not be used for actual game execution.
  */
class NativePlatform extends Platform with PlatformFullScreen:

  def tick(loop: Double => Unit): Unit =
    ()

  def delay(amount: Double, thunk: () => Unit): Unit =
    ()

  def kill(): Unit =
    ()

  def pushGlobalEvent(event: GlobalEvent): Unit =
    ()

  def registerEventCallback(cb: GlobalEvent => Unit): Unit =
    ()

  def clearEventCallback(): Unit =
    ()

  def collectEvents: Batch[GlobalEvent] = Batch.empty

  def toggleFullScreen(): Unit = ()

  def enterFullScreen(): Unit = ()

  def exitFullScreen(): Unit = ()
