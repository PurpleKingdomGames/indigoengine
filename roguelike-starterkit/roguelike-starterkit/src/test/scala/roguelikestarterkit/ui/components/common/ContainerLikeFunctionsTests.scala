package roguelikestarterkit.ui.components.common

import indigo.*
import roguelikestarterkit.syntax.*
import roguelikestarterkit.ui.components.*
import roguelikestarterkit.ui.components.group.*
import roguelikestarterkit.ui.datatypes.*

class ContainerLikeFunctionsTests extends munit.FunSuite:

  def calcBounds[ReferenceData]: (ReferenceData, String) => Bounds =
    (_, text) => Bounds(0, 0, text.length, 1)

  val present: (Coords, String, Dimensions) => Outcome[ComponentFragment] =
    (_, _, _) => Outcome(ComponentFragment.empty)

  test("calculateNextOffset labels") {

    val group: ComponentGroup[Unit] =
      ComponentGroup()
        .withLayout(
          ComponentLayout.Vertical(Padding.zero)
        )
        .add(
          Label("label 1", calcBounds)(present),
          Label("label 2", calcBounds)(present),
          Label("label 3", calcBounds)(present)
        )

    val updated: ComponentGroup[Unit] =
      group.refresh((), Dimensions(100, 100))

    val actual =
      ContainerLikeFunctions.calculateNextOffset[Unit](
        Dimensions(20, 20),
        updated.layout
      )((), updated.components)

    val expected =
      Coords(0, 3)

    assertEquals(actual, expected)
  }

  test("calculateNextOffset group of labels".only) {

    val group: ComponentGroup[Unit] =
      ComponentGroup()
        .withLayout(
          ComponentLayout.Vertical()
        )
        .add(
          ComponentGroup()
            .withLayout(
              ComponentLayout.Vertical()
            )
            .add(
              Label("label 1", calcBounds)(present),
              Label("label 2", calcBounds)(present),
              Label("label 3", calcBounds)(present)
            )
        )

    val parentDimensions = Dimensions(100, 100)

    val updated: ComponentGroup[Unit] =
      group.refresh((), parentDimensions)

    assertEquals(updated.contentBounds, Bounds(0, 0, 100, 3))
    assertEquals(updated.dimensions, Dimensions(100, 3))

    val actual =
      ContainerLikeFunctions.calculateNextOffset[Unit](
        Dimensions(100, 0), // The layout is dynamic and horizontal, so we'll only know the width
        updated.layout
      )((), updated.components)

    val expected =
      Coords(0, 3)

    assertEquals(actual, expected)
  }
