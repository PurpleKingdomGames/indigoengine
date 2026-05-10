package tyrian.sdl

import java.util.concurrent.ConcurrentLinkedQueue

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

import cats.effect.IO
import cats.effect.std.Dispatcher
import cats.effect.unsafe.implicits.global

import indigoengine.shared.collections.Batch

import tyrian.GlobalMsg
import tyrian.Watcher
import tyrian.extensions.Extension
import tyrian.platform.Cmd
import tyrian.platform.Sub
import tyrian.platform.runtime.CmdHelper
import tyrian.platform.runtime.SubHelper
import tyrian.sdl.facades.sdl.SDL.*
import tyrian.sdl.facades.sdl.SDLConstants.*

/** Minimal SDL3 + OpenGL launcher trait for Scala Native, with cats-effect on the side.
  *
  * The JVM main thread owns SDL init, the event-poll/update/render loop, and shutdown. A `Dispatcher[IO]` is acquired
  * at startup so the user's `Cmd`s and `Watcher` subscriptions can run on cats-effect's compute pool without main ever
  * blocking on IO. Messages from CE-side workers are pushed into a thread-safe `ConcurrentLinkedQueue` which the main
  * loop drains each frame.
  *
  * Not auto-starting: SDL is only initialised because the user has chosen to extend `SdlApp`.
  */
trait SdlApp[Model]:

  def title: String = "Tyrian SDL"
  def width: Int    = 400
  def height: Int   = 400

  /** Build the initial model and any startup commands. */
  def init(args: Array[String]): (Model, Cmd[IO, GlobalMsg])

  /** Fold a message into the model and emit follow-up commands. Runs on the main thread. */
  def update(model: Model): GlobalMsg => (Model, Cmd[IO, GlobalMsg])

  /** Issue GL calls for the current frame. Runs on the main thread, with the GL context current. */
  def render(model: Model, ctx: SdlContext): Unit

  /** Long-running message sources (timers, sockets, etc.). Subscribed once at startup based on the initial model. */
  def watchers(@scala.annotation.unused model: Model): Batch[Watcher] = Batch.empty[Watcher]

  /** Extensions receive the live `SdlContext` so users can wire it into extension constructors when they need GL
    * access. v2 only collects the registered set; it does not yet drive their lifecycle.
    */
  def extensions(
      @scala.annotation.unused args: Array[String],
      @scala.annotation.unused model: Model,
      @scala.annotation.unused ctx: SdlContext
  ): Set[Extension] = Set.empty

  @SuppressWarnings(
    Array(
      "scalafix:DisableSyntax.var",
      "scalafix:DisableSyntax.while",
      "scalafix:DisableSyntax.null"
    )
  )
  final def main(args: Array[String]): Unit =
    val ctx = SdlContext.create(title, width, height)

    val msgQueue = new ConcurrentLinkedQueue[GlobalMsg]()

    val (dispatcher, releaseDispatcher) =
      Dispatcher.parallel[IO].allocated.unsafeRunSync()

    def runCmd(cmd: Cmd[IO, GlobalMsg]): Unit =
      CmdHelper.cmdToTaskList[IO, GlobalMsg](cmd).foreach { task =>
        dispatcher.unsafeRunAndForget(
          task.flatMap {
            case Some(m) => IO(msgQueue.add(m)).void
            case scala.None => IO.unit
          }
        )
      }

    val (initModel, initCmd) = init(args)
    var model: Model         = initModel

    val _registered: Set[Extension] = extensions(args, model, ctx)
    val _                           = _registered

    runCmd(initCmd)

    val initialSub: Sub[IO, GlobalMsg] =
      Watcher.internal.Many(watchers(initModel)).toSub
      
    SubHelper.flatten(initialSub).foreach { obs =>
      dispatcher.unsafeRunAndForget(
        SubHelper
          .runObserve[IO, Any, GlobalMsg](obs.asInstanceOf[Sub.Observe[IO, Any, GlobalMsg]]) { result =>
            result.toOption.flatten.foreach(m => { val _ = msgQueue.add(m) })
          }
          .void
      )
    }

    val event   = stackalloc[SDL_Event]()
    var running = true

    while running do
      while SDL_PollEvent(event) != 0 do
        val rawType = event.asInstanceOf[Ptr[CStruct1[UInt]]]._1
        val msg: GlobalMsg =
          if rawType == SDL_EVENT_QUIT then SdlMsg.Quit
          else SdlMsg.Other(rawType)
        val _ = msgQueue.add(msg)

      var drained = msgQueue.poll()
      while drained != null do
        drained match
          case SdlMsg.Quit => running = false
          case _           => ()
        val (newModel, cmd) = update(model)(drained)
        model = newModel
        runCmd(cmd)
        drained = msgQueue.poll()

      render(model, ctx)
      val _ = SDL_GL_SwapWindow(ctx.window)
      SDL_Delay(16.toUInt)

    releaseDispatcher.unsafeRunSync()
    ctx.destroy()
