// package roguelikestarterkit.ui.components.common

// /** `ComponentLayout` instructs a `ComponentGroup` how it should layout the components it contains.
//   * They are always placed one after another, optionally with some padding unless the layout type is
//   * `None`.
//   */
// enum ComponentLayout:
//   case Horizontal(padding: Padding, overflow: Overflow)
//   case Vertical(padding: Padding)

// object ComponentLayout:

//   object Horizontal:
//     def apply(): Horizontal =
//       Horizontal(Padding.zero, Overflow.Hidden)
//     def apply(padding: Padding): Horizontal =
//       Horizontal(padding, Overflow.Hidden)
//     def apply(overflow: Overflow): Horizontal =
//       Horizontal(Padding.zero, overflow)

//     extension (h: Horizontal)
//       def withPadding(value: Padding): Horizontal   = h.copy(padding = value)
//       def withOverflow(value: Overflow): Horizontal = h.copy(overflow = value)

//   object Vertical:
//     def apply(): Vertical =
//       Vertical(Padding.zero)

//     extension (h: Vertical) def withPadding(value: Padding): Vertical = h.copy(padding = value)
