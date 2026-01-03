package example

import tyrian.Html.*
import tyrian.next.*

final case class TopNav():

  def update: GlobalMsg => Result[TopNav] =
    case _ => Result(this)

  def view: HtmlFragment =
    HtmlFragment(
      div(
        a(href := "/another-page")("Internal link (will be ignored)"),
        br,
        a(href := "http://tyrian.indigoengine.io/")("Tyrian website")
      )
    )

object TopNav:
  val initial: TopNav =
    TopNav()
