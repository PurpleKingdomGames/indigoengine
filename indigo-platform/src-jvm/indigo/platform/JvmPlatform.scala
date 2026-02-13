package indigo.platform

import indigo.core.events.GlobalEvent
import indigo.platform.api.Platform
import indigo.platform.api.PlatformFullScreen
import indigoengine.shared.collections.Batch

/** No-op JVM Platform implementation stub.
  *
  * This is a placeholder implementation to validate that the platform abstraction architecture compiles on the JVM. All
  * methods are no-ops and this should not be used for actual game execution.
  */
class JvmPlatform extends Platform with PlatformFullScreen:

  def tick(loop: Double => Unit): Unit = ()

  def delay(amount: Double, thunk: () => Unit): Unit = ()

  def kill(): Unit = ()

  def pushGlobalEvent(event: GlobalEvent): Unit = ()

  def collectEvents: Batch[GlobalEvent] = Batch.empty

  def toggleFullScreen(): Unit = ()

  def enterFullScreen(): Unit = ()

  def exitFullScreen(): Unit = ()
