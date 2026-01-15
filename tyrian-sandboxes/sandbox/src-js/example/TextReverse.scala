package example

import tyrian.*
import tyrian.Html.*

final case class TextReverse(textToReverse: String):

  def update: GlobalMsg => Result[TextReverse] =
    case TextReverseEvent.NewContent(value) =>
      Result(this.copy(textToReverse = value))

    case _ =>
      Result(this)

  def view: HtmlFragment =
    HtmlFragment.insert(
      MarkerIds.textReverse,
      div(
        input(placeholder := "Text to reverse", onInput(s => TextReverseEvent.NewContent(s)), myStyle),
        div(myStyle)(text(textToReverse.reverse))
      )
    )

  private def myStyle =
    styles(
      CSS.width("100%"),
      CSS.height("40px"),
      CSS.padding("10px 0"),
      CSS.`font-size`("2em"),
      CSS.`text-align`("center")
    )

object TextReverse:
  val initial: TextReverse =
    TextReverse("")

enum TextReverseEvent extends GlobalMsg:
  case NewContent(content: String)
