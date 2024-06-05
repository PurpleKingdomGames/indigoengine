package roguelikestarterkit.ui.components.group

import indigo.*
import roguelikestarterkit.Bounds
import roguelikestarterkit.Coords
import roguelikestarterkit.UiContext
import roguelikestarterkit.ui.component.*
import roguelikestarterkit.ui.components.common.*

class ComponentGroupTests extends munit.FunSuite:

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

    def refresh(reference: Unit, model: String, parentBounds: Bounds): String =
      model

    def cascade(model: String, parentBounds: Bounds): String =
      model

  // test("ComponentGroup.calculateContentBounds should return the correct bounds (Vertical)") {
  //   val group: ComponentGroup[Unit] =
  //     ComponentGroup()
  //       .withLayout(
  //         ComponentLayout.Vertical(Padding.zero.withBottom(2))
  //       )
  //       .withBoundsType(BoundsType.Fixed(Bounds(0, 0, 100, 100)))
  //       .add("abc", "def")

  //   val instance =
  //     summon[Component[ComponentGroup[Unit], Unit]]

  //   // This normally happens as part of the update process
  //   val processed = instance.refresh((), group, Bounds(0, 0, 100, 100))

  //   val actual =
  //     ComponentGroup.calculateContentBounds((), processed.components)

  //   val expected =
  //     Bounds(0, 0, 3, 4)

  //   assertEquals(actual, expected)
  // }

  // test("ComponentGroup.calculateContentBounds should return the correct bounds (Horizontal)") {
  //   val group: ComponentGroup[Unit] =
  //     ComponentGroup()
  //       .withLayout(
  //         ComponentLayout.Horizontal(Padding.zero.withRight(2))
  //       )
  //       .withBoundsType(BoundsType.Fixed(Bounds(0, 0, 100, 100)))
  //       .add("abc", "def")

  //   val instance =
  //     summon[Component[ComponentGroup[Unit], Unit]]

  //   // This normally happens as part of the update process
  //   val processed = instance.refresh((), group, Bounds(0, 0, 100, 100))

  //   val actual =
  //     ComponentGroup.calculateContentBounds((), processed.components)

  //   val expected =
  //     Bounds(0, 0, 8, 1)

  //   assertEquals(actual, expected)
  // }

  // // Write a test for ComponentGroup.calculateCascadeBounds
  // test("ComponentGroup.calculateCascadeBounds should return the correct bounds") {
  //   val group: ComponentGroup[Unit] =
  //     ComponentGroup()
  //       .withLayout(
  //         ComponentLayout.Vertical(Padding.zero.withBottom(2))
  //       )
  //       .withBoundsType(BoundsType.Fixed(Bounds(0, 0, 100, 100)))
  //       .add("abc", "def")

  //   val instance =
  //     summon[Component[ComponentGroup[Unit], Unit]]

  //   // This normally happens as part of the update process
  //   val processed =
  //     instance.refresh((), group, Bounds(0, 0, 100, 100))
  //   val updated =
  //     processed.copy(
  //       contentBounds = ComponentGroup.calculateContentBounds((), processed.components),
  //       dirty = false
  //     )

  //   val actualFixed =
  //     ComponentGroup.calculateCascadeBounds(
  //       updated.bounds,
  //       updated.contentBounds,
  //       Bounds(0, 0, 100, 100),
  //       updated.boundsType
  //     )

  //   assertEquals(actualFixed, Bounds(0, 0, 100, 100))

  //   val actualDefault =
  //     ComponentGroup.calculateCascadeBounds(
  //       updated.bounds,
  //       updated.contentBounds,
  //       Bounds(0, 0, 100, 100),
  //       BoundsType.default
  //     )

  //   assertEquals(actualDefault, Bounds(0, 0, 100, 4))
  // }

  test("refresh should re-apply the layout to all existing components") {
    val group: ComponentGroup[Unit] =
      ComponentGroup()
        .withLayout(
          ComponentLayout.Horizontal(Padding(5), Overflow.Wrap)
        )
        .withBoundsType(BoundsType.Fixed(Bounds(0, 0, 100, 100)))
        .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh((), group, Bounds(0, 0, 3, 5))
        .components
        .toList
        .map(_.offset)

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
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh((), group, Bounds(0, 0, 3, 5))
        .components
        .toList
        .map(_.offset)

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
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh((), group, Bounds(0, 0, 3, 5))
        .components
        .toList
        .map(_.offset)

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
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh((), group, Bounds(0, 0, 3, 5))
        .components
        .toList
        .map(_.offset)

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
        ComponentLayout.Horizontal(Padding(0), Overflow.Hidden)
      )
      .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh((), group, Bounds(0, 0, 3, 5))
        .components
        .toList
        .map(_.offset)

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
        ComponentLayout.Horizontal(Padding(5), Overflow.Hidden)
      )
      .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh((), group, Bounds(0, 0, 3, 5))
        .components
        .toList
        .map(_.offset)

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
        ComponentLayout.Horizontal(Padding(0, 0, 0, 5), Overflow.Hidden)
      )
      .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh((), group, Bounds(0, 0, 3, 5))
        .components
        .toList
        .map(_.offset)

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
        ComponentLayout.Horizontal(Padding(0), Overflow.Wrap)
      )
      .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh((), group, Bounds(0, 0, 3, 5))
        .components
        .toList
        .map(_.offset)

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
        ComponentLayout.Horizontal(Padding(5), Overflow.Wrap)
      )
      .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh((), group, Bounds(0, 0, 3, 5))
        .components
        .toList
        .map(_.offset)

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
        ComponentLayout.Horizontal(Padding(2, 0, 0, 5), Overflow.Wrap)
      )
      .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh((), group, Bounds(0, 0, 3, 5))
        .components
        .toList
        .map(_.offset)

    val expected =
      List(
        Coords(5, 2),
        Coords(5, 2 + 1)
      )

    assertEquals(actual, expected)
  }

  test("Refresh should snap to width of parent and height of contents by default.") {
    val c =
      summon[Component[ComponentGroup[Unit], Unit]]

    val group =
      ComponentGroup()
        .withLayout(
          ComponentLayout.Vertical(Padding.zero.withBottom(10))
        )
        .add("abc", "def")

    val updated: ComponentGroup[Unit] =
      c.refresh((), group, Bounds(0, 0, 100, 100))

    assertEquals(updated.contentBounds, Bounds(0, 0, 3, 12))
    assertEquals(updated.bounds, Bounds(0, 0, 100, 12))
  }
