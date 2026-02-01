package indigo.scenes

import indigo.core.Outcome
import indigo.core.events.EventFilters
import indigo.core.events.GlobalEvent
import indigo.core.utils.IndigoLogger
import indigo.scenegraph.SceneUpdateFragment
import indigo.shared.Context
import indigo.shared.subsystems.SubSystemContext
import indigo.shared.subsystems.SubSystemContext.*
import indigo.shared.subsystems.SubSystemsRegister
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.KVP
import indigoengine.shared.collections.NonEmptyBatch
import indigoengine.shared.datatypes.Seconds

import scala.annotation.nowarn

class SceneManager[StartUpData, GameModel](
    scenes: NonEmptyBatch[Scene[StartUpData, GameModel]],
    scenesFinder: SceneFinder
):

  // private given CanEqual[Option[Scene[StartUpData, GameModel]], Option[
  //   Scene[StartUpData, GameModel]
  // ]] =
  //   CanEqual.derived

  // Scene management
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var finderInstance: SceneFinder = scenesFinder
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var lastSceneChangeAt: Seconds = Seconds.zero

  @nowarn("msg=unused")
  private val subSystemStates: KVP[SubSystemsRegister[GameModel]] =
    KVP
      .empty[SubSystemsRegister[GameModel]]
      .addAll(
        scenes.toBatch.map { s =>
          val r = new SubSystemsRegister[GameModel]()
          r.register(Batch.fromSet(s.subSystems))
          s.name.toString -> r
        }
      )

  // Scene delegation

  def updateModel(ctx: Context[StartUpData], model: GameModel): GlobalEvent => Outcome[GameModel] =
    case SceneEvent.First =>
      lastSceneChangeAt = ctx.frame.time.running

      val from = finderInstance.current.name
      finderInstance = finderInstance.first
      val to = finderInstance.current.name

      val events =
        if from == to then Batch.empty
        else Batch(SceneEvent.SceneChange(from, to, lastSceneChangeAt))

      Outcome(model, events)

    case SceneEvent.Last =>
      lastSceneChangeAt = ctx.frame.time.running

      val from = finderInstance.current.name
      finderInstance = finderInstance.last
      val to = finderInstance.current.name

      val events =
        if from == to then Batch.empty
        else Batch(SceneEvent.SceneChange(from, to, lastSceneChangeAt))

      Outcome(model, events)

    case SceneEvent.Next =>
      lastSceneChangeAt = ctx.frame.time.running

      val from = finderInstance.current.name
      finderInstance = finderInstance.forward
      val to = finderInstance.current.name

      val events =
        if from == to then Batch.empty
        else Batch(SceneEvent.SceneChange(from, to, lastSceneChangeAt))

      Outcome(model, events)

    case SceneEvent.LoopNext =>
      lastSceneChangeAt = ctx.frame.time.running

      val from = finderInstance.current.name
      finderInstance = finderInstance.forwardLoop
      val to = finderInstance.current.name

      val events =
        if from == to then Batch.empty
        else Batch(SceneEvent.SceneChange(from, to, lastSceneChangeAt))

      Outcome(model, events)

    case SceneEvent.Previous =>
      lastSceneChangeAt = ctx.frame.time.running

      val from = finderInstance.current.name
      finderInstance = finderInstance.backward
      val to = finderInstance.current.name

      val events =
        if from == to then Batch.empty
        else Batch(SceneEvent.SceneChange(from, to, lastSceneChangeAt))

      Outcome(model, events)

    case SceneEvent.LoopPrevious =>
      lastSceneChangeAt = ctx.frame.time.running

      val from = finderInstance.current.name
      finderInstance = finderInstance.backwardLoop
      val to = finderInstance.current.name

      val events =
        if from == to then Batch.empty
        else Batch(SceneEvent.SceneChange(from, to, lastSceneChangeAt))

      Outcome(model, events)

    case SceneEvent.JumpTo(name) =>
      lastSceneChangeAt = ctx.frame.time.running

      val from = finderInstance.current.name
      finderInstance = finderInstance.jumpToSceneByName(name)
      val to = finderInstance.current.name

      val events =
        if from == to then Batch.empty
        else Batch(SceneEvent.SceneChange(from, to, lastSceneChangeAt))

      Outcome(model, events)

    case event =>
      scenes.find(_.name == finderInstance.current.name) match
        case None =>
          IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name)
          Outcome(model)

        case Some(scene) =>
          Scene
            .updateModel(
              scene,
              SceneContext(scene.name, lastSceneChangeAt, ctx),
              model
            )(event)

  def updateSubSystems(
      ctx: SubSystemContext[Unit],
      model: GameModel,
      globalEvents: Batch[GlobalEvent]
  ): Outcome[SubSystemsRegister[GameModel]] =
    scenes
      .find(_.name == finderInstance.current.name)
      .flatMap { scene =>
        subSystemStates
          .get(scene.name.toString)
          .map {
            _.update(ctx, model, globalEvents)
          }
      }
      .getOrElse(
        Outcome.raiseError(
          new Exception(s"Couldn't find scene with name '${finderInstance.current.name}' in order to update subsystems")
        )
      )

  def updateView(
      ctx: Context[StartUpData],
      model: GameModel
  ): Outcome[SceneUpdateFragment] =
    scenes.find(_.name == finderInstance.current.name) match
      case None =>
        IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name)
        Outcome(SceneUpdateFragment.empty)

      case Some(scene) =>
        val subsystemView = subSystemStates
          .get(scene.name.toString)
          .map { ssr =>
            ssr.present(ctx.forSubSystems, model)
          }
          .getOrElse(Outcome(SceneUpdateFragment.empty))

        Outcome.merge(
          Scene.updateView(
            scene,
            SceneContext(scene.name, lastSceneChangeAt, ctx),
            model
          ),
          subsystemView
        )(_ |+| _)

  def eventFilters: EventFilters =
    scenes.find(_.name == finderInstance.current.name) match
      case None =>
        // This should never be the case, we should always find a scene.
        EventFilters.BlockAll

      case Some(value) =>
        value.eventFilters

object SceneManager:

  def apply[StartUpData, GameModel](
      scenes: NonEmptyBatch[Scene[StartUpData, GameModel]],
      initialScene: SceneName
  ): SceneManager[StartUpData, GameModel] =
    new SceneManager[StartUpData, GameModel](
      scenes,
      SceneFinder.fromScenes[StartUpData, GameModel](scenes).jumpToSceneByName(initialScene)
    )
