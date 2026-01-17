package tyrian.ui.elements.stateful.checkbox

import indigoengine.shared.datatypes.RGBA
import tyrian.Style
import tyrian.ui.datatypes.FontSize
import tyrian.ui.datatypes.Spacing
import tyrian.ui.theme.Theme

final case class CheckboxTheme(
    size: Spacing,
    accentColor: Option[RGBA],
    labelSpacing: Spacing,
    labelFontSize: FontSize,
    labelColor: RGBA,
    disabledLabelColor: RGBA
):

  def withSize(value: Spacing): CheckboxTheme =
    this.copy(size = value)

  def withAccentColor(value: RGBA): CheckboxTheme =
    this.copy(accentColor = Some(value))

  def noAccentColor: CheckboxTheme =
    this.copy(accentColor = scala.None)

  def withLabelSpacing(value: Spacing): CheckboxTheme =
    this.copy(labelSpacing = value)

  def withLabelFontSize(value: FontSize): CheckboxTheme =
    this.copy(labelFontSize = value)

  def withLabelColor(value: RGBA): CheckboxTheme =
    this.copy(labelColor = value)

  def withDisabledLabelColor(value: RGBA): CheckboxTheme =
    this.copy(disabledLabelColor = value)

  def toCheckboxStyles(theme: Theme): Style =
    theme match
      case Theme.None =>
        Style.empty

      case _: Theme.Default =>
        val accentStyle =
          accentColor.fold(Style.empty)(c => Style("accent-color" -> c.toCSSValue))

        Style(
          "width"  -> size.toCSSValue,
          "height" -> size.toCSSValue,
          "margin" -> "0",
          "cursor" -> "pointer"
        ) |+| accentStyle

  def toDisabledCheckboxStyles(theme: Theme): Style =
    toCheckboxStyles(theme) |+| Style("cursor" -> "not-allowed")

  def toLabelStyles(theme: Theme): Style =
    theme match
      case Theme.None =>
        Style.empty

      case t: Theme.Default =>
        Style(
          "font-family" -> t.config.fonts.body.toCSSValue,
          "font-size"   -> labelFontSize.toCSSValue,
          "color"       -> labelColor.toCSSValue,
          "margin-left" -> labelSpacing.toCSSValue,
          "cursor"      -> "pointer"
        )

  def toDisabledLabelStyles(theme: Theme): Style =
    theme match
      case Theme.None =>
        Style.empty

      case t: Theme.Default =>
        Style(
          "font-family" -> t.config.fonts.body.toCSSValue,
          "font-size"   -> labelFontSize.toCSSValue,
          "color"       -> disabledLabelColor.toCSSValue,
          "margin-left" -> labelSpacing.toCSSValue,
          "cursor"      -> "not-allowed"
        )

object CheckboxTheme:

  val default: CheckboxTheme =
    CheckboxTheme(
      size = Spacing.px(16),
      accentColor = scala.None,
      labelSpacing = Spacing.px(8),
      labelFontSize = FontSize.Small,
      labelColor = RGBA.fromHex("#374151"),
      disabledLabelColor = RGBA.fromHex("#9ca3af")
    )
