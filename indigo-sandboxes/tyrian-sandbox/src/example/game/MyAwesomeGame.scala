package example.game

import example.common.ExchangeEvents
import indigo.*

final case class MyAwesomeGame(id: String, clockwise: Boolean) extends Game[Unit, Unit, Unit]:

  val gameId: GameId = GameId(id)

  def initialScene(bootData: Unit): Option[SceneName] =
    None

  def scenes(bootData: Unit): NonEmptyBatch[Scene[Unit, Unit]] =
    NonEmptyBatch(GameScene(clockwise))

  val eventFilters: EventFilters =
    EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[Unit, Unit]] =
    Outcome(
      BootResult.noData(EngineConfig.default)
    )

  def initialModel(startupData: Unit): Outcome[Unit] =
    Outcome(())

  def setup(
      bootData: Unit,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def updateModel(
      context: Context[Unit],
      model: Unit
  ): GlobalEvent => Outcome[Unit] =
    case ExchangeEvents.IndigoToLog(gameId, msg) if gameId == id =>
      IndigoLogger.consoleLog(s"(Indigo) from tyrian: [$id] " + msg)
      val e =
        if clockwise then ExchangeEvents.TyrianToLog(msg.reverse)
        else ExchangeEvents.TyrianToLog(msg + "_" + msg)

      Outcome(model)
        .addGlobalEvents(e)

    case _ =>
      Outcome(model)

  def present(
      context: Context[Unit],
      model: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)
