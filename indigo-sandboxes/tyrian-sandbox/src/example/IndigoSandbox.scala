package example

import cats.effect.IO
import example.common.ExchangeEvents
import example.common.ExchangeMsgs
import example.game.MyAwesomeGame
import indigo.*
import org.scalajs.dom.document
import tyrian.*
import tyrian.Html.*
import tyrian.classic.Nav
import tyrian.classic.cmds.Logger
import tyrian.classic.cmds.Random

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object IndigoSandbox extends App[Model]:

  val gameDivId1: String = "my-game-1"
  val gameDivId2: String = "my-game-2"

  def router: Location => Msg = Routing.externalOnly(Msg.NoOp, Msg.FollowLink(_))

  def init(flags: Map[String, String]): Result[Model] =
    Result(Model.init)

  def update(model: Model): GlobalMsg => Result[Model] =
    case Msg.NoOp =>
      Result(model)

    case Msg.Log(msg) =>
      Result(model)
        .log(msg)

    case Msg.HaltGame1 =>
      model.game1.foreach(_.halt())
      Result(model)

    case Msg.HaltGame2 =>
      model.game2.foreach(_.halt())
      Result(model)

    case Msg.RemoveGame1 =>
      Result(model.copy(showGame1 = false))
        .addActions(
          Action.run {
            document.getElementById(gameDivId1 + "-canvas").remove()
            Msg.NoOp
          }
        )

    case Msg.RemoveGame2 =>
      Result(model.copy(showGame2 = false))
        .addActions(
          Action.run {
            document.getElementById(gameDivId2 + "-canvas").remove()
            Msg.NoOp
          }
        )

    case Msg.FollowLink(href) =>
      Result(model).addCmds(Nav.loadUrl[IO](href))

    case Msg.NewRandomInt(i) =>
      Result(model.copy(randomNumber = i))

    case Msg.NewContent(content) =>
      Result(model.copy(field = content))
        .addCmds(
          Logger.info[IO]("New content: " + content) |+|
            Random.int[IO].map(next => Msg.NewRandomInt(next.value))
        )
        .addGlobalMsgs(
          ExchangeMsgs.IndigoToLog("reverse", content),
          ExchangeMsgs.IndigoToLog("combine", content)
        )

    case Msg.Insert =>
      Result(model.copy(components = Counter.init :: model.components))

    case Msg.Remove =>
      val cs = model.components.drop(1)
      Result(model.copy(components = cs))

    case Msg.Modify(id, m) =>
      val cs = model.components.zipWithIndex.map { case (c, i) =>
        if i == id then Counter.update(m, c) else c
      }

      Result(model.copy(components = cs))

    case ExchangeMsgs.TyrianToLog(msg) =>
      Result(model)
        .addCmds(Logger.stdout[IO]("(Tyrian) from indigo: " + msg))

    case _ =>
      Result(model)

  def view(model: Model): HtmlRoot =
    val counters = model.components.zipWithIndex.map { case (c, i) =>
      Counter.view(c).map(msg => Msg.Modify(i, msg))
    }.toList

    val elems = List(
      button(onClick(Msg.Remove))(text("remove")),
      button(onClick(Msg.Insert))(text("insert"))
    ) ++ counters

    HtmlRoot.div(
      HtmlFragment(
        Batch(
          div(hidden(false))("Random number: " + model.randomNumber.toString),
          div(
            a(href := "/another-page")("Internal link (will be ignored)"),
            br,
            a(href := "http://tyrian.indigoengine.io/")("Tyrian website")
          )
        ) ++
          (if model.showGame1 then Batch(div(id := gameDivId1)().setKey("game 1")) else Batch.empty) ++
          (if model.showGame2 then Batch(div(id := gameDivId2)().setKey("game 2")) else Batch.empty) ++
          Batch(
            div(
              button(onClick(Msg.HaltGame1))(text("Halt game 1")),
              button(onClick(Msg.RemoveGame1))(text("Remove game 1"))
            ),
            div(
              button(onClick(Msg.HaltGame2))(text("Halt game 2")),
              button(onClick(Msg.RemoveGame2))(text("Remove game 2"))
            ),
            div(
              input(placeholder := "Text to reverse", onInput(s => Msg.NewContent(s)), myStyle),
              div(myStyle)(text(model.field.reverse))
            ),
            div(elems)
          )
      )
    )

  def watchers(model: Model): Batch[Watcher] =
    Batch()

  def extensions(flags: Map[String, String], model: Model): Set[Extension] =
    Set(
      Indigo(
        ExtensionId("reverse"),
        flags,
        model.game1,
        () => Option(document.getElementById(gameDivId1)),
        Msg.Log("Game (1) start success."),
        Msg.Log("Game (1) start fail.")
      ).withEventMapping(ExchangeEvents.mapping),
      Indigo(
        ExtensionId("combine"),
        flags,
        model.game2,
        () => Option(document.getElementById(gameDivId2)),
        Msg.Log("Game (2) start success."),
        Msg.Log("Game (2) start fail.")
      ).withEventMapping(ExchangeEvents.mapping)
    )

  private val myStyle =
    styles(
      CSS.width("100%"),
      CSS.height("40px"),
      CSS.padding("10px 0"),
      CSS.`font-size`("2em"),
      CSS.`text-align`("center")
    )

enum Msg extends GlobalMsg:
  case NewContent(content: String)
  case Insert
  case Remove
  case Modify(i: Int, msg: Counter.Msg)
  case NewRandomInt(i: Int)
  case FollowLink(href: String)
  case NoOp
  case HaltGame1
  case HaltGame2
  case RemoveGame1
  case RemoveGame2
  case Log(msg: String)

object Counter:

  opaque type Model = Int

  def init: Model = 0

  enum Msg derives CanEqual:
    case Increment, Decrement

  def view(model: Model): Html[Msg] =
    div(
      button(onClick(Msg.Decrement))(text("-")),
      div(text(model.toString)),
      button(onClick(Msg.Increment))(text("+"))
    )

  def update(msg: Msg, model: Model): Model =
    msg match
      case Msg.Increment => model + 1
      case Msg.Decrement => model - 1

final case class Model(
    field: String,
    components: Batch[Counter.Model],
    randomNumber: Int,
    showGame1: Boolean,
    showGame2: Boolean,
    game1: MyAwesomeGame,
    game2: MyAwesomeGame
)
object Model:

  val init: Model =
    Model(
      "",
      Batch.empty,
      0,
      true,
      true,
      MyAwesomeGame("reverse", clockwise = true),
      MyAwesomeGame("combine", clockwise = false)
    )
