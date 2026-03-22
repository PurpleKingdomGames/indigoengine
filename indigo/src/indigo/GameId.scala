package indigo

/** A simple identifier for your game. During construction, non-alphanumeric characters are converted to hyphens. */
opaque type GameId = String
object GameId:

  given CanEqual[GameId, GameId] = CanEqual.derived

  def apply(id: String): GameId =
    id.replaceAll("[^a-zA-Z0-9]", "-")

  extension (gid: GameId) def asString: String = gid
