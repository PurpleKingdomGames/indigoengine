package tyrian.ui.elements.stateless.html

import indigoengine.shared.collections.Batch
import indigoengine.shared.optics.Lens
import tyrian.GlobalMsg
import tyrian.ui.UIElement
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeOverride

final case class HtmlElement(
    elem: tyrian.Elem[GlobalMsg]
) extends UIElement[HtmlElement, Unit]:

  val classNames: Set[String]            = Set()
  val id: Option[String]                 = None
  val themeOverride: ThemeOverride[Unit] = ThemeOverride.NoOverride

  def withElem(elem: tyrian.Elem[GlobalMsg]): HtmlElement =
    this.copy(elem = elem)

  def withClassNames(classes: Set[String]): HtmlElement =
    this

  def withId(id: String): HtmlElement =
    this

  def themeLens: Lens[Theme.Default, Unit] =
    Lens.unit

  def withThemeOverride(f: ThemeOverride[Unit]): HtmlElement =
    this

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    HtmlElement.toHtml(this)

object HtmlElement:

  import tyrian.Html.*

  def raw(htmlString: String): HtmlElement =
    HtmlElement(elem = div().innerHtml(htmlString))

  def text(content: String): HtmlElement =
    HtmlElement(elem = span(content))

  def toHtml(element: HtmlElement): tyrian.Elem[GlobalMsg] =
    element.elem

  def of(elem: tyrian.Elem[GlobalMsg]): HtmlElement =
    HtmlElement(elem)

  def many(elems: Batch[tyrian.Elem[GlobalMsg]]): Batch[HtmlElement] =
    elems.map(of)
