// package roguelikestarterkit.ui.components

// import indigo.*
// import indigo.syntax.*
// import roguelikestarterkit.shaders.material
// import roguelikestarterkit.terminal.RogueTerminalEmulator
// import roguelikestarterkit.terminal.TerminalMaterial
// import roguelikestarterkit.tiles.RoguelikeTiles10x10
// import roguelikestarterkit.tiles.RoguelikeTiles5x6
// import roguelikestarterkit.ui.component.ComponentFragment
// import roguelikestarterkit.ui.component.StatelessComponent
// import roguelikestarterkit.ui.components.common.TerminalTileColors
// import roguelikestarterkit.ui.datatypes.Bounds
// import roguelikestarterkit.ui.datatypes.CharSheet
// import roguelikestarterkit.ui.datatypes.Coords
// import roguelikestarterkit.ui.datatypes.Dimensions
// import roguelikestarterkit.ui.datatypes.UIContext

// import scala.annotation.targetName

// /** TextAreas are a simple `Component` that render text.
//   */
// final case class TextArea[ReferenceData](
//     text: ReferenceData => List[String],
//     render: (Coords, List[String], Dimensions) => Outcome[ComponentFragment],
//     calculateBounds: (ReferenceData, List[String]) => Bounds
// ):
//   def withText(value: String): TextArea[ReferenceData] =
//     this.copy(text = _ => value.split("\n").toList)
//   def withText(f: ReferenceData => String): TextArea[ReferenceData] =
//     this.copy(text = (r: ReferenceData) => f(r).split("\n").toList)

// object TextArea:

//   def apply[ReferenceData](text: String, calculateBounds: (ReferenceData, List[String]) => Bounds)(
//       present: (Coords, List[String], Dimensions) => Outcome[ComponentFragment]
//   ): TextArea[ReferenceData] =
//     TextArea(
//       (_: ReferenceData) => text.split("\n").toList,
//       present,
//       calculateBounds
//     )

//   @targetName("TextAreaRefToString")
//   def apply[ReferenceData](
//       text: ReferenceData => String,
//       calculateBounds: (ReferenceData, List[String]) => Bounds
//   )(
//       present: (Coords, List[String], Dimensions) => Outcome[ComponentFragment]
//   ): TextArea[ReferenceData] =
//     TextArea(
//       (r: ReferenceData) => text(r).split("\n").toList,
//       present,
//       calculateBounds
//     )

//   private def findBounds(text: List[String]): Bounds =
//     val maxLength =
//       text.foldLeft(0) { (acc, line) =>
//         if line.length > acc then line.length else acc
//       }
//     Bounds(0, 0, maxLength, text.length)

//   private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

//   private def presentTextArea(
//       charSheet: CharSheet,
//       fgColor: RGBA,
//       bgColor: RGBA
//   ): (Coords, List[String], Dimensions) => Outcome[ComponentFragment] = {
//     case (offset, label, dimensions) =>
//       val size = dimensions.unsafeToSize

//       val terminal =
//         RogueTerminalEmulator(size)
//           .putLines(Point.zero, Batch.fromList(label), fgColor, bgColor)
//           .toCloneTiles(
//             CloneId(s"label_${charSheet.assetName.toString}"),
//             offset.toScreenSpace(charSheet.size),
//             charSheet.charCrops
//           ) { case (fg, bg) =>
//             graphic.withMaterial(TerminalMaterial(charSheet.assetName, fg, bg))
//           }

//       Outcome(ComponentFragment(terminal))
//   }

//   /** Creates a TextArea rendered using the RogueTerminalEmulator based on a `TextArea.Theme`, with
//     * bounds based on the text length
//     */
//   def apply[ReferenceData](text: String, theme: Theme): TextArea[ReferenceData] =
//     val t = text.split("\n").toList

//     TextArea(
//       _ => t,
//       presentTextArea(
//         theme.charSheet,
//         theme.colors.foreground,
//         theme.colors.background
//       ),
//       (_, t) => findBounds(t)
//     )

//   /** Creates a TextArea rendered using the RogueTerminalEmulator based on a `TextArea.Theme`, with
//     * custom bounds
//     */
//   def apply[ReferenceData](text: ReferenceData => String, theme: Theme): TextArea[ReferenceData] =
//     TextArea(
//       (r: ReferenceData) => text(r).split("\n").toList,
//       presentTextArea(
//         theme.charSheet,
//         theme.colors.foreground,
//         theme.colors.background
//       ),
//       (r, t) => findBounds(text(r).split("\n").toList)
//     )

//   given [ReferenceData]: StatelessComponent[TextArea[ReferenceData], ReferenceData] with
//     def bounds(reference: ReferenceData, model: TextArea[ReferenceData]): Bounds =
//       model.calculateBounds(reference, model.text(reference))

//     def present(
//         context: UIContext[ReferenceData],
//         model: TextArea[ReferenceData]
//     ): Outcome[ComponentFragment] =
//       model.render(
//         context.bounds.coords,
//         model.text(context.reference),
//         bounds(context.reference, model).dimensions
//       )

//   final case class Theme(
//       charSheet: CharSheet,
//       colors: TerminalTileColors
//   ):
//     def withCharSheet(value: CharSheet): Theme =
//       this.copy(charSheet = value)

//     def withColors(foreground: RGBA, background: RGBA): Theme =
//       this.copy(colors = TerminalTileColors(foreground, background))

//   object Theme:
//     def apply(charSheet: CharSheet, foreground: RGBA, background: RGBA): Theme =
//       Theme(
//         charSheet,
//         TerminalTileColors(foreground, background)
//       )

//     def apply(charSheet: CharSheet): Theme =
//       Theme(charSheet, RGBA.White, RGBA.Black)
