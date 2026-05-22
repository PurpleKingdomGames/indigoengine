package indigo.internal

import indigoengine.shared.datatypes.Millis
import tyrian.*

import scala.annotation.nowarn

@nowarn("msg=unused")
final class WorldEventWatchers(clickTime: Millis, disableContextMenu: Boolean):

  val watchers: Batch[Watcher] =
    val base: Batch[Watcher] =
      Batch()

    base

object WorldEventWatchers:

  def init(clickTime: Millis, disableContextMenu: Boolean): WorldEventWatchers =
    new WorldEventWatchers(clickTime, disableContextMenu)
