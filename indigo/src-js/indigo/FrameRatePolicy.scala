package indigo

/** Sets the frame rate policy to either `Unlimited` or `Skip`, in the latter case we 'skip' frames in an attempt to
  * meet the target frame rate that you have supplied.
  *
  * The Indigo extension for Tyrian embeds your game into a Tyrian app (which serves as your games platform integration
  * point), but also controls the frame rate of your game. Frame rate implementations vary by platform, but in the
  * browser at least they strongly align to the refresh rate of the players display device.
  *
  * That being the case, we cannot guarantee specific frame rates, and we won't try.
  *
  * For example: Let's say that you'd like 60 frames per second, but your device refreshes at 144 Hz, that means that
  * updates will come in at a rate of 144 frames per second, or 2.4 frames for every 1 that we actually want. The
  * cleanest implementation for dealing with the fact that 144 isn't divisible by 60 is to skip frames until we hit the
  * frame rate, but the result is that when you ask for 60 FPS, you actually get a number like 48. Someone else playing
  * your game on a different refresh rate will get a different number. This does NOT mean your game or the engine are
  * slow. If you switch to a number that does divide well, like 72, then you may well get 72 FPS. You can also set the
  * policy to be `Unlimited` and get the full 144 FPS, but for some games that might not give you enough time to update
  * your frames.
  *
  * Previous iterations of Indigo attemptted to really hit the exact frame rate you asked for using various tricks, but
  * the decision was made to clean up and simplify and remove the weirdness!
  */
enum FrameRatePolicy derives CanEqual:
  case Unlimited
  case Skip(target: FPS)
