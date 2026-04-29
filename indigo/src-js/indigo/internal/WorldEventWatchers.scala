package indigo.internal

import indigoengine.shared.datatypes.Millis
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.html
import org.scalajs.dom.window
import tyrian.*
import tyrian.syntax.*

final class WorldEventWatchers(canvas: html.Canvas, clickTime: Millis, disableContextMenu: Boolean):

  private val impls = new WorldEventWatcherImpls(canvas)

  val watchers: Batch[Watcher] =
    val base: Batch[Watcher] =
      Batch(
        Watcher.fromEvent[dom.PointerEvent]("pointermove", canvas)(impls.onPointerMove),
        Watcher.fromEvent[dom.PointerEvent]("pointerenter", canvas)(impls.onPointerEnter),
        Watcher.fromEvent[dom.PointerEvent]("pointerleave", canvas)(impls.onPointerLeave),
        Watcher.fromEvent[dom.PointerEvent]("pointerdown", canvas)(impls.onPointerDown),
        Watcher.fromEvent[dom.PointerEvent]("pointerup", canvas)(impls.onPointerUp(clickTime.toLong)),
        Watcher.fromEvent[dom.PointerEvent]("pointercancel", canvas)(impls.onPointerCancel),
        Watcher.fromEvent[dom.KeyboardEvent]("keydown", document)(impls.onKeyDown),
        Watcher.fromEvent[dom.KeyboardEvent]("keyup", document)(impls.onKeyUp),
        Watcher.fromEvent[dom.WheelEvent]("wheel", canvas)(impls.onWheel),
        Watcher.fromEvent[dom.FocusEvent]("focus", canvas)(impls.onCanvasFocus),
        Watcher.fromEvent[dom.FocusEvent]("blur", canvas)(impls.onCanvasBlur),
        Watcher.fromEvent[dom.FocusEvent]("focus", window)(impls.onWindowFocus),
        Watcher.fromEvent[dom.FocusEvent]("blur", window)(impls.onWindowBlur)
      )

    // Bug / TODO: This doesn't actually work, probably because Tyrian has it's own preventDefault and is getting there first.
    if disableContextMenu then base :+ Watcher.fromEvent[dom.MouseEvent]("contextmenu", canvas)(impls.onContextMenu)
    else base

object WorldEventWatchers:

  def init(canvas: html.Canvas, clickTime: Millis, disableContextMenu: Boolean): WorldEventWatchers =
    new WorldEventWatchers(canvas, clickTime, disableContextMenu)
