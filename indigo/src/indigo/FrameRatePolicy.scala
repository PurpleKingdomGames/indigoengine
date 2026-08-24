package indigo

/** Sets the frame rate policy to either `Unlimited` or `Skip`, in the latter case we 'skip' frames in order to meet the
  * target frame rate that you have supplied.
  *
  * The Indigo extension for Tyrian embeds your game into a Tyrian app (which serves as your games platform integration
  * point), but also controls the frame rate of your game. Frame rate implementations vary by platform, but in the
  * browser at least they strongly align to the refresh rate of the players display device.
  *
  * The refresh rate of the display is therefore a hard ceiling. Asking for 240 FPS on a 144 Hz display will get you
  * 144. Below that ceiling, `Skip` will hit the rate you asked for on average, whatever the display is doing: 60 FPS on
  * a 144 Hz display really is 60 FPS, and so is 60 FPS on a 60 Hz display.
  *
  * It manages that by treating the target as a deadline rather than a gap to measure. Each frame moves the deadline on
  * by exactly one frame's worth of time, so the leftover time either side of a frame is carried forward rather than
  * thrown away, and we run whichever display refresh lands closest to the deadline.
  *
  * The trade off is that when your target doesn't divide evenly into the refresh rate, individual frames arrive
  * unevenly even though the average is correct. At 60 FPS on a 144 Hz display, frames land two or three display
  * refreshes apart in a repeating pattern. `timeDelta` always reports the real elapsed time, so anything driven by it
  * stays correct; only fixed-step logic that assumes a perfectly even cadence would notice.
  *
  * You can also set the policy to be `Unlimited` and run at whatever rate the display offers, but for some games that
  * might not give you enough time to update your frames.
  */
enum FrameRatePolicy derives CanEqual:
  case Unlimited
  case Skip(target: FPS)
