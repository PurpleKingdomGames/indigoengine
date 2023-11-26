package roguelikestarterkit.ui.datatypes

import indigo.*

final case class UiContext[StartUpData, A](
    bounds: Bounds,
    charSheet: CharSheet,
    mouseCoords: Coords,
    data: A,
    frameContext: FrameContext[StartUpData]
):
  export frameContext.gameTime
  export frameContext.dice
  export frameContext.inputState
  export frameContext.boundaryLocator
  export frameContext.startUpData
  export frameContext.gameTime.running
  export frameContext.gameTime.delta
  export frameContext.inputState.mouse
  export frameContext.inputState.keyboard
  export frameContext.inputState.gamepad
  export frameContext.findBounds
  export frameContext.bounds

  lazy val screenSpaceBounds: Rectangle =
    bounds.toScreenSpace(charSheet.size)

object UiContext:
  def apply[StartUpData, A](
      frameContext: FrameContext[StartUpData],
      charSheet: CharSheet,
      data: A
  ): UiContext[StartUpData, A] =
    val mouseCoords = Coords(frameContext.mouse.position / charSheet.size.toPoint)
    UiContext(Bounds.zero, charSheet, mouseCoords, data, frameContext)
