package tyrian.ui.theme

import indigoengine.shared.datatypes.RGBA
import tyrian.Style
import tyrian.ui.Input
import tyrian.ui.TextBlock
import tyrian.ui.UIKey
import tyrian.ui.theme.Theme
import indigoengine.shared.datatypes.Degrees

class ThemeTests extends munit.FunSuite {

  test("Should render TextBlock with no styles when using Theme.NoStyles") {
    given Theme = Theme.None

    val actual = TextBlock("Hello World").toElem.toString

    // When Theme.NoStyles is used, an empty style attribute is present
    val expected = """<span>Hello World</span>"""

    assertEquals(actual, expected)
  }

  test("Should render TextBlock with styles when using Theme.Styles") {
    given Theme = Theme.default

    val actual = TextBlock("Hello World").toElem.toString

    val expectedStyles = Style(
      "font-family" -> "system-ui, ui-sans-serif, -apple-system, 'Segoe UI Variable', 'Segoe UI', Roboto, Ubuntu, Cantarell, 'Noto Sans', 'Helvetica Neue', Arial, 'Apple Color Emoji', 'Segoe UI Emoji', 'Noto Color Emoji', sans-serif",
      "font-size"   -> "1rem",
      "font-weight" -> "400",
      "color"       -> "rgba(51, 51, 51, 255)",
      "text-align"  -> "left",
      "line-height" -> "1.5rem",
      "white-space" -> "normal"
    )

    val expected = s"""<span style="${expectedStyles.asString}">Hello World</span>"""

    assertEquals(actual, expected)
  }

  test("Should render heading with no styles when using Theme.NoStyles") {
    given Theme = Theme.None

    val actual = TextBlock("Title").toHeading1.toElem.toString

    // Even headings get empty style attribute with NoStyles theme
    val expected = """<h1>Title</h1>"""

    assertEquals(actual, expected)
  }

  test("Should render heading with styles when using Theme.Styles") {
    given Theme = Theme.default

    val actual = TextBlock("Title").toHeading1.toElem.toString

    val expectedStyles = Style(
      "font-family" -> "system-ui, ui-sans-serif, -apple-system, 'Segoe UI Variable', 'Segoe UI', Roboto, Ubuntu, Cantarell, 'Noto Sans', 'Helvetica Neue', Arial, 'Apple Color Emoji', 'Segoe UI Emoji', 'Noto Color Emoji', sans-serif",
      "font-size"   -> "2rem",
      "font-weight" -> "600",
      "color"       -> "rgba(26, 26, 26, 255)",
      "text-align"  -> "left",
      "line-height" -> "1.2rem",
      "white-space" -> "normal"
    )

    val expected = s"""<h1 style="${expectedStyles.asString}">Title</h1>"""

    assertEquals(actual, expected)
  }

  test("Should preserve class names regardless of theme type") {
    val textBlock = TextBlock("Test").withClassNames(Set("custom-class", "another-class"))

    // Test with NoStyles
    {
      given Theme          = Theme.None
      val actualNoStyles   = textBlock.toElem.toString
      val expectedNoStyles = """<span class="custom-class another-class">Test</span>"""
      assertEquals(actualNoStyles, expectedNoStyles)
    }

    // Test with Styles
    {
      given Theme          = Theme.default
      val actualWithStyles = textBlock.toElem.toString
      val expectedStyles = Style(
        "font-family" -> "system-ui, ui-sans-serif, -apple-system, 'Segoe UI Variable', 'Segoe UI', Roboto, Ubuntu, Cantarell, 'Noto Sans', 'Helvetica Neue', Arial, 'Apple Color Emoji', 'Segoe UI Emoji', 'Noto Color Emoji', sans-serif",
        "font-size"   -> "1rem",
        "font-weight" -> "400",
        "color"       -> "rgba(51, 51, 51, 255)",
        "text-align"  -> "left",
        "line-height" -> "1.5rem",
        "white-space" -> "normal"
      )
      val expectedWithStyles =
        s"""<span style="${expectedStyles.asString}" class="custom-class another-class">Test</span>"""
      assertEquals(actualWithStyles, expectedWithStyles)
    }
  }

  test("Theme modifications should be ignored with NoStyles") {
    given Theme = Theme.None

    val actual = TextBlock("Test")
      .overrideTheme(_.withFontSize(tyrian.ui.datatypes.FontSize.Large))
      .toElem
      .toString

    // Theme override should have no effect with NoStyles (empty style attribute)
    val expected = """<span>Test</span>"""

    assertEquals(actual, expected)
  }

  test("Theme modifications should work with Styles theme") {
    given Theme = Theme.default

    val actual = TextBlock("Test")
      .overrideTheme(_.withFontSize(tyrian.ui.datatypes.FontSize.Large))
      .toElem
      .toString

    val expectedStyles = Style(
      "font-family" -> "system-ui, ui-sans-serif, -apple-system, 'Segoe UI Variable', 'Segoe UI', Roboto, Ubuntu, Cantarell, 'Noto Sans', 'Helvetica Neue', Arial, 'Apple Color Emoji', 'Segoe UI Emoji', 'Noto Color Emoji', sans-serif",
      "font-size"   -> "1.125rem", // Large font size
      "font-weight" -> "400",
      "color"       -> "rgba(51, 51, 51, 255)",
      "text-align"  -> "left",
      "line-height" -> "1.5rem",
      "white-space" -> "normal"
    )

    val expected = s"""<span style="${expectedStyles.asString}">Test</span>"""

    assertEquals(actual, expected)
  }

  test("Different text variants should all respect NoStyles") {
    given Theme = Theme.None

    val variants = List(
      (TextBlock("Normal").toNormal, "span", "Normal"),
      (TextBlock("Caption").toCaption, "span", "Caption"),
      (TextBlock("Code").toCode, "code", "Code"),
      (TextBlock("Label").toLabel, "label", "Label")
    )

    variants.foreach { case (textBlock, expectedTag, content) =>
      val actual   = textBlock.toElem.toString
      val expected = s"""<$expectedTag>$content</$expectedTag>"""
      assertEquals(actual, expected, s"Failed for variant with tag $expectedTag")
    }
  }

  // Theme color inheritance tests

  test("TextBlock should inherit text color from ThemeColors") {
    // Create a custom theme with a different text color
    val customColors = ThemeColors.default.withText(RGBA.Red)
    val customTheme  = Theme.Default.default.withColors(customColors)

    given Theme = customTheme

    val actual = TextBlock("Hello").toElem.toString

    // The text color should now be red (rgba(255, 0, 0, 255))
    assert(actual.contains("rgba(255, 0, 0, 255)"), s"Expected red text color, got: $actual")
  }

  test("TextBlock heading should inherit heading color from ThemeColors") {
    // Create a custom theme with a different heading color
    val customColors = ThemeColors.default.withTextHeading(RGBA.Blue)
    val customTheme  = Theme.Default.default.withColors(customColors)

    given Theme = customTheme

    val actual = TextBlock("Title").toHeading1.toElem.toString

    // The heading color should now be blue (rgba(0, 0, 255, 255))
    assert(actual.contains("rgba(0, 0, 255, 255)"), s"Expected blue heading color, got: $actual")
  }

  test("TextBlock caption should inherit muted text color from ThemeColors") {
    // Create a custom theme with a different muted color
    val customColors = ThemeColors.default.withTextMuted(RGBA.Green)
    val customTheme  = Theme.Default.default.withColors(customColors)

    given Theme = customTheme

    val actual = TextBlock("Caption text").toCaption.toElem.toString

    // The caption color should now be green (rgba(0, 255, 0, 255))
    assert(actual.contains("rgba(0, 255, 0, 255)"), s"Expected green muted color, got: $actual")
  }

  test("TextBlock code should inherit code color from ThemeColors") {
    // Create a custom theme with a different code color
    val customColors = ThemeColors.default.withTextCode(RGBA.Magenta)
    val customTheme  = Theme.Default.default.withColors(customColors)

    given Theme = customTheme

    val actual = TextBlock("some_code").toCode.toElem.toString

    // The code color should now be magenta (rgba(255, 0, 255, 255))
    assert(actual.contains("rgba(255, 0, 255, 255)"), s"Expected magenta code color, got: $actual")
  }

  test("Explicit text color override should take precedence over theme color") {
    // Create a custom theme with red text color
    val customColors = ThemeColors.default.withText(RGBA.Red)
    val customTheme  = Theme.Default.default.withColors(customColors)

    given Theme = customTheme

    // Override with blue
    val actual = TextBlock("Hello")
      .overrideTheme(_.withTextColor(RGBA.Blue))
      .toElem
      .toString

    // The text color should be blue (explicit override), not red (theme color)
    assert(actual.contains("rgba(0, 0, 255, 255)"), s"Expected blue override color, got: $actual")
  }

  test("Input should inherit input text color from ThemeColors") {
    val key          = UIKey("test-input")
    val customColors = ThemeColors.default.withInputText(RGBA.fromHex("#ff0000"))
    val customTheme  = Theme.Default.default.withColors(customColors)

    given Theme = customTheme

    val actual = Input(key).toElem.toString

    // The input text color should be red
    assert(actual.contains("rgba(255, 0, 0, 255)"), s"Expected red input text color, got: $actual")
  }

  test("Input should inherit disabled colors from ThemeColors") {
    val key          = UIKey("test-input")
    val customColors = ThemeColors.default.withDisabledText(RGBA.fromHex("#aaaaaa"))
    val customTheme  = Theme.Default.default.withColors(customColors)

    given Theme = customTheme

    val actual = Input(key).disabled.toElem.toString

    // The disabled input should use the custom disabled text color
    assert(actual.contains("rgba(170, 170, 170, 255)"), s"Expected custom disabled text color, got: $actual")
  }

  // ThemeColors convenience constructor tests

  test("ThemeColors.apply with primary and secondary creates valid theme") {
    val colors = ThemeColors(RGBA.Blue, RGBA.Orange, RGBA.White)

    // Primary and secondary should be set directly
    assertEquals(colors.primary, RGBA.Blue)
    assertEquals(colors.secondary, RGBA.Orange)
    assertEquals(colors.background, RGBA.White)

    // Text link should match primary
    assertEquals(colors.textLink, RGBA.Blue)
  }

  test("ThemeColors.apply with light background produces dark text") {
    val colors = ThemeColors(RGBA.Blue, RGBA.Orange, RGBA.White)

    // Text should be dark on light background
    assert(!colors.text.isLight, s"Text should be dark on light background, got: ${colors.text}")
    assert(!colors.textHeading.isLight, s"Heading text should be dark on light background")
  }

  test("ThemeColors.apply with dark background produces light text") {
    val colors = ThemeColors(RGBA.Blue, RGBA.Orange, RGBA.Black)

    // Text should be light on dark background
    assert(colors.text.isLight, s"Text should be light on dark background, got: ${colors.text}")
    assert(colors.textHeading.isLight, s"Heading text should be light on dark background")
  }

  test("ThemeColors.fromPrimary derives complementary secondary") {
    val colors = ThemeColors.fromPrimary(RGBA.Blue)

    // Secondary should be complementary (180° hue rotation)
    // Blue (240°) + 180° = 420° -> 60° (Yellow)
    val expectedSecondary = RGBA.Blue.rotateHue(Degrees(180))
    assertEquals(colors.secondary, expectedSecondary)
  }

  test("ThemeColors.fromPrimary with red produces cyan secondary") {
    val colors = ThemeColors.fromPrimary(RGBA.Red)

    // Red (0°) rotated 180° should give cyan
    val hsl = colors.secondary.toHSL
    assert(hsl.h > 175 && hsl.h < 185, s"Expected hue ~180 for cyan, got ${hsl.h}")
  }

  test("ThemeColors.highContrast uses pure black text on white background") {
    val colors = ThemeColors.highContrast(RGBA.Blue, RGBA.White)

    assertEquals(colors.text, RGBA.Black)
    assertEquals(colors.textHeading, RGBA.Black)
    assertEquals(colors.inputText, RGBA.Black)
  }

  test("ThemeColors.highContrast uses pure white text on black background") {
    val colors = ThemeColors.highContrast(RGBA.Blue, RGBA.Black)

    assertEquals(colors.text, RGBA.White)
    assertEquals(colors.textHeading, RGBA.White)
    assertEquals(colors.inputText, RGBA.White)
  }

  test("ThemeColors convenience constructors can be used with Theme") {
    val colors      = ThemeColors.fromPrimary(RGBA.Purple)
    val customTheme = Theme.Default.default.withColors(colors)

    given Theme = customTheme

    val actual = TextBlock("Test").toElem.toString

    // Just verify it renders without error and contains the text
    assert(actual.contains("Test"), "Should render the text content")
  }

  test("ThemeColors.apply two-arg version uses white background") {
    val colors = ThemeColors(RGBA.Blue, RGBA.Orange)

    assertEquals(colors.background, RGBA.White)
    // Should have dark text since background is white
    assert(!colors.text.isLight, "Text should be dark on white background")
  }

}
