package tyrian.ui.elements.stateless.text

import indigoengine.shared.datatypes.RGBA
import tyrian.Style
import tyrian.ui.datatypes.FontSize
import tyrian.ui.datatypes.FontWeight
import tyrian.ui.datatypes.LineHeight
import tyrian.ui.datatypes.TextAlignment
import tyrian.ui.datatypes.TextDecoration
import tyrian.ui.datatypes.TextStyle
import tyrian.ui.datatypes.Wrapping
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeColors

final case class TextTheme(
    fontSize: FontSize,
    fontWeight: FontWeight,
    textColor: Option[RGBA],
    textColorFallback: ThemeColors => RGBA,
    alignment: TextAlignment,
    lineHeight: LineHeight,
    wrapping: Wrapping,
    style: TextStyle,
    decoration: TextDecoration
):

  def bold: TextTheme          = withFontWeight(FontWeight.Bold)
  def italic: TextTheme        = withStyle(TextStyle.Italic)
  def underlined: TextTheme    = withDecoration(TextDecoration.Underline)
  def strikethrough: TextTheme = withDecoration(TextDecoration.Strikethrough)

  def clearWeight: TextTheme     = withFontWeight(FontWeight.Normal)
  def clearStyle: TextTheme      = withStyle(TextStyle.Normal)
  def clearDecoration: TextTheme = withDecoration(TextDecoration.None)

  def alignLeft: TextTheme    = withAlignment(TextAlignment.Left)
  def alignCenter: TextTheme  = withAlignment(TextAlignment.Center)
  def alignRight: TextTheme   = withAlignment(TextAlignment.Right)
  def alignJustify: TextTheme = withAlignment(TextAlignment.Justify)

  def withFontSize(value: FontSize): TextTheme =
    this.copy(fontSize = value)

  def withFontWeight(value: FontWeight): TextTheme =
    this.copy(fontWeight = value)

  def withTextColor(value: RGBA): TextTheme =
    this.copy(textColor = Some(value))

  def inheritTextColor: TextTheme =
    this.copy(textColor = None)

  def withTextColorFallback(f: ThemeColors => RGBA): TextTheme =
    this.copy(textColorFallback = f)

  def withAlignment(value: TextAlignment): TextTheme =
    this.copy(alignment = value)

  def withLineHeight(value: LineHeight): TextTheme =
    this.copy(lineHeight = value)

  def withWrapping(value: Wrapping): TextTheme =
    this.copy(wrapping = value)
  def wrap: TextTheme =
    withWrapping(Wrapping.Wrap)
  def noWrap: TextTheme =
    withWrapping(Wrapping.NoWrap)

  def withStyle(value: TextStyle): TextTheme =
    this.copy(style = value)

  def withDecoration(value: TextDecoration): TextTheme =
    this.copy(decoration = value)

  def toStyles(theme: Theme): Style =
    theme match
      case Theme.None =>
        Style.empty

      case t: Theme.Default =>
        val resolvedColor = textColor.getOrElse(textColorFallback(t.config.colors))

        val baseStyle = Style(
          "font-family" -> t.config.fonts.body.toCSSValue,
          "font-size"   -> fontSize.toCSSValue,
          "font-weight" -> fontWeight.toCSSValue,
          "color"       -> resolvedColor.toCSSValue,
          "text-align"  -> alignment.toCSSValue,
          "line-height" -> lineHeight.toCSSValue,
          "white-space" -> wrapping.toTextCSSValue
        )

        val styleModifiers = List(
          if style != TextStyle.Normal then Some("font-style" -> style.toCSSValue) else None,
          if decoration != TextDecoration.None then Some("text-decoration" -> decoration.toCSSValue) else None
        ).flatten

        styleModifiers.foldLeft(baseStyle)((style, prop) => style |+| Style(prop))
