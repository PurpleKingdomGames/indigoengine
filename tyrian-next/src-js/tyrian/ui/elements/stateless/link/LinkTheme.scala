package tyrian.ui.elements.stateless.link

import indigoengine.shared.datatypes.RGBA
import tyrian.Style

final case class LinkTheme(
    unvisited: Option[RGBA]
):

  def withUnvisitedColor(color: RGBA): LinkTheme =
    this.copy(unvisited = Some(color))

  def defaultUnvisitedColor: LinkTheme =
    this.copy(unvisited = None)

  def toStyles: Option[Style] =
    unvisited.map(color => Style("color" -> color.toCSSValue))

object LinkTheme:

  val default: LinkTheme =
    LinkTheme(
      unvisited = None
    )
