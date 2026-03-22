package indigo

final case class Settings(
    frameRatePolicy: FrameRatePolicy,
    antiAliasing: Boolean,
    premultipliedAlpha: Boolean,
    transparentBackground: Boolean,
    clickTime: Millis,
    disableContextMenu: Boolean
):

  def withFrameRatePolicy(value: FrameRatePolicy): Settings =
    this.copy(frameRatePolicy = value)
  def unlimitedFrameRate: Settings =
    withFrameRatePolicy(FrameRatePolicy.Unlimited)
  def targetFrameRate(target: FPS): Settings =
    withFrameRatePolicy(FrameRatePolicy.Skip(target))

  def withAntiAliasing(enabled: Boolean): Settings =
    this.copy(antiAliasing = enabled)
  def useAntiAliasing: Settings =
    withAntiAliasing(true)
  def noAntiAliasing: Settings =
    withAntiAliasing(false)

  def withPremultipliedAlpha(enabled: Boolean): Settings =
    this.copy(premultipliedAlpha = enabled)
  def usePremultipliedAlpha: Settings =
    withPremultipliedAlpha(true)
  def noPremultipliedAlpha: Settings =
    withPremultipliedAlpha(false)

  def withTransparentBackground(enabled: Boolean): Settings =
    this.copy(transparentBackground = enabled)
  def useTransparentBackground: Settings =
    withTransparentBackground(true)
  def noTransparentBackground: Settings =
    withTransparentBackground(false)

  def withClickTime(millis: Millis): Settings =
    this.copy(clickTime = millis)

  def withDisableContextMenu(disabled: Boolean): Settings =
    this.copy(disableContextMenu = disabled)
  def noContextMenu: Settings =
    withDisableContextMenu(true)
  def allowContextMenu: Settings =
    withDisableContextMenu(false)

object Settings:

  val default: Settings =
    Settings(
      FrameRatePolicy.Skip(FPS.`60`),
      antiAliasing = false,
      premultipliedAlpha = true,
      transparentBackground = true,
      clickTime = Millis(250),
      disableContextMenu = true
    )
