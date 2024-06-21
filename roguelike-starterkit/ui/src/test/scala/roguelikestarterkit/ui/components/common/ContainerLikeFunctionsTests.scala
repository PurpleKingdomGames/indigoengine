// package roguelikestarterkit.ui.components.common

// import indigo.*
// import roguelikestarterkit.syntax.*
// import roguelikestarterkit.ui.component.*
// import roguelikestarterkit.ui.components.*
// import roguelikestarterkit.ui.components.group.*
// import roguelikestarterkit.ui.datatypes.*

// class ContainerLikeFunctionsTests extends munit.FunSuite:

//   val charSheet =
//     CharSheet(
//       AssetName("test"),
//       Size(10),
//       Batch.empty,
//       FontKey("test")
//     )

//   test("calculateNextOffset labels") {

//     val c: Component[Label[Unit], Unit] =
//       summon[Component[Label[Unit], Unit]]

//     val group: ComponentGroup[Unit] =
//       ComponentGroup()
//         .withLayout(
//           ComponentLayout.Vertical(Padding.zero)
//         )
//         .add(
//           Label("label 1", Label.Theme(charSheet)),
//           Label("label 2", Label.Theme(charSheet)),
//           Label("label 3", Label.Theme(charSheet))
//         )

//     val updated: ComponentGroup[Unit] =
//       group.refresh((), Dimensions(100, 100))

//     val actual =
//       ContainerLikeFunctions.calculateNextOffset[Unit](
//         Dimensions(20, 20),
//         updated.layout
//       )((), updated.components)

//     val expected =
//       Coords(0, 3)

//     assertEquals(actual, expected)
//   }

//   test("calculateNextOffset group of labels".only) {

//     val group: ComponentGroup[Unit] =
//       ComponentGroup()
//         .withLayout(
//           ComponentLayout.Vertical()
//         )
//         .add(
//           ComponentGroup()
//             .withLayout(
//               ComponentLayout.Vertical()
//             )
//             .add(
//               Label("label 1", Label.Theme(charSheet)),
//               Label("label 2", Label.Theme(charSheet)),
//               Label("label 3", Label.Theme(charSheet))
//             )
//         )

//     val parentDimensions = Dimensions(100, 100)

//     val updated: ComponentGroup[Unit] =
//       group.refresh((), parentDimensions)

//     assertEquals(updated.contentBounds, Bounds(0, 0, 100, 3))
//     assertEquals(updated.dimensions, Dimensions(100, 3))

//     val actual =
//       ContainerLikeFunctions.calculateNextOffset[Unit](
//         Dimensions(100, 0), // The layout is dynamic and horizontal, so we'll only know the width
//         updated.layout
//       )((), updated.components)

//     val expected =
//       Coords(0, 3)

//     assertEquals(actual, expected)
//   }
