package indigo.internal.models

import indigo.Game
import indigo.Indigo
import indigo.internal.WorldEventWatchers
import indigo.internal.services.AudioPlayer
import tyrian.*

final case class Model(
    game: Game[?, ?, ?],
    attempts: Int,
    lastUpdatedAt: Seconds,
    running: Boolean,
    _eventWatchers: Option[WorldEventWatchers],
    _audioPlayer: AudioPlayer
)
object Model:
  def apply(game: Game[?, ?, ?]): Model =
    Model(
      game,
      Indigo.MaxStartupAttempts,
      Seconds.zero,
      running = true,
      None,
      new AudioPlayer()
    )
