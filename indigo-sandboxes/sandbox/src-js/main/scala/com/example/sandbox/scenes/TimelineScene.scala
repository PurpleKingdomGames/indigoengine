package com.example.sandbox.scenes

import com.example.sandbox.Dude
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import indigo.*
import indigo.scenes.*
import indigo.syntax.*
import indigo.syntax.animations.*

object TimelineScene extends Scene[SandboxGameModel]:

  type SceneModel = Dude

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, Dude] =
    Lens.readOnly(_.dude.dude)

  def name: SceneName =
    SceneName("timeline")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext,
      model: Dude
  ): GlobalEvent => Outcome[Dude] =
    _ => Outcome(model)

  val crate: Graphic[Material.ImageEffects] =
    Graphic(64, 64, SandboxAssets.cratesMaterial)
      .modifyMaterial(_.toImageEffects.disableLighting)
      .withCrop(0, 0, 32, 32)

  val move: Graphic[Material.ImageEffects] => SignalFunction[Point, Graphic[Material.ImageEffects]] = g =>
    SignalFunction(pt => g.moveTo(pt))

  val modifier: Graphic[Material.ImageEffects] => SignalFunction[Seconds, Graphic[Material.ImageEffects]] =
    g => sin >>> SignalFunction(d => (d + 1) / 2) >>> SignalFunction(d => g.modifyMaterial(_.withAlpha(d)))

  val tl: Seconds => Timeline[Graphic[Material.ImageEffects]] = delay =>
    timeline(
      layer(
        startAfter(delay),
        animate(5.seconds) {
          easeInOut >>> lerp(Point(0), Point(100)) >>> move(_)
        },
        animate(3.seconds) {
          easeInOut >>> lerp(Point(100), Point(100, 0)) >>> move(_)
        }
      ),
      layer(
        startAfter(delay),
        animate(8.seconds, modifier)
      )
    )

  val spriteTimeline: Timeline[Sprite[Material.ImageEffects]] =
    val loopLength = 700.millis.toSeconds

    timeline(
      layer(
        animate(3.seconds) { sprite =>
          wrap(loopLength) >>> lerp(loopLength) >>> SignalFunction(d => sprite.scrubTo(d))
        },
        show(2.seconds)(_.changeCycle(CycleLabel("blink"))),
        animate(3.seconds) { sprite =>
          wrap(loopLength) >>> lerp(loopLength) >>> SignalFunction(d => sprite.scrubTo(d))
        }
      )
    )

  val clipTimeline: Timeline[Clip[Material.Bitmap]] =
    timeline(
      layer(
        animate(5.seconds) { clip =>
          wrap(clip.length) >>> lerp(0, 1, clip.length) >>> SignalFunction(d => clip.scrubTo(d))
        }
      )
    )

  val trafficLights =
    Clip(Point(0), Size(64), ClipSheet(3, Seconds(0.25), 2), Material.Bitmap(SandboxAssets.trafficLightsName))
      .moveTo(50, 0)

  def present(
      context: SceneContext,
      model: Dude
  ): Outcome[SceneUpdateFragment] =
    val dude = model.sprite.changeCycle(CycleLabel("walk right")).moveTo(32, 32)

    Outcome(
      SceneUpdateFragment(
        tl(2.seconds).atOrLast(context.frame.time.running)(crate).toBatch ++
          spriteTimeline
            .at(context.frame.time.running)(dude)
            .toBatch ++
          clipTimeline
            .at(context.frame.time.running)(trafficLights)
            .toBatch
      )
    )
