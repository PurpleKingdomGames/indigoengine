package com.example.sandbox.scenes

import com.example.sandbox.DudeDown
import com.example.sandbox.DudeIdle
import com.example.sandbox.DudeLeft
import com.example.sandbox.DudeRight
import com.example.sandbox.DudeUp
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGame
import com.example.sandbox.SandboxGameModel
import indigo.*
import indigo.core.events.ScreenCaptureEvent
import indigo.scenegraph.Shape
import indigo.scenegraph.Shape.Box
import indigo.scenes.*

object CaptureScreenScene extends Scene[SandboxGameModel]:

  type SceneModel = SandboxGameModel

  val uiKey          = LayerKey("ui")
  val defaultKey     = LayerKey("default")
  val dudeCloneId    = CloneId("Dude")
  val clippingRect   = Rectangle(25, 25, 150, 100)
  val screenshot1Key = BindingKey("screenshot1")
  val screenshot2Key = BindingKey("screenshot2")

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepLatest

  def name: SceneName =
    SceneName("captureScreen")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext,
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    case PointerEvent.Click(x, y) if x >= 250 && x <= 266 && y >= 165 && y <= 181 =>
      Outcome(model)
        .addGlobalEvents(
          // Get the full screen and scale it down
          ScreenCaptureEvent.Capture(
            ScreenCaptureConfig.default
              .withName("screenshot1")
              .withScale(0.5),
            screenshot1Key
          ),
          // Get the screen inside the clipping rectangle and scale it. We don't remove the UI layer here
          ScreenCaptureEvent.Capture(
            ScreenCaptureConfig.default
              .withName("screenshot2")
              .withScale(0.5)
              .withCrop(clippingRect),
            screenshot2Key
          )
        )

    case ScreenCaptureEvent.Captured(key, image) =>
      IndigoLogger.info(image.path.toString())

      if key == screenshot1Key then
        Outcome(
          model.copy(
            captureScreenScene = model.captureScreenScene.copy(screenshot1 = Some(image.name))
          )
        )
      else if key == screenshot2Key then
        Outcome(
          model.copy(
            captureScreenScene = model.captureScreenScene.copy(screenshot2 = Some(image.name))
          )
        )
      else Outcome(model)

    case ScreenCaptureEvent.CaptureError(_, message) =>
      IndigoLogger.error(s"Screen capture failed: $message")
      Outcome(model)

    case _ =>
      Outcome(model)

  def present(
      context: SceneContext,
      model: SandboxGameModel
  ): Outcome[SceneUpdateFragment] =
    val screenshotScale = 0.3
    val viewPort        = Size(SandboxGame.viewportWidth, SandboxGame.viewportHeight)
    val bigRect         = Rectangle((viewPort.width * screenshotScale).toInt, (viewPort.height * screenshotScale).toInt)
    val smallRect = Rectangle(
      0,
      0,
      (clippingRect.width * screenshotScale).toInt,
      (clippingRect.height * screenshotScale).toInt
    )
    Outcome(
      SceneUpdateFragment(
        uiKey -> Layer(
          Batch(
            Graphic(16, 16, Material.Bitmap(SandboxAssets.cameraIcon)).moveTo(250, 165),
            Shape.Box(clippingRect, Fill.None, Stroke(1, RGBA.SlateGray))
          ) ++ ((model.captureScreenScene.screenshot1, model.captureScreenScene.screenshot2) match {
            case (Some(image1), Some(image2)) =>
              Batch(
                Graphic(
                  viewPort,
                  Material.Bitmap(image1)
                ).scaleBy(Vector2(screenshotScale))
                  .moveTo(viewPort.width - (viewPort.width * screenshotScale).toInt - 5, 5),
                Box(bigRect, Fill.None, Stroke(1, RGBA.Black))
                  .moveTo(viewPort.width - (viewPort.width * screenshotScale).toInt - 5, 5),
                Graphic(
                  clippingRect.size,
                  Material.Bitmap(image2)
                ).scaleBy(Vector2(screenshotScale))
                  .moveTo(
                    viewPort.width - (clippingRect.width * screenshotScale).toInt - 5,
                    (viewPort.height * screenshotScale).toInt + 10
                  ),
                Box(smallRect, Fill.None, Stroke(1, RGBA.Black))
                  .moveTo(
                    viewPort.width - (clippingRect.width * screenshotScale).toInt - 5,
                    (viewPort.height * screenshotScale).toInt + 10
                  )
              )
            case _ => Batch.empty
          })
        ),
        defaultKey -> Layer(gameLayer(model, model.captureScreenScene))
      )
    )

  def gameLayer(currentState: SandboxGameModel, model: Model): Batch[SceneNode] =
    Batch(
      currentState.dude.walkDirection match {
        case d @ DudeLeft =>
          currentState.dude.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeRight =>
          currentState.dude.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeUp =>
          currentState.dude.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeDown =>
          currentState.dude.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeIdle =>
          currentState.dude.dude.sprite
            .changeCycle(d.cycleName)
            .play()
      },
      currentState.dude.dude.sprite
        .moveBy(8, 10)
        .moveBy(model.offset)
        .modifyMaterial(
          _.withAlpha(1)
            .withTint(RGBA.Green.withAmount(0.25))
            .withSaturation(1.0)
        ),
      currentState.dude.dude.sprite
        .moveBy(8, -10)
        .modifyMaterial(_.withAlpha(0.5).withTint(RGBA.Red.withAmount(0.75))),
      CloneBatch(dudeCloneId, CloneBatchData(16, 64, Radians.zero, -1.0, 1.0))
    )

  final case class Model(
      screenshot1: Option[AssetName],
      screenshot2: Option[AssetName],
      offset: Point
  )
