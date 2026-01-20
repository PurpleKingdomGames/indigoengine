package tyrian.ui.elements.stateless.table

import indigoengine.shared.datatypes.RGBA
import tyrian.Style
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderCollapse
import tyrian.ui.theme.Theme

final case class TableTheme(
    background: Option[RGBA],
    border: Option[Border],
    borderCollapse: BorderCollapse,
    row: RowTheme,
    header: CellTheme,
    cell: CellTheme
):
  def withBackground(color: RGBA): TableTheme =
    this.copy(background = Some(color))
  def noBackground: TableTheme =
    this.copy(background = None)

  def withBorder(border: Border): TableTheme =
    this.copy(border = Some(border))
  def noBorder: TableTheme =
    this.copy(border = None)

  def withRowTheme(theme: RowTheme): TableTheme =
    this.copy(row = theme)
  def modifyRowTheme(f: RowTheme => RowTheme): TableTheme =
    this.copy(row = f(this.row))

  def withHeaderTheme(theme: CellTheme): TableTheme =
    this.copy(header = theme)
  def modifyHeaderTheme(f: CellTheme => CellTheme): TableTheme =
    this.copy(header = f(this.header))

  def withCellTheme(theme: CellTheme): TableTheme =
    this.copy(cell = theme)
  def modifyCellTheme(f: CellTheme => CellTheme): TableTheme =
    this.copy(cell = f(this.cell))

  def toTableStyles(theme: Theme): Style =
    theme match
      case Theme.None =>
        Style.empty

      case t: Theme.Default =>
        val bgStyle = background
          .orElse(Some(t.config.colors.background))
          .map(bg => Style("background-color" -> bg.toCSSValue))
          .getOrElse(Style.empty)

        val borderStyle = border.map(_.toStyle).getOrElse(Style.empty)

        Style(
          "border-collapse" -> borderCollapse.toCSSValue,
          "width"           -> "100%",
          "overflow"        -> "hidden"
        ) |+| bgStyle |+| borderStyle

  def toRowStyles(theme: Theme, isAlternate: Boolean): Style =
    theme match
      case Theme.None =>
        Style.empty

      case t: Theme.Default =>
        if isAlternate then
          row.alternative
            .orElse(row.background)
            .orElse(Some(t.config.colors.backgroundAlternate))
            .map(bg => Style("background-color" -> bg.toCSSValue))
            .getOrElse(Style.empty)
        else
          row.background
            .map(bg => Style("background-color" -> bg.toCSSValue))
            .getOrElse(Style.empty)

  def toHeaderStyles(theme: Theme): Style =
    header.toStyle(theme)

  def toCellStyles(theme: Theme): Style =
    cell.toStyle(theme)

object TableTheme:

  val default: TableTheme =
    TableTheme(
      background = None,
      border = None,
      borderCollapse = BorderCollapse.Separate,
      row = RowTheme.default,
      header = CellTheme.Defaults.header,
      cell = CellTheme.Defaults.cell
    )
