package com.example.sandbox.scenes

import com.example.sandbox.Constants
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGame
import com.example.sandbox.SandboxGameModel
import example.TestFont
import indigo.*
import indigo.scenes.*
import indigoextras.ui.*
import indigoextras.ui.syntax.*

object LocaleScene extends Scene[SandboxGameModel]:

  type SceneModel = SandboxGameModel

  val name: SceneName =
    SceneName("LocaleScene")

  val modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[SandboxGameModel]] =
    Set(
      WindowManager[Unit, SandboxGameModel, Unit](
        id = SubSystemId("locale-window-manager"),
        magnification = LocaleUI.magnification,
        snapGrid = Size.one,
        extractReference = _ => (),
        startUpData = (),
        layerKey = Constants.LayerKeys.windows
      )
        .register(LocaleUI.localesWindow)
    )

  def updateModel(
      context: SceneContext,
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    case e =>
      model.localeButton
        .update(LocaleUI.uiContext(context.toContext))(e)
        .map(b => model.copy(localeButton = b))

  def present(
      context: SceneContext,
      model: SandboxGameModel
  ): Outcome[SceneUpdateFragment] =
    model.localeButton
      .present(LocaleUI.uiContext(context.toContext))
      .map { buttonLayer =>
        SceneUpdateFragment(
          Constants.LayerKeys.game -> buttonLayer
        ).withMagnification(LocaleUI.magnification)
      }

object LocaleUI:

  val magnification: Magnification = Magnification.x2

  val windowId: WindowId = WindowId("locale-window")

  val textInstance: Text[Material.ImageEffects] =
    Text(
      "",
      TestFont.fontKey,
      SandboxAssets.testFontMaterial
    )

  def currentLocaleText(ctx: UIContext[?]): String =
    ctx.services.locale.current.map(_.toString).getOrElse("Unknown locale")

  def textBounds(ctx: UIContext[?], text: String): Bounds =
    Bounds(ctx.services.bounds.get(textInstance.withText(text)))

  def uiContext(context: Context): UIContext[Unit] =
    val ctx  = UIContext(context).withMagnification(magnification)
    val size = textBounds(ctx, currentLocaleText(ctx)).dimensions

    ctx.moveParentTo(
      (SandboxGame.gameWidth - size.width) / 2,
      (SandboxGame.gameHeight - size.height) / 2
    )

  val currentLocaleButton: Button[Unit] =
    Button[Unit](ctx => textBounds(ctx, currentLocaleText(ctx))) { (ctx, _) =>
      Outcome(
        Layer(
          textInstance
            .withText(currentLocaleText(ctx))
            .withMaterial(SandboxAssets.testFontMaterial.withTint(RGBA.White))
            .moveTo(ctx.parent.coords.unsafeToPoint)
        )
      )
    }
      .presentOver { (ctx, _) =>
        Outcome(
          Layer(
            textInstance
              .withText(currentLocaleText(ctx))
              .withMaterial(SandboxAssets.testFontMaterial.withTint(RGBA.Yellow))
              .moveTo(ctx.parent.coords.unsafeToPoint)
          )
        )
      }
      .onClick(WindowEvent.OpenAt(windowId, Coords(20, 20)))

  val localesWindow: Window[ComponentGroup[Unit], Unit] =
    Window(
      id = windowId,
      snapGrid = Size.one,
      minSize = Dimensions(140, 90),
      content = windowChrome("Preferred locales")
    )
      .resizeTo(Dimensions(160, 100))
      .withBackground { windowContext =>
        Outcome(
          Layer.Content(
            Shape.Box(
              windowContext.bounds.unsafeToRectangle,
              if windowContext.hasFocus then Fill.Color(RGBA.SlateGray)
              else Fill.Color(RGBA.SlateGray.mix(RGBA.Black)),
              Stroke(1, RGBA.White)
            )
          )
        )
      }

  def windowChrome(title: String): ComponentGroup[Unit] =
    ComponentGroup()
      .withBoundsMode(BoundsMode.inherit)
      .anchor(
        localesPane,
        Anchor.TopLeft.withPadding(Padding(22, 2, 2, 2))
      )
      .anchor(
        titleBar(title)
          .onDrag { (_: Unit, dragData) =>
            Batch(
              WindowEvent.Move(
                windowId,
                dragData.position - dragData.offset,
                Space.Screen
              )
            )
          }
          .reportDrag
          .withBoundsType(BoundsType.FillWidth(20, Padding(0))),
        Anchor.TopLeft
      )
      .anchor(
        closeWindowButton.onClick(WindowEvent.Close(windowId)),
        Anchor.TopRight
      )

  def localesPane: ScrollPane[ComponentList[Unit], Unit] =
    ScrollPane(
      BindingKey("locale scroll pane"),
      Dimensions(156, 76),
      localeLabels,
      scrollButton
    )
      .withBackground { bounds =>
        Layer(
          Shape.Box(
            bounds.unsafeToRectangle,
            Fill.Color(RGBA.Black.withAlpha(0.5)),
            Stroke(1, RGBA.White)
          )
        )
      }
      .withScrollBackground { bounds =>
        Layer(
          Shape.Box(
            bounds.unsafeToRectangle,
            Fill.Color(RGBA.Yellow.mix(RGBA.Black)),
            Stroke.None
          )
        )
      }

  def localeLabels: ComponentList[Unit] =
    ComponentList(Dimensions(156, 76)) { (ctx: UIContext[Unit]) =>
      ctx.services.locale.preferred.zipWithIndex.map { case (locale, i) =>
        ComponentId("locale-" + i) ->
          Label[Unit](
            locale.toString,
            (ctx, text) => textBounds(ctx, text)
          ) { case (ctx, label) =>
            Outcome(
              Layer(
                textInstance
                  .withText(label.text(ctx))
                  .moveTo(ctx.parent.coords.unsafeToPoint)
              )
            )
          }
      }
    }
      .withLayout(ComponentLayout.Vertical(Padding(2)))

  def titleBar(title: String): Button[Unit] =
    Button[Unit](Bounds(Dimensions(0))) { (ctx, btn) =>
      Outcome(
        Layer(
          Shape
            .Box(
              btn.bounds.unsafeToRectangle,
              Fill.Color(RGBA.SlateGray.mix(RGBA.Yellow).mix(RGBA.Black)),
              Stroke(1, RGBA.White)
            )
            .moveTo(ctx.parent.coords.unsafeToPoint),
          textInstance
            .withText(title)
            .moveTo(ctx.parent.coords.unsafeToPoint + Point(4, 2))
        )
      )
    }

  def closeWindowButton: Button[Unit] =
    val size = Size(20, 20)

    makeButton(size) { coords =>
      val innerBox = Rectangle(size).contract(4).moveTo(coords + Point(4))

      Batch(
        Shape.Line(innerBox.topLeft, innerBox.bottomRight, Stroke(2, RGBA.Black)),
        Shape.Line(innerBox.bottomLeft, innerBox.topRight, Stroke(2, RGBA.Black))
      )
    }

  def scrollButton: Button[Unit] =
    makeButton(Size(16, 16))(_ => Batch.empty)

  def makeButton(size: Size)(extraNodes: Point => Batch[SceneNode]): Button[Unit] =
    Button[Unit](Bounds(Dimensions(size))) { (ctx, btn) =>
      Outcome(
        Layer(
          Shape
            .Box(
              btn.bounds.unsafeToRectangle,
              Fill.Color(RGBA.Magenta.mix(RGBA.Black)),
              Stroke(1, RGBA.Magenta)
            )
            .moveTo(ctx.parent.coords.unsafeToPoint)
        ).addNodes(extraNodes(ctx.parent.coords.unsafeToPoint))
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
          ).addNodes(extraNodes(ctx.parent.coords.unsafeToPoint))
        )
      }
      .presentOver { (ctx, btn) =>
        Outcome(
          Layer(
            Shape
              .Box(
                btn.bounds.unsafeToRectangle,
                Fill.Color(RGBA.Yellow.mix(RGBA.Black)),
                Stroke(1, RGBA.Yellow)
              )
              .moveTo(ctx.parent.coords.unsafeToPoint)
          ).addNodes(extraNodes(ctx.parent.coords.unsafeToPoint))
        )
      }
