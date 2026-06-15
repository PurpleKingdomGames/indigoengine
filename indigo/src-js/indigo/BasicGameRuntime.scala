package indigo

import indigo.*
import tyrian.*
import tyrian.ui.*

/** Provides a no-frills cut-down Tyrian runtime for running a single Indigo game. Its goal is to be uncomplicated while
  * offering a basic useful level of functionality.
  *
  * For anything more complicated, for instance if you want specific control over the view the game is drawn into, then
  * you'll want to build your own.
  *
  * The `Model` can be anything you wish, and only exists at the level of this runtime / Tyrian app. Your game cannot
  * see it directly.
  *
  * This runtime and your game can communicate by translating `GlobalEvent <-> GlobalMsg` and vice versa, and handling
  * those messages in the respective update functions. This allows you to ask the runtime to handle side effecting
  * operations such as HTTP requests and local storage, and to send relevant data back to your game once completed.
  */
trait BasicGameRuntime[Model] extends App[WebGL2Context, Model]:

  /** Kicks off an instance of your game.
    */
  def game: Game[?, ?, ?]

  /** Used to define your game's platform settings, such as the frame rate policy.
    */
  def settings: Settings

  /** Defines a partial two-way mapping of Indigo GlobalEvents and Tyrian GlobalMsgs. For example, if you want to save
    * your game you emit a GlobalEvent in Indigo, this mapping sees the event, and translates it to a GlobalMsg that can
    * be matched in the [[update]] function, you can then acknowledge the save by reversing the process.
    */
  def eventMapping: PartialIso[GlobalMsg, GlobalEvent]

  private val containerMarkerId = MarkerId("indigo-game-container")
  private given Theme           = Theme.None

  def extensions(flags: Map[String, String], model: Model): Set[Extension[WebGL2Context, HtmlFragment]] =
    Set(
      Indigo(
        ExtensionId("indigo game"),
        flags,
        game,
        containerMarkerId
      ).withSettings(settings)
        .withEventMapping(eventMapping)
    )

  def router: Location => GlobalMsg =
    Routing.none(RoutingDisabled.default)

  def view(model: Model): HtmlRoot =
    val surround: Batch[Elem[GlobalMsg]] => Html[GlobalMsg] =
      elems =>
        Container(
          Column(
            HtmlElement.many(elems)
          ).fillWidth.fillHeight
        )
          .withSize(
            Extent.CSS("100vw"),
            Extent.CSS("100vh")
          )
          .toHtml

    val fragment: HtmlFragment =
      HtmlFragment(
        Marker(containerMarkerId)
      )

    HtmlRoot(surround, fragment)

  def watchers(model: Model): Batch[Watcher] =
    Batch.empty

final case class RoutingDisabled(reason: String) extends GlobalMsg
object RoutingDisabled:
  val default: RoutingDisabled =
    RoutingDisabled("Routing is not used in the BasicGameRuntime.")
