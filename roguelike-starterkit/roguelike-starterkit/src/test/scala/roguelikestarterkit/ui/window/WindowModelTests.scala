package roguelikestarterkit.ui.window

import indigo.*
import roguelikestarterkit.ui.component.*
import roguelikestarterkit.ui.components.common.ComponentLayout
import roguelikestarterkit.ui.components.common.Overflow
import roguelikestarterkit.ui.components.common.Padding
import roguelikestarterkit.ui.components.group.BoundsType
import roguelikestarterkit.ui.components.group.ComponentGroup
import roguelikestarterkit.ui.datatypes.*

class WindowModelTests extends munit.FunSuite:

  // instances

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

    def refresh(reference: Unit, model: Bounds, contentDimensions: Dimensions): Bounds =
      Bounds(contentDimensions)

  given Component[Bounds, Unit] with
    def bounds(reference: Unit, model: Bounds): Bounds =
      model

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

    def refresh(reference: Unit, model: Bounds, parentDimensions: Dimensions): Bounds =
      Bounds(parentDimensions)

  given Component[String, Unit] with
    def bounds(reference: Unit, model: String): Bounds =
      Bounds(0, 0, model.length, 1)

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

    def refresh(reference: Unit, model: String, parentDimensions: Dimensions): String =
      model

  // tests

  test("model cascades bounds changes") {

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

    assertEquals(
      model.withBounds(Bounds(10, 10, 20, 20)).refresh(()).contentModel,
      Bounds(11, 11, 18, 18)
    )
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
      .withBoundsType(BoundsType.fixed(100, 100))
      .add("abc")

  test("model cascades bounds changes to component group - BoundsType.Fixed") {
    val model: WindowModel[ComponentGroup[Unit], Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        group
      ).withBounds(Bounds(0, 0, 40, 40))
        .refresh(())

    assertEquals(model.contentModel.dimensions, Dimensions(100, 100))
  }

  test("model cascades bounds changes to component group - BoundsType.Inherit") {
    val model: WindowModel[ComponentGroup[Unit], Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        group.withBoundsType(BoundsType.inherit)
      ).withBounds(Bounds(0, 0, 40, 40))
        .refresh(())

    assertEquals(model.contentModel.dimensions, Dimensions(38, 38))
  }

  test("model cascades bounds changes to component group - BoundsType.Relative") {
    val model: WindowModel[ComponentGroup[Unit], Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        group.withBoundsType(BoundsType.relative(0.25, 0.25))
      ).withBounds(Bounds(0, 0, 40, 40))
        .refresh(())

    assertEquals(model.contentModel.dimensions, Dimensions(9, 9))
  }

  test("model cascades bounds changes to component group - BoundsType.Relative 100%") {
    val model: WindowModel[ComponentGroup[Unit], Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        group.withBoundsType(BoundsType.relative(1, 1))
      ).withBounds(Bounds(0, 0, 40, 40))
        .refresh(())

    assertEquals(model.contentModel.dimensions, Dimensions(38, 38))
  }

  test("model cascades bounds changes to component group - BoundsType.RelativePosition") {
    val model: WindowModel[ComponentGroup[Unit], Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        group.withBoundsType(BoundsType.relative(0.5, 0.5))
      ).withBounds(Bounds(0, 0, 40, 40))
        .refresh(())

    assertEquals(model.contentModel.dimensions, Dimensions(19, 19))
  }

  test("model cascades bounds changes to nested component groups") {

    val group =
      ComponentGroup(100, 100)
        .withLayout(ComponentLayout.Horizontal(Padding(5), Overflow.Wrap))
        .withBoundsType(BoundsType.inherit)
        .add(
          ComponentGroup(100, 100)
            .withLayout(ComponentLayout.Horizontal(Padding(5), Overflow.Wrap))
            .withBoundsType(BoundsType.relative(0.5, 0.5))
            .add(Bounds(0, 0, 0, 0))
        )

    val model: WindowModel[ComponentGroup[Unit], Unit] =
      WindowModel(
        WindowId("test"),
        charSheet,
        group
      ).withBounds(Bounds(0, 0, 40, 40))
        .refresh(())

    // Window cascades to top level component group
    assertEquals(model.contentModel.dimensions, Dimensions(38, 38))

    model.contentModel.components.headOption match
      case None =>
        fail("No sub components found")

      case Some(value) =>
        val cg = value.model.asInstanceOf[ComponentGroup[Unit]]

        // top comp group cascades to next level.
        assertEquals(cg.dimensions, Dimensions(19, 19))

        cg.components.headOption match
          case None =>
            fail("No sub components found 2")

          case Some(value) =>
            val b = value.model.asInstanceOf[Bounds]

            // second comp group cascades to next level.
            assertEquals(b.asInstanceOf[Bounds], Bounds(0, 0, 19, 19))

  }
