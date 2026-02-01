package com.example.sandbox.scenes

import com.example.sandbox.Fonts
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import indigo.*
import indigo.scenes.*

object ConfettiScene extends Scene[SandboxStartupData, SandboxGameModel]:

  val spawnCount: Int = 600

  type SceneModel = ConfettiModel

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, ConfettiModel] =
    Lens(_.confetti, (m, c) => m.copy(confetti = c))

  def name: SceneName =
    SceneName("confetti")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: ConfettiModel
  ): GlobalEvent => Outcome[ConfettiModel] =

    case FrameTick =>
      val pos = Signal.Orbit(context.startUpData.viewportCenter * 2, 100).at(context.frame.time.running * 0.5).toPoint
      Outcome(
        model
          .spawn(
            context.services.random,
            pos.x,
            pos.y,
            spawnCount
          )
          .update
      )

    case _ =>
      Outcome(model)

  val cloneId: CloneId = CloneId("dots")

  val cloneBlanks: Batch[CloneBlank] =
    Batch(CloneBlank(cloneId, Graphic(16, 16, Material.Bitmap(SandboxAssets.dots))).static)

  val crops =
    Array(
      Array[Int](0, 0, 16, 16),
      Array[Int](16, 0, 16, 16),
      Array[Int](0, 16, 16, 16),
      Array[Int](16, 16, 16, 16)
    )

  val count: Text[Material.ImageEffects] =
    Text("", 228, 20, Fonts.fontKey, SandboxAssets.fontMaterial)

  def particlesToCloneTiles(particles: Batch[Particle]): CloneTiles =
    CloneTiles(
      cloneId,
      particles.map { p =>
        val crop = crops(p.color)
        CloneTileData(p.x, p.y, Radians.zero, p.scale, p.scale, crop(0), crop(1), crop(2), crop(3))
      }
    )

  def present(
      context: SceneContext[SandboxStartupData],
      model: ConfettiModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Layer(
          model.particles.map(particlesToCloneTiles)
        ).withMagnification(1),
        Layer(
          count.withText(s"count: ${model.particles.length * spawnCount}")
        ).withMagnification(1)
      ).addCloneBlanks(cloneBlanks)
    )

final case class ConfettiModel(color: Int, particles: Batch[Batch[Particle]]):
  def spawn(random: Context.Services.Random, x: Int, y: Int, count: Int): ConfettiModel =
    this.copy(
      particles = Batch(Batch.fromIndexedSeq(0 until count).map { _ =>
        Particle(
          x,
          y,
          random.nextFloat * 2.0f - 1.0f,
          random.nextFloat * 2.0f,
          color,
          ((random.nextFloat * 0.5f) + 0.5f) * 0.5f
        )
      }) ++ particles
    )

  def update: ConfettiModel =
    this.copy(
      color = (color + 1) % 4,
      particles = particles
        .map {
          _.filter(p => p.y < 400).map { p =>
            val newFy = p.fy - 0.1f
            val newFx = p.fx * 0.95f
            p.copy(
              x = p.x + (15 * newFx).toInt,
              y = p.y - (5 * newFy).toInt,
              fx = newFx,
              fy = newFy
            )
          }
        }
        .filter(_.nonEmpty)
    )

object ConfettiModel:
  val empty: ConfettiModel =
    ConfettiModel(0, Batch())

final case class Particle(x: Int, y: Int, fx: Float, fy: Float, color: Int, scale: Float)
object Particle:
  given CanEqual[Option[Particle], Option[Particle]] = CanEqual.derived
  given CanEqual[List[Particle], List[Particle]]     = CanEqual.derived
