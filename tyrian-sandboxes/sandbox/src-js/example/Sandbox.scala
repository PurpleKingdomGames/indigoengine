package example

import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Sandbox extends App[Model]:

  def router: Location => GlobalMsg =
    Routing.externalOnly(AppEvent.NoOp, AppEvent.FollowLink(_))

  def init(flags: Map[String, String]): Result[Model] =
    Result(Model.init)

  def update(model: Model): GlobalMsg => Result[Model] =
    case e =>
      model.update(e)

  def view(model: Model): HtmlRoot =
    HtmlRoot(model.view)

  def watchers(model: Model): Batch[Watcher] =
    Batch.empty

  def extensions(flags: Map[String, String], model: Model): Set[Extension] =
    Set(CounterExtension)
