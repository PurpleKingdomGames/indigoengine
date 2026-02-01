package example

import indigo.*
import indigo.scenes.*

object LoadScene extends Scene[Unit, Model]:

  type SceneModel = Unit

  val name: SceneName =
    SceneName("load")

  val modelLens: Lens[Model, Unit] =
    Lens.unit

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[Model]] =
    Set()

  def updateModel(
      context: SceneContext[Unit],
      model: Unit
  ): GlobalEvent => Outcome[Unit] =
    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(model)
        .addGlobalEvents(SceneEvent.Next)

    case _ =>
      Outcome(model)

  def present(
      context: SceneContext[Unit],
      model: Unit
  ): Outcome[SceneUpdateFragment] =
    val tb =
      Text(
        "Hit space to start",
        PixelatedFont.fontKey,
        Assets.assets.generated.PixelatedFontMaterial.toImageEffects.withOverlay(Fill.Color(RGBA.Red))
      ).alignCenter

    val bounds =
      context.services.bounds.find(tb).getOrElse(Rectangle(0, 0, 0, 0))

    Outcome(
      SceneUpdateFragment(
        tb.moveTo(400 - (bounds.width / 2), 10)
      )
    )
