package demo

import indigo.*
import indigo.scenes.*
import indigo.shared.subsystems.SubSystemFrameContext.*
import roguelikestarterkit.*

object NoTerminalUI extends Scene[Size, Model, ViewModel]:

  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val name: SceneName =
    SceneName("NoTerminalUI scene")

  val modelLens: Lens[Model, Model] =
    Lens.keepLatest

  val viewModelLens: Lens[ViewModel, ViewModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[Model]] =
    Set()

  def updateModel(
      context: SceneContext[Size],
      model: Model
  ): GlobalEvent => Outcome[Model] =
    case e =>
      val ctx = UIContext(context.frameContext.forSubSystems.copy(reference = 0), Size(1))
      summon[Component[ComponentList[Int], Int]].updateModel(ctx, model.componentList)(e).map {
        cl =>
          model.copy(componentList = cl)
      }

  def updateViewModel(
      context: SceneContext[Size],
      model: Model,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: SceneContext[Size],
      model: Model,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] =

    val rendered =
      summon[Component[ComponentList[Int], Int]]
        .present(
          UIContext(context.frameContext.forSubSystems.copy(reference = 0), Size(1)),
          model.componentList
        )

    rendered.map { componentFragment =>
      SceneUpdateFragment(
        componentFragment.toLayer
      )
    }
