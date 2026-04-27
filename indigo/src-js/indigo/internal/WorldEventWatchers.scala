package indigo.internal

import org.scalajs.dom
import org.scalajs.dom.html
import tyrian.*
import tyrian.syntax.*

final class WorldEventWatchers(canvas: html.Canvas):

  private val impls = new WorldEventWatcherImpls(canvas)

  val watchers: Batch[Watcher] =
    Batch(
      Watcher.fromEvent[dom.PointerEvent]("pointermove", canvas)(impls.onPointerMove),
      Watcher.fromEvent[dom.PointerEvent]("pointerenter", canvas)(impls.onPointerEnter),
      Watcher.fromEvent[dom.PointerEvent]("pointerleave", canvas)(impls.onPointerLeave),
      Watcher.fromEvent[dom.PointerEvent]("pointerdown", canvas)(impls.onPointerDown),
      Watcher.fromEvent[dom.PointerEvent]("pointerup", canvas)(impls.onPointerUp),
      Watcher.fromEvent[dom.PointerEvent]("pointercancel", canvas)(impls.onPointerCancel)
    )

object WorldEventWatchers:

  def init(canvas: html.Canvas): WorldEventWatchers =
    new WorldEventWatchers(canvas)
