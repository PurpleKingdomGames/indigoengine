package roguelikestarterkit.ui.components.group

import indigo.*
import roguelikestarterkit.Bounds
import roguelikestarterkit.Coords
import roguelikestarterkit.UiContext
import roguelikestarterkit.ui.component.*

class ComponentGroupTests extends munit.FunSuite {

  given Component[String, Unit] with
    def bounds(model: String): Bounds =
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

    def reflow(model: String): String =
      model

    def cascade(model: String, parentBounds: Bounds): String =
      model

  test("reflow should reapply the layout to all existing components") {
    val group: ComponentGroup[Unit] =
      ComponentGroup()
        .withLayout(
          ComponentLayout
            .Horizontal(Padding(5), Overflow.Wrap)
        )
        .withBoundsType(BoundsType.Fixed(Bounds(0, 0, 100, 100)))
        .add("abc", "def")

    val actual = group.reflow.components.toList.map(_.offset)

    val expected =
      List(
        Coords(5, 5),
        Coords(18, 5) // It's like this: 5 |3| 5.5 |3| 5
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - vertical, padding 0") {
    val group = ComponentGroup(Bounds(0, 0, 10, 5))
      .withLayout(ComponentLayout.Vertical(Padding(0)))
      .add("abc", "def")

    val actual =
      group.components.map(_.offset).toList

    val expected =
      List(
        Coords(0, 0),
        Coords(0, 1)
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - vertical, padding 5") {
    val group = ComponentGroup(Bounds(0, 0, 10, 5))
      .withLayout(ComponentLayout.Vertical(Padding(5)))
      .add("abc", "def")

    val actual =
      group.components.map(_.offset).toList

    val expected =
      List(
        Coords(5, 5),
        Coords(5, 5 + 1 + 5 + 5)
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - vertical, padding top=5") {
    val group = ComponentGroup(Bounds(0, 0, 10, 5))
      .withLayout(
        ComponentLayout.Vertical(Padding(5, 0, 0, 0))
      )
      .add("abc", "def")

    val actual =
      group.components.map(_.offset).toList

    val expected =
      List(
        Coords(0, 5),
        Coords(0, 5 + 1 + 5)
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - horizontal, padding 0, hidden") {
    val group = ComponentGroup(Bounds(0, 0, 5, 5))
      .withLayout(
        ComponentLayout
          .Horizontal(Padding(0), Overflow.Hidden)
      )
      .add("abc", "def")

    val actual =
      group.components.map(_.offset).toList

    val expected =
      List(
        Coords(0, 0),
        Coords(3, 0)
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - horizontal, padding 5, hidden") {
    val group = ComponentGroup(Bounds(0, 0, 5, 5))
      .withLayout(
        ComponentLayout
          .Horizontal(Padding(5), Overflow.Hidden)
      )
      .add("abc", "def")

    val actual =
      group.components.map(_.offset).toList

    val expected =
      List(
        Coords(5, 5),
        Coords(5 + 3 + 5 + 5, 5)
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - horizontal, padding left=5, hidden") {
    val group = ComponentGroup(Bounds(0, 0, 5, 5))
      .withLayout(
        ComponentLayout
          .Horizontal(Padding(0, 0, 0, 5), Overflow.Hidden)
      )
      .add("abc", "def")

    val actual =
      group.components.map(_.offset).toList

    val expected =
      List(
        Coords(5, 0),
        Coords(5 + 3 + 5, 0)
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - horizontal, padding 0, wrap") {
    val group = ComponentGroup(Bounds(0, 0, 5, 5))
      .withLayout(
        ComponentLayout
          .Horizontal(Padding(0), Overflow.Wrap)
      )
      .add("abc", "def")

    val actual =
      group.components.map(_.offset).toList

    val expected =
      List(
        Coords(0, 0),
        Coords(0, 1)
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - horizontal, padding 5, wrap") {
    val group = ComponentGroup(Bounds(0, 0, 5, 5))
      .withLayout(
        ComponentLayout
          .Horizontal(Padding(5), Overflow.Wrap)
      )
      .add("abc", "def")

    val actual =
      group.components.map(_.offset).toList

    val expected =
      List(
        Coords(5, 5),
        Coords(5, 5 + 1 + 5)
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - horizontal, padding left=5 top=2, wrap") {
    val group = ComponentGroup(Bounds(0, 0, 3, 5))
      .withLayout(
        ComponentLayout
          .Horizontal(Padding(2, 0, 0, 5), Overflow.Wrap)
      )
      .add("abc", "def")

    val actual =
      group.components.map(_.offset).toList

    val expected =
      List(
        Coords(5, 2),
        Coords(5, 2 + 1)
      )

    assertEquals(actual, expected)
  }

  test("Cascade should snap to width of parent and height of contents by default.") {
    val group: ComponentGroup[Unit] =
      ComponentGroup()
        .withLayout(
          ComponentLayout.Vertical(Padding(0, 0, 1, 0))
        )
        .add("abc", "def")
        .cascade(Bounds(0, 0, 100, 100))

    assertEquals(group.contentBounds, Bounds(0, 0, 3, 3))
    assertEquals(group.bounds, Bounds(0, 0, 100, 3))
  }
}
