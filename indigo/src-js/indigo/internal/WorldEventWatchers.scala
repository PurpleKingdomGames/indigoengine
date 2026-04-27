package indigo.internal

import org.scalajs.dom
import org.scalajs.dom.html
import tyrian.*
import tyrian.syntax.*

import WorldEventWatcherImpls.*

object WorldEventWatchers:

  def watchers(canvas: html.Canvas): Batch[Watcher] =
    Batch(
      pointerMove(canvas)
    )

  def pointerMove(canvas: html.Canvas): Watcher =
    Watcher.fromEvent[dom.PointerEvent]("pointermove", canvas)(onPointerMove(canvas))
