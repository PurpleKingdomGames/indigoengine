package tyrian.ui.elements.stateful.textarea

import indigoengine.shared.datatypes.RGBA
import tyrian.ui.UIKey
import tyrian.ui.datatypes.BorderRadius
import tyrian.ui.datatypes.BorderStyle
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.FontSize
import tyrian.ui.datatypes.FontWeight
import tyrian.ui.datatypes.Padding
import tyrian.ui.elements.stateful.input.TextInputMsg
import tyrian.ui.theme.Theme

class TextAreaTests extends munit.FunSuite {

  given Theme = Theme.default

  val testKey = UIKey("test-textarea")

  test("Should be able to render a basic textarea") {
    val actual = TextArea(testKey).toElem.toString

    assert(actual.contains("<textarea"), "Should contain textarea tag")
    assert(actual.contains("style="), "Should contain style attribute")
  }

  test("Should render textarea with placeholder") {
    val actual = TextArea(testKey)
      .withPlaceholder("Enter text here")
      .toElem
      .toString

    assert(actual.contains("""placeholder="Enter text here""""), "Should contain placeholder")
  }

  test("Should render textarea with value") {
    val actual = TextArea(testKey)
      .withValue("test value")
      .toElem
      .toString

    assert(actual.contains("test value"), "Should contain value text")
  }

  test("Should render disabled textarea") {
    val actual = TextArea(testKey).disabled.toElem.toString

    assert(actual.contains("""disabled="true""""), "Should contain disabled attribute")
  }

  test("Should render readonly textarea") {
    val actual = TextArea(testKey).readOnly.toElem.toString

    assert(actual.contains("""readonly="true""""), "Should contain readonly attribute")
  }

  test("Should render textarea with rows attribute") {
    val actual = TextArea(testKey).withRows(5).toElem.toString

    assert(actual.contains("""rows="5""""), "Should contain rows attribute")
  }

  test("Should render textarea with cols attribute") {
    val actual = TextArea(testKey).withCols(40).toElem.toString

    assert(actual.contains("""cols="40""""), "Should contain cols attribute")
  }

  test("Should apply resize mode None") {
    val actual = TextArea(testKey).withResize(ResizeMode.None).toElem.toString

    assert(actual.contains("resize:none"), "Should contain resize:none style")
  }

  test("Should apply resize mode Both") {
    val actual = TextArea(testKey).withResize(ResizeMode.Both).toElem.toString

    assert(actual.contains("resize:both"), "Should contain resize:both style")
  }

  test("Should apply resize mode Horizontal") {
    val actual = TextArea(testKey).withResize(ResizeMode.Horizontal).toElem.toString

    assert(actual.contains("resize:horizontal"), "Should contain resize:horizontal style")
  }

  test("Should apply resize mode Vertical (default)") {
    val actual = TextArea(testKey).toElem.toString

    assert(actual.contains("resize:vertical"), "Should contain resize:vertical style")
  }

  test("Should apply custom text color") {
    val textarea = TextArea(testKey)
      .overrideTheme(
        _.withTextColor(RGBA.Red)
      )

    val inputTheme = textarea.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.input).get
    assertEquals(inputTheme.textColor, Some(RGBA.Red))
  }

  test("Should apply custom background color") {
    val textarea =
      TextArea(testKey)
        .overrideTheme(_.withBackgroundColor(RGBA.Blue))

    val inputTheme = textarea.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.input).get
    assertEquals(inputTheme.backgroundColor, Some(RGBA.Blue))
  }

  test("Should apply custom border color") {
    val textarea =
      TextArea(testKey)
        .overrideTheme(_.withBorderColor(RGBA.Green))

    val inputTheme = textarea.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.input).get
    assertEquals(inputTheme.border.map(_.color).get, RGBA.Green)
  }

  test("Should apply custom border width") {
    val textarea =
      TextArea(testKey)
        .overrideTheme(_.withBorderWidth(BorderWidth.Medium))

    val inputTheme = textarea.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.input).get
    assertEquals(inputTheme.border.map(_.width).get, BorderWidth.Medium)
  }

  test("Should apply custom border style") {
    val textarea =
      TextArea(testKey)
        .overrideTheme(_.withBorderStyle(BorderStyle.Dashed))

    val inputTheme = textarea.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.input).get
    assertEquals(inputTheme.border.map(_.style).get, BorderStyle.Dashed)
  }

  test("Should apply custom border radius") {
    val textarea =
      TextArea(testKey)
        .overrideTheme(_.withBorderRadius(BorderRadius.Large))

    val inputTheme = textarea.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.input).get
    assertEquals(inputTheme.border.map(_.radius).get, BorderRadius.Large)
  }

  test("Should apply rounded modifier") {
    val textarea = TextArea(testKey).overrideTheme(_.rounded)

    val inputTheme = textarea.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.input).get
    assertEquals(inputTheme.border.map(_.radius).get, BorderRadius.Medium)
  }

  test("Should apply square modifier") {
    val textarea = TextArea(testKey).overrideTheme(_.square)

    val inputTheme = textarea.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.input).get
    assertEquals(inputTheme.border.map(_.radius).get, BorderRadius.None)
  }

  test("Should apply no border modifier") {
    val textarea = TextArea(testKey).overrideTheme(_.noBorder)

    val inputTheme = textarea.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.input).get
    assertEquals(inputTheme.border, None)
  }

  test("Should apply custom padding") {
    val textarea = TextArea(testKey)
      .overrideTheme(_.withPadding(Padding.Large))

    val inputTheme = textarea.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.input).get
    assertEquals(inputTheme.padding, Padding.Large)
  }

  test("Should apply custom font size") {
    val textarea = TextArea(testKey)
      .overrideTheme(_.withFontSize(FontSize.Large))

    val inputTheme = textarea.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.input).get
    assertEquals(inputTheme.fontSize, FontSize.Large)
  }

  test("Should apply custom font weight") {
    val textarea =
      TextArea(testKey)
        .overrideTheme(_.withFontWeight(FontWeight.Bold))

    val inputTheme = textarea.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.input).get
    assertEquals(inputTheme.fontWeight, FontWeight.Bold)
  }

  test("Should stack multiple theme modifications") {
    val textarea =
      TextArea(testKey)
        .overrideTheme(
          _.withTextColor(RGBA.Red)
            .withBackgroundColor(RGBA.Blue)
            .rounded
            .withPadding(Padding.Large)
        )

    val inputTheme = textarea.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.input).get
    assertEquals(inputTheme.textColor, Some(RGBA.Red))
    assertEquals(inputTheme.backgroundColor, Some(RGBA.Blue))
    assertEquals(inputTheme.border.map(_.radius).get, BorderRadius.Medium)
    assertEquals(inputTheme.padding, Padding.Large)
  }

  test("Should preserve all textarea properties when applying theme modifications") {
    val textarea =
      TextArea(testKey)
        .withPlaceholder("Test placeholder")
        .withValue("Test value")
        .withRows(5)
        .withCols(40)
        .withResize(ResizeMode.Both)
        .disabled
        .overrideTheme(_.withTextColor(RGBA.Red))

    assertEquals(textarea.placeholder, "Test placeholder")
    assertEquals(textarea.value, "Test value")
    assertEquals(textarea.rows, Some(5))
    assertEquals(textarea.cols, Some(40))
    assertEquals(textarea.resize, ResizeMode.Both)
    assertEquals(textarea.isDisabled, true)
    assertEquals(textarea.key, testKey)

    val inputTheme = textarea.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.input).get
    assertEquals(inputTheme.textColor, Some(RGBA.Red))
  }

  test("Should handle class names correctly") {
    val textarea =
      TextArea(testKey)
        .withClassNames(Set("custom-class", "another-class"))
        .overrideTheme(_.withTextColor(RGBA.Blue))

    assertEquals(textarea.classNames, Set("custom-class", "another-class"))

    val actual = textarea.toElem.toString
    assert(
      actual.contains("""class="custom-class another-class"""") ||
        actual.contains("""class="another-class custom-class""""),
      "Should contain class attribute with both classes"
    )
  }

  test("Should update textarea state correctly via Changed message") {
    val textarea =
      TextArea(testKey).withValue("initial")

    val updatedResult = textarea.update(TextInputMsg.Changed(testKey, "new value"))
    assertEquals(updatedResult.unsafeGet.value, "new value")
  }

  test("Should update textarea state correctly via Clear message") {
    val textarea =
      TextArea(testKey).withValue("initial")

    val clearedResult = textarea.update(TextInputMsg.Clear(testKey))
    assertEquals(clearedResult.unsafeGet.value, "")
  }

  test("Should ignore messages for different keys") {
    val textarea = TextArea(testKey).withValue("initial")
    val otherKey = UIKey("other-textarea")

    val result1 = textarea.update(TextInputMsg.Changed(otherKey, "new value"))
    assertEquals(result1.unsafeGet.value, "initial")

    val result2 = textarea.update(TextInputMsg.Clear(otherKey))
    assertEquals(result2.unsafeGet.value, "initial")
  }

}
