package roguelikestarterkit.ui.window

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import roguelikestarterkit.ComponentGroup
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.UiContext

trait WindowContent[A]:

  /** Update this content's model.
    */
  def updateModel[StartupData, ContextData](
      context: UiContext[StartupData, ContextData],
      model: A
  ): GlobalEvent => Outcome[A]

  /** Produce a renderable output for this content base on the model
    */
  def present[StartupData, ContextData](
      context: UiContext[StartupData, ContextData],
      model: A
  ): Outcome[SceneUpdateFragment]

  /** Called when the window's content area bounds changes.
    */
  def onContentAreaBoundsChange(model: A, newBounds: Bounds): A

object WindowContent:

  given WindowContent[Unit] with

    def updateModel[StartupData, ContextData](
        context: UiContext[StartupData, ContextData],
        model: Unit
    ): GlobalEvent => Outcome[Unit] =
      _ => Outcome(model)

    def present[StartupData, ContextData](
        context: UiContext[StartupData, ContextData],
        model: Unit
    ): Outcome[SceneUpdateFragment] =
      Outcome(SceneUpdateFragment.empty)

    def onContentAreaBoundsChange(model: Unit, newBounds: Bounds): Unit =
      model

  given WindowContent[ComponentGroup] with

    def updateModel[StartupData, ContextData](
        context: UiContext[StartupData, ContextData],
        model: ComponentGroup
    ): GlobalEvent => Outcome[ComponentGroup] =
      e => model.update(context)(e)

    def present[StartupData, ContextData](
        context: UiContext[StartupData, ContextData],
        model: ComponentGroup
    ): Outcome[SceneUpdateFragment] =
      model.present(context).map(_.toSceneUpdateFragment)

    def onContentAreaBoundsChange(model: ComponentGroup, newBounds: Bounds): ComponentGroup =
      model.propagatedChange(newBounds)
