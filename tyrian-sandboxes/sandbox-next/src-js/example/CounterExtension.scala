package example

import tyrian.Html.*
import tyrian.next.*
import tyrian.next.extensions.*
import tyrian.ui.HtmlElement
import tyrian.ui.theme.Theme

object CounterExtension extends Extension:

  given Theme = Theme.None

  type ExtensionModel = Int

  def id: ExtensionId =
    ExtensionId("counter ext")

  def init: Outcome[ExtensionModel] =
    Outcome(0)

  def update(currentValue: Int): GlobalMsg => Outcome[ExtensionModel] =
    case CustomEvent.Increment =>
      Outcome(currentValue + 1)

    case CustomEvent.Decrement =>
      Outcome(currentValue - 1)

    case _ =>
      Outcome(currentValue)

  def view(currentValue: Int): HtmlFragment =
    HtmlFragment.insert(
      MarkerIds.counterExt,
      HtmlElement(
        div(
          h3("This counter was rendered by an extension."),
          button(onClick(CustomEvent.Decrement))(text("-")),
          div(text(currentValue.toString)),
          button(onClick(CustomEvent.Increment))(text("+")),
          br
        )
      )
    )

  def watchers(currentValue: Int): Batch[Watcher] =
    Batch.empty

  enum CustomEvent extends GlobalMsg:
    case Increment, Decrement
