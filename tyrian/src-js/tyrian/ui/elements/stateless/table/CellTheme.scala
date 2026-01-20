package tyrian.ui.elements.stateless.table

import indigoengine.shared.datatypes.RGBA
import tyrian.Style
import tyrian.ui.datatypes.FontSize
import tyrian.ui.datatypes.FontWeight
import tyrian.ui.datatypes.Padding
import tyrian.ui.theme.Theme

final case class CellTheme(
    background: Option[RGBA],
    fontWeight: Option[FontWeight],
    fontSize: Option[FontSize],
    padding: Option[Padding],
    textColor: Option[RGBA]
):

  def withBackground(color: RGBA): CellTheme =
    this.copy(background = Some(color))
  def noBackground: CellTheme =
    this.copy(background = None)

  def withFontWeight(weight: FontWeight): CellTheme =
    this.copy(fontWeight = Some(weight))
  def defaultFontWeight: CellTheme =
    this.copy(fontWeight = None)

  def withFontSize(size: FontSize): CellTheme =
    this.copy(fontSize = Some(size))
  def defaultFontSize: CellTheme =
    this.copy(fontSize = None)

  def withPadding(padding: Padding): CellTheme =
    this.copy(padding = Some(padding))
  def defaultPadding: CellTheme =
    this.copy(padding = None)

  def withTextColor(color: RGBA): CellTheme =
    this.copy(textColor = Some(color))
  def defaultTextColor: CellTheme =
    this.copy(textColor = None)

  def toStyle(theme: Theme): Style =
    theme match
      case Theme.None =>
        Style.empty

      case t: Theme.Default =>
        val resolvedFontWeight = fontWeight.getOrElse(FontWeight.Normal)
        val resolvedFontSize   = fontSize.getOrElse(FontSize.Medium)
        val resolvedTextColor  = textColor.getOrElse(t.config.colors.text)

        val baseStyle = Style(
          "font-weight" -> resolvedFontWeight.toCSSValue,
          "font-size"   -> resolvedFontSize.toCSSValue,
          "color"       -> resolvedTextColor.toCSSValue
        )

        val bgStyle = background
          .map(bg => Style("background-color" -> bg.toCSSValue))
          .getOrElse(Style.empty)

        val paddingStyle = padding.map(_.toStyle).getOrElse(Style.empty)

        baseStyle |+| bgStyle |+| paddingStyle

object CellTheme:

  object Defaults:

    val header: CellTheme =
      CellTheme(
        background = None,
        fontWeight = Some(FontWeight.Bold),
        fontSize = Some(FontSize.Large),
        padding = None,
        textColor = None
      )

    val cell: CellTheme =
      CellTheme(
        background = None,
        fontWeight = Some(FontWeight.Normal),
        fontSize = Some(FontSize.Medium),
        padding = None,
        textColor = None
      )
