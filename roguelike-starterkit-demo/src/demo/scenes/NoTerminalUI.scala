package demo.scenes

import demo.Assets
import demo.GameEvent
import demo.models.ChangeValue
import demo.models.GameModel
import indigo.*
import indigo.shared.subsystems.SubSystemContext.*
import indigo.syntax.*
import indigoextras.ui.*
import indigoextras.ui.syntax.*
import roguelikestarterkit.RoguelikeTiles
import roguelikestarterkit.TerminalMaterial

object NoTerminalUI extends Scene[Size, GameModel]:

  type SceneModel = GameModel

  val name: SceneName =
    SceneName("NoTerminalUI scene")

  val modelLens: Lens[GameModel, GameModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[GameModel]] =
    Set()

  def updateModel(
      context: SceneContext[Size],
      model: GameModel
  ): GlobalEvent => Outcome[GameModel] =
    case ChangeValue(value) =>
      Outcome(model.copy(num = value))

    case e =>
      val ctx =
        UIContext(context.toContext.forSubSystems.copy(reference = model.num), Size(1), 1)
      summon[Component[ComponentGroup[Int], Int]].updateModel(ctx, model.components)(e).map { cl =>
        model.copy(components = cl)
      }

  def present(
      context: SceneContext[Size],
      model: GameModel
  ): Outcome[SceneUpdateFragment] =
    model.components
      .present(UIContext(context.toContext.forSubSystems.copy(reference = 0), Size(1), 1))
      .map(l => SceneUpdateFragment(l))

object NoTerminalUIComponents:

  private val text: Text[TerminalMaterial] =
    Text(
      "",
      RoguelikeTiles.Size10x10.Fonts.fontKey,
      TerminalMaterial(Assets.assets.AnikkiSquare10x10)
    )

  val components: ComponentGroup[Int] =
    ComponentGroup(BoundsMode.fixed(200, 300))
      .add(
        ComponentList(Dimensions(200, 40)) { (_: UIContext[Int]) =>
          (1 to 3).toBatch.map { i =>
            ComponentId("lbl" + i) -> Label[Int](
              "Custom rendered label " + i,
              (_, _) => Bounds(0, 0, 150, 10)
            ) { case (ctx, label) =>
              Outcome(
                Layer(
                  text
                    .withText(label.text(ctx))
                    // .modifyMaterial(_.withTint(RGBA.Red))
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
          (_, _) => Bounds(0, 0, 150, 10)
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
        Switch[Int](Bounds(40, 40))(
          (ctx, switch) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    switch.bounds(ctx).unsafeToRectangle,
                    Fill.Color(RGBA.Green.mix(RGBA.Black)),
                    Stroke(1, RGBA.Green)
                  )
                  .moveTo(ctx.parent.coords.unsafeToPoint)
              )
            ),
          (ctx, switch) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    switch.bounds(ctx).unsafeToRectangle,
                    Fill.Color(RGBA.Red.mix(RGBA.Black)),
                    Stroke(1, RGBA.Red)
                  )
                  .moveTo(ctx.parent.coords.unsafeToPoint)
              )
            )
        )
          .onSwitch((_, switch) => Batch(GameEvent.Log("Switched to: " + switch.state)))
          .switchOn
      )
      .add(
        Button[Int](Bounds(32, 32)) { (ctx, btn) =>
          Outcome(
            Layer(
              Shape
                .Box(
                  btn.bounds(ctx).unsafeToRectangle,
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
                    btn.bounds(ctx).unsafeToRectangle,
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
                    btn.bounds(ctx).unsafeToRectangle,
                    Fill.Color(RGBA.Yellow.mix(RGBA.Black)),
                    Stroke(1, RGBA.Yellow)
                  )
                  .moveTo(ctx.parent.coords.unsafeToPoint)
              )
            )
          )
          .onClick(GameEvent.Log("Button clicked"))
          .onPress(GameEvent.Log("Button pressed"))
          .onRelease(GameEvent.Log("Button released"))
      )
      .add(
        ComponentList(Dimensions(200, 150)) { (_: UIContext[Int]) =>
          (1 to 3).toBatch.map { i =>
            ComponentId("radio-" + i) ->
              ComponentGroup(BoundsMode.fixed(200, 30))
                .withLayout(ComponentLayout.Horizontal(Padding.right(10)))
                .add(
                  Switch[Int](Bounds(20, 20))(
                    (ctx, switch) =>
                      Outcome(
                        Layer(
                          Shape
                            .Circle(
                              switch.bounds(ctx).unsafeToRectangle.toIncircle,
                              Fill.Color(RGBA.Green.mix(RGBA.Black)),
                              Stroke(1, RGBA.Green)
                            )
                            .moveTo(ctx.parent.coords.unsafeToPoint + Point(10))
                        )
                      ),
                    (ctx, switch) =>
                      Outcome(
                        Layer(
                          Shape
                            .Circle(
                              switch.bounds(ctx).unsafeToRectangle.toIncircle,
                              Fill.Color(RGBA.Red.mix(RGBA.Black)),
                              Stroke(1, RGBA.Red)
                            )
                            .moveTo(ctx.parent.coords.unsafeToPoint + Point(10))
                        )
                      )
                  )
                    .onSwitch { (_, _) =>
                      Batch(
                        GameEvent.Log("Selected: " + i),
                        ChangeValue(i)
                      )
                    }
                    .withAutoToggle { (ctx, _) =>
                      if ctx.reference == i then Option(SwitchState.On)
                      else Option(SwitchState.Off)
                    }
                )
                .add(
                  Label[Int](
                    "Radio " + i,
                    (_, _) => Bounds(0, 0, 150, 10)
                  ) { case (ctx, label) =>
                    Outcome(
                      Layer(
                        text
                          .modifyMaterial(_.withForeground(RGBA.Red))
                          .withText(label.text(ctx))
                          .moveTo(ctx.parent.coords.unsafeToPoint)
                      )
                    )
                  }
                )
          }
        }
      )
