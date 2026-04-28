package indigo.internal

import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.html
import org.scalajs.dom.window
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
      Watcher.fromEvent[dom.PointerEvent]("pointercancel", canvas)(impls.onPointerCancel),
      Watcher.fromEvent[dom.KeyboardEvent]("keydown", document)(impls.onKeyDown),
      Watcher.fromEvent[dom.KeyboardEvent]("keyup", document)(impls.onKeyUp),
      Watcher.fromEvent[dom.WheelEvent]("wheel", canvas)(impls.onWheel),
      Watcher.fromEvent[dom.FocusEvent]("focus", canvas)(impls.onCanvasFocus),
      Watcher.fromEvent[dom.FocusEvent]("blur", canvas)(impls.onCanvasBlur),
      Watcher.fromEvent[dom.FocusEvent]("focus", window)(impls.onWindowFocus),
      Watcher.fromEvent[dom.FocusEvent]("blur", window)(impls.onWindowBlur)
    )

object WorldEventWatchers:

  def init(canvas: html.Canvas): WorldEventWatchers =
    new WorldEventWatchers(canvas)
