package indigo.internal.models

import indigo.GameId
import indigo.GlobalEvent
import indigo.core.assets.AssetName
import indigo.core.assets.AssetType
import indigo.core.audio.PlaybackPolicy
import indigo.core.audio.Volume
import indigo.core.datatypes.BindingKey
import indigo.core.render.ScreenCaptureConfig
import indigo.core.utils.LogLevel
import indigo.internal.models.FullScreenRequest
import indigo.internal.models.LaunchStatus
import tyrian.*

enum Msg extends GlobalMsg:
  case Halt(gameId: GameId)
  case Launch(status: LaunchStatus)
  case WorldEvents(events: Batch[GlobalEvent])
  case CanvasResize(width: Int, height: Int)
  case FullScreen(request: FullScreenRequest)
  case LoadAssets(assets: Set[AssetType], key: BindingKey, makeAvailable: Boolean)
  case CaptureScreen(config: ScreenCaptureConfig, key: BindingKey)
  case PlaySound(assetName: AssetName, volume: Volume, policy: PlaybackPolicy)
  case Log(level: LogLevel, message: String)
