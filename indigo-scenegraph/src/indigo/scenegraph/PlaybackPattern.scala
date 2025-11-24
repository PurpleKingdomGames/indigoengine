package indigo.scenegraph

import indigo.core.audio.Track

/** Scene audio can either be played on a loop, or be silenced.
  */
enum PlaybackPattern derives CanEqual:
  case SingleTrackLoop(track: Track) extends PlaybackPattern
