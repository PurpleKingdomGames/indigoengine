package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.component.*
import roguelikestarterkit.ui.components.group.BoundsType
import roguelikestarterkit.ui.components.group.ComponentGroup
import roguelikestarterkit.ui.components.group.ComponentLayout
import roguelikestarterkit.ui.components.group.Overflow
import roguelikestarterkit.ui.components.group.Padding
import roguelikestarterkit.ui.datatypes.*

class WindowModelTests extends munit.FunSuite:

  test("model cascades bounds changes") {

    given WindowContent[Bounds, Unit] with
      def updateModel(
          context: UiContext[Unit],
          model: Bounds
      ): GlobalEvent => Outcome[Bounds] =
        _ => Outcome(model)

      def present(
          context: UiContext[Unit],
          model: Bounds
      ): Outcome[Layer] =
        Outcome(Layer.empty)

      def cascade(model: Bounds, newBounds: Bounds): Bounds =
        newBounds

      def refresh(model: Bounds): Bounds =
        model

    val charSheet =
      CharSheet(
        AssetName("test"),
        Size(10),
        Batch.empty,
        FontKey("test")
      )

    val bounds =
      Bounds(0, 0, 10, 10)

    val model: WindowModel[Bounds, Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        bounds
      )

    assertEquals(model.withBounds(Bounds(10, 10, 20, 20)).contentModel, Bounds(11, 11, 18, 18))
  }

  val charSheet =
    CharSheet(
      AssetName("test"),
      Size(10),
      Batch.empty,
      FontKey("test")
    )

  val group =
    ComponentGroup()
      .withLayout(ComponentLayout.Horizontal(Padding(5), Overflow.Wrap))
      .withBoundsType(BoundsType.Fixed(Bounds(0, 0, 100, 100)))
      .add("abc")

  test("model cascades bounds changes to component group - BoundsType.Fixed") {
    val model: WindowModel[ComponentGroup[Unit], Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        group
      ).withBounds(Bounds(0, 0, 40, 40))

    assertEquals(model.contentModel.bounds, Bounds(0, 0, 100, 100))
  }

  test("model cascades bounds changes to component group - BoundsType.Inherit") {
    val model: WindowModel[ComponentGroup[Unit], Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        group.inheritBounds
      ).withBounds(Bounds(0, 0, 40, 40))

    assertEquals(model.contentModel.bounds, Bounds(1, 1, 38, 38))
  }

  test("model cascades bounds changes to component group - BoundsType.Relative") {
    val model: WindowModel[ComponentGroup[Unit], Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        group.relative(0.5, 0.5, 0.25, 0.25)
      ).withBounds(Bounds(0, 0, 40, 40))

    assertEquals(model.contentModel.bounds, Bounds(19, 19, 9, 9))
  }

  test("model cascades bounds changes to component group - BoundsType.Relative 100%") {
    val model: WindowModel[ComponentGroup[Unit], Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        group.relative(0, 0, 1, 1)
      ).withBounds(Bounds(0, 0, 40, 40))

    assertEquals(model.contentModel.bounds, Bounds(0, 0, 38, 38))
  }

  test("model cascades bounds changes to component group - BoundsType.RelativePosition") {
    val model: WindowModel[ComponentGroup[Unit], Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        group.relativePosition(0.5, 0.5)
      ).withBounds(Bounds(0, 0, 40, 40))

    assertEquals(model.contentModel.bounds, Bounds(19, 19, 100, 100))
  }

  test("model cascades bounds changes to component group - BoundsType.RelativeSize") {
    val model: WindowModel[ComponentGroup[Unit], Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        group.relativeSize(0.5, 0.5)
      ).withBounds(Bounds(0, 0, 40, 40))

    assertEquals(model.contentModel.bounds, Bounds(0, 0, 19, 19))
  }

  test("model cascades bounds changes to component group - BoundsType.Offset") {
    val model: WindowModel[ComponentGroup[Unit], Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        group.offset(5, 5, -5, -5)
      ).withBounds(Bounds(0, 0, 40, 40))

    assertEquals(model.contentModel.bounds, Bounds(6, 6, 33, 33))
  }

  test("model cascades bounds changes to component group - BoundsType.OffsetPosition") {
    val model: WindowModel[ComponentGroup[Unit], Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        group.offsetPosition(5, 5)
      ).withBounds(Bounds(0, 0, 40, 40))

    assertEquals(model.contentModel.bounds, Bounds(6, 6, 100, 100))
  }

  test("model cascades bounds changes to component group - BoundsType.OffsetSize") {
    val model: WindowModel[ComponentGroup[Unit], Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        group.offsetSize(-5, -5)
      ).withBounds(Bounds(0, 0, 40, 40))

    assertEquals(model.contentModel.bounds, Bounds(0, 0, 33, 33))
  }

  test("model cascades bounds changes to nested component groups") {

    val group =
      ComponentGroup(Bounds(0, 0, 100, 100))
        .withLayout(ComponentLayout.Horizontal(Padding(5), Overflow.Wrap))
        .inheritBounds
        .add(
          ComponentGroup(Bounds(0, 0, 100, 100))
            .withLayout(ComponentLayout.Horizontal(Padding(5), Overflow.Wrap))
            .offset(2, 4, -2, -4)
            .add(Bounds(0, 0, 0, 0))
        )

    val model: WindowModel[ComponentGroup[Unit], Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        group.inheritBounds
      ).withBounds(Bounds(0, 0, 40, 40))

    // Window cascades to top level component group
    assertEquals(model.contentModel.bounds, Bounds(1, 1, 38, 38))

    model.contentModel.components.headOption match
      case None =>
        fail("No sub components found")

      case Some(value) =>
        val cg = value.model.asInstanceOf[ComponentGroup[Unit]]

        // top comp group cascades to next level.
        assertEquals(cg.bounds, Bounds(3, 5, 36, 34))

        cg.components.headOption match
          case None =>
            fail("No sub components found 2")

          case Some(value) =>
            val b = value.model.asInstanceOf[Bounds]

            // second comp group cascades to next level.
            assertEquals(b.asInstanceOf[Bounds], Bounds(3, 5, 36, 34))

  }

  given Component[String, Unit] = new Component[String, Unit] {
    def bounds(model: String): Bounds = Bounds(0, 0, model.length, 1)

    def updateModel(
        context: UiContext[Unit],
        model: String
    ): GlobalEvent => Outcome[String] =
      _ => Outcome(model)

    def present(
        context: UiContext[Unit],
        model: String
    ): Outcome[ComponentFragment] =
      Outcome(ComponentFragment.empty)

    def reflow(model: String): String =
      model

    def cascade(model: String, parentBounds: Bounds): String =
      model
  }

  given Component[Bounds, Unit] = new Component[Bounds, Unit] {
    def bounds(model: Bounds): Bounds = model

    def updateModel(
        context: UiContext[Unit],
        model: Bounds
    ): GlobalEvent => Outcome[Bounds] =
      _ => Outcome(model)

    def present(
        context: UiContext[Unit],
        model: Bounds
    ): Outcome[ComponentFragment] =
      Outcome(ComponentFragment.empty)

    def reflow(model: Bounds): Bounds =
      model

    def cascade(model: Bounds, parentBounds: Bounds): Bounds =
      parentBounds
  }
