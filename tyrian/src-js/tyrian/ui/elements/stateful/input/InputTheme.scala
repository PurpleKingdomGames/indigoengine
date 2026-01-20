package tyrian.ui.elements.stateful.input

import indigoengine.shared.datatypes.RGBA
import tyrian.Style
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderRadius
import tyrian.ui.datatypes.BorderStyle
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.FontSize
import tyrian.ui.datatypes.FontWeight
import tyrian.ui.datatypes.Padding
import tyrian.ui.datatypes.Spacing
import tyrian.ui.theme.Theme

final case class InputTheme(
    fontSize: FontSize,
    fontWeight: FontWeight,
    textColor: Option[RGBA],
    backgroundColor: Option[RGBA],
    border: Option[Border],
    padding: Padding,
    disabledBackgroundColor: Option[RGBA],
    disabledTextColor: Option[RGBA],
    disabledBorderColor: Option[RGBA]
):

  def withFontSize(value: FontSize): InputTheme =
    this.copy(fontSize = value)

  def withFontWeight(value: FontWeight): InputTheme =
    this.copy(fontWeight = value)

  def withTextColor(value: RGBA): InputTheme =
    this.copy(textColor = Some(value))

  def inheritTextColor: InputTheme =
    this.copy(textColor = None)

  def withBackgroundColor(value: RGBA): InputTheme =
    this.copy(backgroundColor = Some(value))

  def inheritBackgroundColor: InputTheme =
    this.copy(backgroundColor = None)

  def withBorder(border: Border): InputTheme =
    this.copy(border = Some(border))

  def noBorder: InputTheme =
    this.copy(border = None)

  def modifyBorder(f: Border => Border): InputTheme =
    withBorder(
      border match
        case Some(b) => f(b)
        case None    => f(Border.default)
    )

  def solidBorder(width: BorderWidth, color: RGBA): InputTheme =
    modifyBorder(_.withStyle(BorderStyle.Solid).withWidth(width).withColor(color))
  def dashedBorder(width: BorderWidth, color: RGBA): InputTheme =
    modifyBorder(_.withStyle(BorderStyle.Dashed).withWidth(width).withColor(color))

  def square: InputTheme       = withBorderRadius(BorderRadius.None)
  def rounded: InputTheme      = withBorderRadius(BorderRadius.Medium)
  def roundedSmall: InputTheme = withBorderRadius(BorderRadius.Small)
  def roundedLarge: InputTheme = withBorderRadius(BorderRadius.Large)
  def circular: InputTheme     = withBorderRadius(BorderRadius.Full)

  def withBorderRadius(radius: BorderRadius): InputTheme =
    modifyBorder(_.withRadius(radius))

  def withBorderColor(value: RGBA): InputTheme =
    modifyBorder(_.withColor(value))

  def withBorderWidth(value: BorderWidth): InputTheme =
    modifyBorder(_.withWidth(value))

  def withBorderStyle(value: BorderStyle): InputTheme =
    modifyBorder(_.withStyle(value))

  def withPadding(value: Padding): InputTheme =
    this.copy(padding = value)

  def withDisabledBackgroundColor(value: RGBA): InputTheme =
    this.copy(disabledBackgroundColor = Some(value))

  def inheritDisabledBackgroundColor: InputTheme =
    this.copy(disabledBackgroundColor = None)

  def withDisabledTextColor(value: RGBA): InputTheme =
    this.copy(disabledTextColor = Some(value))

  def inheritDisabledTextColor: InputTheme =
    this.copy(disabledTextColor = None)

  def withDisabledBorderColor(value: RGBA): InputTheme =
    this.copy(disabledBorderColor = Some(value))

  def inheritDisabledBorderColor: InputTheme =
    this.copy(disabledBorderColor = None)

  def toStyles(theme: Theme): Style =
    theme match
      case Theme.None =>
        Style.empty

      case t: Theme.Default =>
        val borderStyle  = border.map(_.toStyle).getOrElse(Style.empty)
        val resolvedText = textColor.getOrElse(t.config.colors.inputText)
        val resolvedBg   = backgroundColor.getOrElse(t.config.colors.inputBackground)

        Style(
          "font-family"      -> t.config.fonts.body.toCSSValue,
          "font-size"        -> fontSize.toCSSValue,
          "font-weight"      -> fontWeight.toCSSValue,
          "color"            -> resolvedText.toCSSValue,
          "background-color" -> resolvedBg.toCSSValue,
          "box-sizing"       -> "border-box",
          "outline"          -> "none"
        ) |+| borderStyle |+| padding.toStyle

  def toDisabledStyles(theme: Theme): Style =
    theme match
      case Theme.None =>
        toStyles(theme)

      case t: Theme.Default =>
        val resolvedDisabledText   = disabledTextColor.getOrElse(t.config.colors.disabledText)
        val resolvedDisabledBg     = disabledBackgroundColor.getOrElse(t.config.colors.disabledBackground)
        val resolvedDisabledBorder = disabledBorderColor.getOrElse(t.config.colors.borderMuted)

        toStyles(theme) |+| Style(
          "color"            -> resolvedDisabledText.toCSSValue,
          "background-color" -> resolvedDisabledBg.toCSSValue,
          "border-color"     -> resolvedDisabledBorder.toCSSValue,
          "cursor"           -> "not-allowed"
        )

object InputTheme:

  val default: InputTheme =
    InputTheme(
      fontSize = FontSize.Small,
      fontWeight = FontWeight.Normal,
      textColor = None,
      backgroundColor = None,
      border = None,
      padding = Padding(Spacing.px(8)),
      disabledBackgroundColor = None,
      disabledTextColor = None,
      disabledBorderColor = None
    )
