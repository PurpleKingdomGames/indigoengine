package tyrian.runtime

import indigoengine.sdl.facades.sdl.SDL.*
import indigoengine.shared.datatypes.Millis
import indigoengine.shared.datatypes.Seconds
import tyrian.SDLContext
import tyrian.SDLMsg

import java.util.concurrent.atomic.AtomicReference
import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

final class SDLRuntime private (val ctx: SDLContext, listeners: SDLEventListeners):

  type Handle = Long

  def addSDLEventListener(handle: SDLEventListenerHandle)(callback: SDLMsg => Unit): SDLEventListenerHandle =
    listeners.addSDLEventListener(handle)(callback)

  def removeSDLEventListener(handle: SDLEventListenerHandle): Unit =
    listeners.removeSDLEventListener(handle)

  @SuppressWarnings(
    Array(
      "scalafix:DisableSyntax.var",
      "scalafix:DisableSyntax.while",
      "scalafix:DisableSyntax.null"
    )
  )
  def run(perTick: (SDLContext, Seconds) => Unit): Unit =
    val startNanos = System.nanoTime()
    val event      = stackalloc[SDL_Event]()
    var running    = true

    while running do
      val runningTime        = Millis((System.nanoTime() - startNanos) / 1_000_000L)
      val runningTimeSeconds = runningTime.toSeconds

      while SDL_PollEvent(event) != 0 do
        val rawEventType = event.asInstanceOf[Ptr[CStruct1[UInt]]]._1
        val msg: SDLMsg  = SDLMsg.fromSDLEvent(rawEventType)

        if msg == SDLMsg.Quit then running = false

        listeners.dispatch(msg)

      listeners.dispatch(SDLMsg.Frame(runningTimeSeconds))

      perTick(ctx, runningTimeSeconds)

      // val fit = frameHooks.values().iterator()
      // while fit.hasNext() do
      //   fit.next()(ctx, timeMillis)

      val _ = SDL_GL_SwapWindow(ctx.window)
      SDL_Delay(16.toUInt)

object SDLRuntime:

  // TODO: Makes a singleton, pattern could use review.
  val current: AtomicReference[SDLRuntime] =
    new AtomicReference()

  def create(title: String, width: Int, height: Int): SDLRuntime =
    new SDLRuntime(
      SDLContext.create(title, width, height),
      new SDLEventListeners()
    )
