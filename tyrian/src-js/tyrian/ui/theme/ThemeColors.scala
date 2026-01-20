package tyrian.ui.theme

import indigoengine.shared.datatypes.Degrees
import indigoengine.shared.datatypes.RGBA

/** ThemeColors provides centralised semantic color definitions. Components can reference these colors instead of
  * hardcoding RGBA values, enabling consistent theming across the UI.
  */
final case class ThemeColors(
    // Core brand colors
    primary: RGBA,
    secondary: RGBA,
    // Text colors
    text: RGBA,
    textMuted: RGBA,
    textHeading: RGBA,
    textCode: RGBA,
    textLink: RGBA,
    // Background colors
    background: RGBA,
    backgroundMuted: RGBA,
    backgroundAlternate: RGBA,
    // Disabled states
    disabledText: RGBA,
    disabledBackground: RGBA,
    // Borders
    border: RGBA,
    borderMuted: RGBA,
    // Form-specific
    inputText: RGBA,
    inputBackground: RGBA
):
  def withPrimary(value: RGBA): ThemeColors             = this.copy(primary = value)
  def withSecondary(value: RGBA): ThemeColors           = this.copy(secondary = value)
  def withText(value: RGBA): ThemeColors                = this.copy(text = value)
  def withTextMuted(value: RGBA): ThemeColors           = this.copy(textMuted = value)
  def withTextHeading(value: RGBA): ThemeColors         = this.copy(textHeading = value)
  def withTextCode(value: RGBA): ThemeColors            = this.copy(textCode = value)
  def withTextLink(value: RGBA): ThemeColors            = this.copy(textLink = value)
  def withBackground(value: RGBA): ThemeColors          = this.copy(background = value)
  def withBackgroundMuted(value: RGBA): ThemeColors     = this.copy(backgroundMuted = value)
  def withBackgroundAlternate(value: RGBA): ThemeColors = this.copy(backgroundAlternate = value)
  def withDisabledText(value: RGBA): ThemeColors        = this.copy(disabledText = value)
  def withDisabledBackground(value: RGBA): ThemeColors  = this.copy(disabledBackground = value)
  def withBorder(value: RGBA): ThemeColors              = this.copy(border = value)
  def withBorderMuted(value: RGBA): ThemeColors         = this.copy(borderMuted = value)
  def withInputText(value: RGBA): ThemeColors           = this.copy(inputText = value)
  def withInputBackground(value: RGBA): ThemeColors     = this.copy(inputBackground = value)

object ThemeColors:

  def default: ThemeColors =
    ThemeColors(
      primary = RGBA.fromHex("#3366ff"),
      secondary = RGBA.fromHex("#ff3366"),
      text = RGBA.fromHex("#333333"),
      textMuted = RGBA.fromHex("#666666"),
      textHeading = RGBA.fromHex("#1a1a1a"),
      textCode = RGBA.fromHex("#d73a49"),
      textLink = RGBA.fromHex("#3366ff"),
      background = RGBA.fromHex("#ffffff"),
      backgroundMuted = RGBA.fromHex("#f5f5f5"),
      backgroundAlternate = RGBA.fromHex("#f9f9f9"),
      disabledText = RGBA.fromHex("#9ca3af"),
      disabledBackground = RGBA.fromHex("#f9fafb"),
      border = RGBA.fromHex("#333333"),
      borderMuted = RGBA.fromHex("#e5e7eb"),
      inputText = RGBA.fromHex("#374151"),
      inputBackground = RGBA.fromHex("#ffffff")
    )

  /** Create theme from primary and secondary brand colors. Derives all other colors automatically based on background
    * lightness.
    */
  def apply(primary: RGBA, secondary: RGBA, background: RGBA): ThemeColors =
    val isLightBg = background.isLight

    // Text colors - dark on light bg, light on dark bg
    val textBase =
      if isLightBg then RGBA.Black.lighten(0.1)
      else RGBA.White.darken(0.05)
    val textMutedColor   = textBase.mix(background, 0.4)
    val textHeadingColor = if isLightBg then textBase.darken(0.1) else textBase.lighten(0.1)

    // Backgrounds - subtle variations
    val bgMuted     = background.mix(textBase, 0.05)
    val bgAlternate = background.mix(textBase, 0.02)

    // Disabled states
    val disabledTxt = textBase.mix(background, 0.5)
    val disabledBg  = background.mix(textBase, 0.03)

    // Borders
    val borderColor      = textBase.mix(background, 0.7)
    val borderMutedColor = textBase.mix(background, 0.85)

    ThemeColors(
      primary = primary,
      secondary = secondary,
      text = textBase,
      textMuted = textMutedColor,
      textHeading = textHeadingColor,
      textCode = secondary.mix(textBase, 0.3),
      textLink = primary,
      background = background,
      backgroundMuted = bgMuted,
      backgroundAlternate = bgAlternate,
      disabledText = disabledTxt,
      disabledBackground = disabledBg,
      border = borderColor,
      borderMuted = borderMutedColor,
      inputText = textBase,
      inputBackground = background
    )

  /** Create theme from primary and secondary brand colors with white background. */
  def apply(primary: RGBA, secondary: RGBA): ThemeColors =
    apply(primary, secondary, RGBA.White)

  /** Create theme from just primary color. Secondary is derived as complementary (180° hue rotation).
    */
  def fromPrimary(primary: RGBA, background: RGBA): ThemeColors =
    val secondary = primary.rotateHue(Degrees(180))
    apply(primary, secondary, background)

  /** Create theme from just primary color with white background. */
  def fromPrimary(primary: RGBA): ThemeColors =
    fromPrimary(primary, RGBA.White)

  /** High contrast theme for accessibility with pure black/white text */
  def highContrast(primary: RGBA, background: RGBA): ThemeColors =
    val isLightBg = background.isLight
    val textBase  = if isLightBg then RGBA.Black else RGBA.White

    // Higher contrast muted text
    val textMutedColor = textBase.mix(background, 0.25)

    // Backgrounds - more pronounced variations
    val bgMuted     = background.mix(textBase, 0.08)
    val bgAlternate = background.mix(textBase, 0.04)

    // Disabled states still visible
    val disabledTxt = textBase.mix(background, 0.4)
    val disabledBg  = background.mix(textBase, 0.05)

    // Stronger borders
    val borderColor      = textBase.mix(background, 0.5)
    val borderMutedColor = textBase.mix(background, 0.7)

    val secondary = primary.rotateHue(Degrees(180))

    ThemeColors(
      primary = primary,
      secondary = secondary,
      text = textBase,
      textMuted = textMutedColor,
      textHeading = textBase,
      textCode = secondary.mix(textBase, 0.2),
      textLink = primary,
      background = background,
      backgroundMuted = bgMuted,
      backgroundAlternate = bgAlternate,
      disabledText = disabledTxt,
      disabledBackground = disabledBg,
      border = borderColor,
      borderMuted = borderMutedColor,
      inputText = textBase,
      inputBackground = background
    )

  /** High contrast theme for accessibility with white background */
  def highContrast(primary: RGBA): ThemeColors =
    highContrast(primary, RGBA.White)
