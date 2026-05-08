package indigo.core.events

import indigo.core.assets.AssetType
import indigo.core.datatypes.BindingKey
import indigo.core.render.ScreenCaptureConfig

/** Events for capturing a snapshot of the rendered scene as an image asset.
  *
  * Capture is processed by the host platform layer (e.g. the Tyrian Indigo extension on the web), which re-renders the
  * scene with the requested layer filters, copies the result into an image, registers it with the asset system, and
  * responds with a `Captured` event.
  */
enum ScreenCaptureEvent extends GlobalEvent:

  /** Request a screen capture.
    *
    * @param config
    *   How the capture should be produced (name, crop, scale, excluded layers, image type).
    * @param key
    *   Tracking key used to correlate the request with the `Captured` (or `CaptureError`) response.
    */
  case Capture(config: ScreenCaptureConfig, key: BindingKey)

  /** Response event indicating that the capture has been produced and made available as an asset.
    *
    * @param key
    *   The tracking key from the original `Capture` request.
    * @param image
    *   The captured image, already registered with the asset system.
    */
  case Captured(key: BindingKey, image: AssetType.Image)

  /** Sent when a capture could not be produced.
    *
    * @param key
    *   The tracking key from the original `Capture` request.
    * @param message
    *   A human-readable description of the failure.
    */
  case CaptureError(key: BindingKey, message: String)
