package indigo.internal.models

import indigo.Game
import indigo.Indigo
import indigo.internal.FrameScheduler
import indigo.internal.WorldEventWatchers
import indigo.internal.services.AudioPlayer

final case class Model(
    game: Game[?, ?, ?],
    attempts: Int,
    frameScheduler: FrameScheduler,
    running: Boolean,
    _eventWatchers: Option[WorldEventWatchers],
    _audioPlayer: AudioPlayer
)
object Model:
  def apply(game: Game[?, ?, ?]): Model =
    Model(
      game,
      Indigo.MaxStartupAttempts,
      FrameScheduler.Uninitialised,
      running = true,
      None,
      new AudioPlayer()
    )
