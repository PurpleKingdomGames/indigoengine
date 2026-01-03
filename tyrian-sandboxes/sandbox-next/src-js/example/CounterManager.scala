package example

import tyrian.Html.*
import tyrian.next.*

final case class CounterManager(counters: List[Counter]):

  def update: GlobalMsg => Result[CounterManager] =
    case CounterManagerEvent.Modify(index, msg) =>
      val cs = counters.zipWithIndex.map { case (c, i) =>
        if i == index then c.update(msg) else c
      }

      Result(this.copy(counters = cs))

    case CounterManagerEvent.Insert =>
      Result(
        this.copy(
          counters = Counter.initial :: counters
        )
      )

    case CounterManagerEvent.Remove =>
      Result(this.copy(counters = counters.drop(1)))

    case _ =>
      Result(this)

  def view: HtmlFragment =
    HtmlFragment.insert(
      MarkerIds.counters,
      div(
        List(
          hr,
          h3("Counters"),
          button(onClick(CounterManagerEvent.Remove))(text("remove")),
          button(onClick(CounterManagerEvent.Insert))(text("insert"))
        ) ++
          counters.zipWithIndex.map { case (c, i) =>
            c.view.map(msg => CounterManagerEvent.Modify(i, msg))
          }
      )
    )

object CounterManager:
  val initial: CounterManager =
    CounterManager(Nil)

enum CounterManagerEvent extends GlobalMsg:
  case Modify(index: Int, msg: CounterEvent)
  case Insert
  case Remove
