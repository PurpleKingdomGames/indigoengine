package demo

import indigo.*

object Constants:

  val magnification: Magnification = Magnification.x2

  object LayerKeys:
    val background: LayerKey = LayerKey("background")
    val game: LayerKey       = LayerKey("game")
    val ui: LayerKey         = LayerKey("ui")
    val fps: LayerKey        = LayerKey("fps")
