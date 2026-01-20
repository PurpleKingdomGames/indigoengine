package tyrian.ui.elements.stateless.link

import indigoengine.shared.datatypes.RGBA
import tyrian.Style
import tyrian.ui.theme.Theme

final case class LinkTheme(
    unvisited: Option[RGBA]
):

  def withUnvisitedColor(color: RGBA): LinkTheme =
    this.copy(unvisited = Some(color))

  def inheritUnvisitedColor: LinkTheme =
    this.copy(unvisited = None)

  def toStyles(theme: Theme): Style =
    theme match
      case Theme.None =>
        Style.empty

      case t: Theme.Default =>
        val resolvedColor = unvisited.getOrElse(t.config.colors.textLink)
        Style("color" -> resolvedColor.toCSSValue)

object LinkTheme:

  val default: LinkTheme =
    LinkTheme(
      unvisited = None
    )
