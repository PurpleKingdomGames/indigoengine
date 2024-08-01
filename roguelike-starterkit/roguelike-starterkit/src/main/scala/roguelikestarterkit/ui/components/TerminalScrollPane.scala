package roguelikestarterkit.ui.components

import indigo.*
import roguelikestarterkit.tiles.Tile
import roguelikestarterkit.ui.component.Component
import roguelikestarterkit.ui.components.group.BoundsMode
import roguelikestarterkit.ui.components.group.ScrollPane
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.CharSheet
import roguelikestarterkit.ui.datatypes.Dimensions

object TerminalScrollPane:

  def apply[A, ReferenceData](
      bindingKey: BindingKey,
      content: A,
      charSheet: CharSheet
  )(using c: Component[A, ReferenceData]): ScrollPane[A, ReferenceData] =
    val theme = Theme(charSheet)
    ScrollPane(
      bindingKey,
      content,
      theme.scrollBar
    ).withScrollBackground(theme.background)

  def apply[A, ReferenceData](
      bindingKey: BindingKey,
      content: A,
      theme: Theme
  )(using c: Component[A, ReferenceData]): ScrollPane[A, ReferenceData] =
    ScrollPane(
      bindingKey,
      content,
      theme.scrollBar
    ).withScrollBackground(theme.background)

  def apply[A, ReferenceData](
      bindingKey: BindingKey,
      boundsMode: BoundsMode,
      content: A,
      charSheet: CharSheet
  )(using
      c: Component[A, ReferenceData]
  ): ScrollPane[A, ReferenceData] =
    val theme = Theme(charSheet)
    ScrollPane(
      bindingKey,
      boundsMode,
      content,
      theme.scrollBar
    ).withScrollBackground(theme.background)

  def apply[A, ReferenceData](
      bindingKey: BindingKey,
      boundsMode: BoundsMode,
      content: A,
      theme: Theme
  )(using
      c: Component[A, ReferenceData]
  ): ScrollPane[A, ReferenceData] =
    ScrollPane(
      bindingKey,
      boundsMode,
      content,
      theme.scrollBar
    ).withScrollBackground(theme.background)

  def apply[A, ReferenceData](
      bindingKey: BindingKey,
      dimensions: Dimensions,
      content: A,
      charSheet: CharSheet
  )(using
      c: Component[A, ReferenceData]
  ): ScrollPane[A, ReferenceData] =
    val theme = Theme(charSheet)
    ScrollPane(
      bindingKey,
      dimensions,
      content,
      theme.scrollBar
    ).withScrollBackground(theme.background)

  def apply[A, ReferenceData](
      bindingKey: BindingKey,
      dimensions: Dimensions,
      content: A,
      theme: Theme
  )(using
      c: Component[A, ReferenceData]
  ): ScrollPane[A, ReferenceData] =
    ScrollPane(
      bindingKey,
      dimensions,
      content,
      theme.scrollBar
    ).withScrollBackground(theme.background)

  def apply[A, ReferenceData](
      bindingKey: BindingKey,
      width: Int,
      height: Int,
      content: A,
      charSheet: CharSheet
  )(using
      c: Component[A, ReferenceData]
  ): ScrollPane[A, ReferenceData] =
    val theme = Theme(charSheet)
    ScrollPane(
      bindingKey,
      width,
      height,
      content,
      theme.scrollBar
    ).withScrollBackground(theme.background)

  def apply[A, ReferenceData](
      bindingKey: BindingKey,
      width: Int,
      height: Int,
      content: A,
      theme: Theme
  )(using
      c: Component[A, ReferenceData]
  ): ScrollPane[A, ReferenceData] =
    ScrollPane(
      bindingKey,
      width,
      height,
      content,
      theme.scrollBar
    ).withScrollBackground(theme.background)

  final case class Theme(
      charSheet: CharSheet,
      scrollBar: Button[Unit],
      background: Bounds => Layer
  )
  object Theme:
    private val defaultBgColor: Fill = Fill.Color(RGBA.White.mix(RGBA.Black, 0.8))

    def apply(charSheet: CharSheet): Theme = Theme(
      charSheet,
      TerminalButton
        .fromTile(
          Tile.`#`,
          TerminalButton.Theme(
            charSheet,
            RGBA.Black -> RGBA.Silver,
            RGBA.Black -> RGBA.White,
            RGBA.White -> RGBA.Black,
            hasBorder = false
          )
        ),
      bounds =>
        Layer(
          Shape.Box(
            bounds.toScreenSpace(charSheet.size),
            defaultBgColor
          )
        )
    )
