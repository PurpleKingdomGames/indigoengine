package example

import cats.effect.IO
import tyrian.*
import tyrian.Html.*
import tyrian.classic.Nav
import tyrian.next.*

final case class Model(
    topNav: TopNav,
    textReverse: TextReverse,
    counters: CounterManager
):

  def update: GlobalMsg => Result[Model] =
    case AppEvent.NoOp =>
      Result(this)

    case AppEvent.FollowLink(href) =>
      Result(this)
        .addActions(Nav.loadUrl[IO](href))

    case e =>
      for {
        tn <- topNav.update(e)
        tr <- textReverse.update(e)
        cs <- counters.update(e)
      } yield this.copy(
        topNav = tn,
        textReverse = tr,
        counters = cs
      )

  def view: HtmlFragment =
    topNav.view |+|
      HtmlFragment(
        div(
          Marker(MarkerIds.textReverse),
          Marker(MarkerIds.counterExt),
          Marker(MarkerIds.counters)
        )
      ) |+|
      counters.view |+| // Out of order rendering.
      textReverse.view

object Model:
  val init: Model =
    Model(TopNav.initial, TextReverse.initial, CounterManager.initial)
