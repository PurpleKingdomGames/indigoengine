package com.example.sandbox

import com.example.sandbox.scenes.ChangeValue
import example.TestFont
import indigo.*
import indigo.syntax.*
import indigoextras.mesh.*
import indigoextras.ui.*

object SandboxModelShared:

  def randomPoint(dice: Dice, offset: Point): Point =
    Point(dice.rollFromZero(100), dice.rollFromZero(100)).moveBy(offset)

  val customButton: Button[Int] =
    Button[Int](Bounds(32, 32)) { (ctx, btn) =>
      Outcome(
        Layer(
          Shape
            .Box(
              btn.bounds.unsafeToRectangle,
              Fill.Color(RGBA.Magenta.mix(RGBA.Black)),
              Stroke(1, RGBA.Magenta)
            )
            .moveTo(ctx.parent.coords.unsafeToPoint)
        )
      )
    }
      .presentDown { (ctx, btn) =>
        Outcome(
          Layer(
            Shape
              .Box(
                btn.bounds.unsafeToRectangle,
                Fill.Color(RGBA.Cyan.mix(RGBA.Black)),
                Stroke(1, RGBA.Cyan)
              )
              .moveTo(ctx.parent.coords.unsafeToPoint)
          )
        )
      }
      .presentOver((ctx, btn) =>
        Outcome(
          Layer(
            Shape
              .Box(
                btn.bounds.unsafeToRectangle,
                Fill.Color(RGBA.Yellow.mix(RGBA.Black)),
                Stroke(1, RGBA.Yellow)
              )
              .moveTo(ctx.parent.coords.unsafeToPoint)
          )
        )
      )
      .onClick(Log("Button clicked"))
      .onPress(Log("Button pressed"))
      .onRelease(Log("Button released"))

  private val text =
    Text("", TestFont.fontKey, SandboxAssets.testFontMaterial)
  private val textRed =
    Text("", TestFont.fontKey, SandboxAssets.testFontMaterial.withTint(RGBA.Red))

  def components: ComponentGroup[Int] =
    ComponentGroup(BoundsMode.fixed(200, 300))
      .withLayout(ComponentLayout.Horizontal(Padding(4), Overflow.Wrap))
      .add(
        ComponentList[Int, Label[Int]](Dimensions(200, 64)) { _ =>
          (1 to 3).toBatch.map { i =>
            ComponentId("lbl" + i) -> Label[Int](
              "Custom rendered label " + i,
              (ctx, label) => Bounds(ctx.services.bounds.get(textRed.withText(label)))
            ) { case (ctx, label) =>
              Outcome(
                Layer(
                  textRed
                    .withText(label.text(ctx))
                    .moveTo(ctx.parent.coords.unsafeToPoint)
                )
              )
            }
          }
        }
      )
      .add(
        Label[Int](
          "Another label",
          (ctx, label) => Bounds(ctx.services.bounds.get(text.withText(label)))
        ) { case (ctx, label) =>
          Outcome(
            Layer(
              text
                .withText(label.text(ctx))
                .moveTo(ctx.parent.coords.unsafeToPoint)
            )
          )
        }
      )
      .add(
        Switch[Int](BoundsType.fixed[Int](40, 40))(
          (context, switch) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    switch.bounds.unsafeToRectangle,
                    Fill.Color(RGBA.Green.mix(RGBA.Black)),
                    Stroke(1, RGBA.Green)
                  )
                  .moveTo(context.parent.coords.unsafeToPoint)
              )
            ),
          (context, switch) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    switch.bounds.unsafeToRectangle,
                    Fill.Color(RGBA.Red.mix(RGBA.Black)),
                    Stroke(1, RGBA.Red)
                  )
                  .moveTo(context.parent.coords.unsafeToPoint)
              )
            )
        )
          .onSwitch((ctx, _) => Batch(Log("Switched to: " + ctx.reference)))
          .switchOn
      )
      .add(
        Button[Int](Bounds(32, 32)) { (context, button) =>
          Outcome(
            Layer(
              Shape
                .Box(
                  button.bounds.unsafeToRectangle,
                  Fill.Color(RGBA.Magenta.mix(RGBA.Black)),
                  Stroke(1, RGBA.Magenta)
                )
                .moveTo(context.parent.coords.unsafeToPoint)
            )
          )
        }
          .presentDown { (context, button) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    button.bounds.unsafeToRectangle,
                    Fill.Color(RGBA.Cyan.mix(RGBA.Black)),
                    Stroke(1, RGBA.Cyan)
                  )
                  .moveTo(context.parent.coords.unsafeToPoint)
              )
            )
          }
          .presentOver((context, button) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    button.bounds.unsafeToRectangle,
                    Fill.Color(RGBA.Yellow.mix(RGBA.Black)),
                    Stroke(1, RGBA.Yellow)
                  )
                  .moveTo(context.parent.coords.unsafeToPoint)
              )
            )
          )
          .onClick(Log("Button clicked"))
          .onPress(Log("Button pressed"))
          .onRelease(Log("Button released"))
      )
      .add(
        ComponentList[Int, ComponentGroup[Int]](Dimensions(200, 64)) { _ =>
          (1 to 3).toBatch.map { i =>
            ComponentId("radio-" + i) ->
              ComponentGroup(BoundsMode.fixed(200, 30))
                .withLayout(ComponentLayout.Horizontal(Padding.right(10)))
                .add(
                  Switch[Int](BoundsType.fixed[Int](20, 20))(
                    (context, switch) =>
                      Outcome(
                        Layer(
                          Shape
                            .Circle(
                              switch.bounds.unsafeToRectangle.toIncircle,
                              Fill.Color(RGBA.Green.mix(RGBA.Black)),
                              Stroke(1, RGBA.Green)
                            )
                            .moveTo(context.parent.coords.unsafeToPoint + Point(10))
                        )
                      ),
                    (context, switch) =>
                      Outcome(
                        Layer(
                          Shape
                            .Circle(
                              switch.bounds.unsafeToRectangle.toIncircle,
                              Fill.Color(RGBA.Red.mix(RGBA.Black)),
                              Stroke(1, RGBA.Red)
                            )
                            .moveTo(context.parent.coords.unsafeToPoint + Point(10))
                        )
                      )
                  )
                    .onSwitch { (_, _) =>
                      Batch(
                        Log("Selected: " + i),
                        ChangeValue(i)
                      )
                    }
                    .withAutoToggle { (ctx, _) =>
                      if ctx.reference == i then Option(SwitchState.On) else Option(SwitchState.Off)
                    }
                )
                .add(
                  Label[Int](
                    "Radio " + i,
                    (ctx, label) => Bounds(ctx.services.bounds.get(textRed.withText(label)))
                  ) { case (ctx, label) =>
                    Outcome(
                      Layer(
                        textRed
                          .withText(label.text(ctx))
                          .moveTo(ctx.parent.bounds.coords.unsafeToPoint)
                      )
                    )
                  }
                )
          }
        }
      )
      .add(
        Button[Int](Bounds(16, 16)) { (context, button) =>
          Outcome(
            Layer(
              Shape
                .Box(
                  button.bounds.unsafeToRectangle,
                  Fill.Color(RGBA.Magenta.mix(RGBA.Black)),
                  Stroke(1, RGBA.Magenta)
                )
                .moveTo(context.parent.coords.unsafeToPoint)
            )
          )
        }
          .presentDown { (context, button) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    button.bounds.unsafeToRectangle,
                    Fill.Color(RGBA.Cyan.mix(RGBA.Black)),
                    Stroke(1, RGBA.Cyan)
                  )
                  .moveTo(context.parent.coords.unsafeToPoint)
              )
            )
          }
          .presentOver((context, button) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    button.bounds.unsafeToRectangle,
                    Fill.Color(RGBA.Yellow.mix(RGBA.Black)),
                    Stroke(1, RGBA.Yellow)
                  )
                  .moveTo(context.parent.coords.unsafeToPoint)
              )
            )
          )
          .onClick(Log("Button clicked!"))
          .onPress(Log("Button pressed!"))
          .onRelease(Log("Button released!"))
          .makeDraggable
          .onDrag(Log("Dragging!"))
      )

  def updateModel(
      context: Context,
      state: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] = {
    case KeyboardEvent.KeyDown(Key.ARROW_LEFT) =>
      println("left")
      Outcome(
        state.copy(
          dude = state.dude.walkLeft
        )
      )

    case KeyboardEvent.KeyDown(Key.ARROW_RIGHT) =>
      Outcome(
        state.copy(
          dude = state.dude.walkRight
        )
      )

    case KeyboardEvent.KeyDown(Key.ARROW_UP) =>
      Outcome(
        state.copy(
          dude = state.dude.walkUp
        )
      )

    case KeyboardEvent.KeyDown(Key.ARROW_DOWN) =>
      Outcome(
        state.copy(
          dude = state.dude.walkDown
        )
      )

    // Left commented out so they can be brought back for testing
    // but otherwise don't interfere with keyboard input tests.
    // case KeyboardEvent.KeyUp(Key.KEY_F) =>
    //   println("Toggle full screen mode...")
    //   Outcome(state)
    //     .addGlobalEvents(FullScreenEvent.Toggle)

    // case KeyboardEvent.KeyUp(Key.KEY_E) =>
    //   println("Enter full screen mode...")
    //   Outcome(state)
    //     .addGlobalEvents(FullScreenEvent.Enter)

    // case KeyboardEvent.KeyUp(Key.KEY_X) =>
    //   println("Exit full screen mode...")
    //   Outcome(state)
    //     .addGlobalEvents(FullScreenEvent.Exit)

    case KeyboardEvent.KeyUp(_) =>
      Outcome(
        state.copy(
          dude = state.dude.idle
        )
      )

    case ViewportResize(size) =>
      Outcome(
        state.copy(
          viewportSize = size
        )
      )

    case e =>
      updateViewModel(context, state.viewModel)(e).map: updated =>
        state.copy(viewModel = updated)

  }

  private def updateViewModel(
      context: Context,
      viewModel: SandboxViewModel
  ): GlobalEvent => Outcome[SandboxViewModel] = {
    case FrameTick =>
      val updateOffset: Point =
        context.frame.input.gamepad.dpad match {
          case GamepadDPad(true, _, _, _) =>
            viewModel.offset + Point(0, -1)

          case GamepadDPad(_, true, _, _) =>
            viewModel.offset + Point(0, 1)

          case GamepadDPad(_, _, true, _) =>
            viewModel.offset + Point(-1, 0)

          case GamepadDPad(_, _, _, true) =>
            viewModel.offset + Point(1, 0)

          case _ =>
            viewModel.offset
        }

      Outcome(viewModel.copy(offset = updateOffset))

    case FullScreenEvent.Entered =>
      println("Entered full screen mode")
      Outcome(viewModel)

    case FullScreenEvent.Exited =>
      println("Exited full screen mode")
      Outcome(viewModel)

    case KeyboardEvent.KeyDown(Key.PAGE_UP) =>
      Outcome(viewModel)
        .addGlobalEvents(SceneEvent.LoopNext)

    case KeyboardEvent.KeyDown(Key.PAGE_DOWN) =>
      Outcome(viewModel)
        .addGlobalEvents(SceneEvent.LoopPrevious)

    case KeyboardEvent.KeyDown(Key.HOME) =>
      Outcome(viewModel)
        .addGlobalEvents(SceneEvent.First)

    case KeyboardEvent.KeyDown(Key.END) =>
      Outcome(viewModel)
        .addGlobalEvents(SceneEvent.Last)

    case _ =>
      Outcome(viewModel)
  }

final case class MeshData(
    points: Batch[Point],
    superTriangle: Triangle,
    mesh: Mesh
)

final case class DudeModel(dude: Dude, walkDirection: DudeDirection) {
  def idle: DudeModel      = this.copy(walkDirection = DudeIdle)
  def walkLeft: DudeModel  = this.copy(walkDirection = DudeLeft)
  def walkRight: DudeModel = this.copy(walkDirection = DudeRight)
  def walkUp: DudeModel    = this.copy(walkDirection = DudeUp)
  def walkDown: DudeModel  = this.copy(walkDirection = DudeDown)
}

sealed trait DudeDirection derives CanEqual:
  val cycleName: CycleLabel
case object DudeIdle  extends DudeDirection { val cycleName: CycleLabel = CycleLabel("blink")      }
case object DudeLeft  extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk left")  }
case object DudeRight extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk right") }
case object DudeUp    extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk up")    }
case object DudeDown  extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk down")  }
