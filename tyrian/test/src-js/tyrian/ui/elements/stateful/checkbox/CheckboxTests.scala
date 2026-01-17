package tyrian.ui.elements.stateful.checkbox

import indigoengine.shared.datatypes.RGBA
import tyrian.ui.UIKey
import tyrian.ui.datatypes.FontSize
import tyrian.ui.datatypes.Spacing
import tyrian.ui.theme.Theme

class CheckboxTests extends munit.FunSuite {

  given Theme = Theme.default

  val testKey = UIKey("test-checkbox")

  test("Should be able to render a basic checkbox") {
    val actual = Checkbox(testKey).toElem.toString

    assert(actual.contains("<input"), "Should contain input tag")
    assert(actual.contains("""type="checkbox""""), "Should contain checkbox type")
  }

  test("Should render unchecked checkbox by default") {
    val checkbox = Checkbox(testKey)

    assertEquals(checkbox.isChecked, false)
  }

  test("Should render checked checkbox") {
    val actual = Checkbox(testKey).checked.toElem.toString

    assert(actual.contains("""checked="true""""), "Should contain checked attribute")
  }

  test("Should render unchecked checkbox explicitly") {
    val checkbox = Checkbox(testKey).checked.unchecked

    assertEquals(checkbox.isChecked, false)
    val actual = checkbox.toElem.toString
    assert(!actual.contains("""checked="true""""), "Should not contain checked attribute")
  }

  test("Should render disabled checkbox") {
    val actual = Checkbox(testKey).disabled.toElem.toString

    assert(actual.contains("""disabled="true""""), "Should contain disabled attribute")
  }

  test("Should render enabled checkbox explicitly") {
    val checkbox = Checkbox(testKey).disabled.enabled

    assertEquals(checkbox.isDisabled, false)
    val actual = checkbox.toElem.toString
    assert(!actual.contains("""disabled="true""""), "Should not contain disabled attribute")
  }

  test("Should render checkbox without label by default") {
    val actual = Checkbox(testKey).toElem.toString

    assert(!actual.contains("<label"), "Should not contain label wrapper")
    assert(!actual.contains("<span"), "Should not contain span for label text")
  }

  test("Should render checkbox with label") {
    val actual = Checkbox(testKey)
      .withLabel("Accept terms")
      .toElem
      .toString

    assert(actual.contains("<label"), "Should contain label wrapper")
    assert(actual.contains("Accept terms"), "Should contain label text")
  }

  test("Should render checkbox with label removed via noLabel") {
    val checkbox = Checkbox(testKey)
      .withLabel("Accept terms")
      .noLabel

    assertEquals(checkbox.label, None)
    val actual = checkbox.toElem.toString
    assert(!actual.contains("<label"), "Should not contain label wrapper")
  }

  test("Should apply custom size") {
    val checkbox = Checkbox(testKey)
      .overrideTheme(_.withSize(Spacing.px(24)))

    val checkboxTheme = checkbox.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.checkbox).get
    assertEquals(checkboxTheme.size, Spacing.px(24))
  }

  test("Should apply custom accent color") {
    val checkbox = Checkbox(testKey)
      .overrideTheme(_.withAccentColor(RGBA.Blue))

    val checkboxTheme = checkbox.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.checkbox).get
    assertEquals(checkboxTheme.accentColor, Some(RGBA.Blue))
  }

  test("Should remove accent color via noAccentColor") {
    val checkbox = Checkbox(testKey)
      .overrideTheme(_.withAccentColor(RGBA.Blue).noAccentColor)

    val checkboxTheme = checkbox.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.checkbox).get
    assertEquals(checkboxTheme.accentColor, None)
  }

  test("Should apply custom label spacing") {
    val checkbox = Checkbox(testKey)
      .overrideTheme(_.withLabelSpacing(Spacing.px(16)))

    val checkboxTheme = checkbox.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.checkbox).get
    assertEquals(checkboxTheme.labelSpacing, Spacing.px(16))
  }

  test("Should apply custom label font size") {
    val checkbox = Checkbox(testKey)
      .overrideTheme(_.withLabelFontSize(FontSize.Large))

    val checkboxTheme = checkbox.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.checkbox).get
    assertEquals(checkboxTheme.labelFontSize, FontSize.Large)
  }

  test("Should apply custom label color") {
    val checkbox = Checkbox(testKey)
      .overrideTheme(_.withLabelColor(RGBA.Red))

    val checkboxTheme = checkbox.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.checkbox).get
    assertEquals(checkboxTheme.labelColor, RGBA.Red)
  }

  test("Should apply custom disabled label color") {
    val checkbox = Checkbox(testKey)
      .overrideTheme(_.withDisabledLabelColor(RGBA.fromHex("#cccccc")))

    val checkboxTheme = checkbox.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.checkbox).get
    assertEquals(checkboxTheme.disabledLabelColor, RGBA.fromHex("#cccccc"))
  }

  test("Should stack multiple theme modifications") {
    val checkbox =
      Checkbox(testKey)
        .overrideTheme(
          _.withSize(Spacing.px(20))
            .withAccentColor(RGBA.Green)
            .withLabelFontSize(FontSize.Medium)
            .withLabelColor(RGBA.Blue)
        )

    val checkboxTheme = checkbox.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.checkbox).get
    assertEquals(checkboxTheme.size, Spacing.px(20))
    assertEquals(checkboxTheme.accentColor, Some(RGBA.Green))
    assertEquals(checkboxTheme.labelFontSize, FontSize.Medium)
    assertEquals(checkboxTheme.labelColor, RGBA.Blue)
  }

  test("Should preserve all checkbox properties when applying theme modifications") {
    val checkbox =
      Checkbox(testKey)
        .checked
        .withLabel("Test label")
        .disabled
        .overrideTheme(_.withAccentColor(RGBA.Red))

    assertEquals(checkbox.isChecked, true)
    assertEquals(checkbox.label, Some("Test label"))
    assertEquals(checkbox.isDisabled, true)
    assertEquals(checkbox.key, testKey)

    val checkboxTheme = checkbox.applyThemeOverrides(Theme.Default.default).toOption.map(_.elements.checkbox).get
    assertEquals(checkboxTheme.accentColor, Some(RGBA.Red))
  }

  test("Should handle class names correctly") {
    val checkbox =
      Checkbox(testKey)
        .withClassNames(Set("custom-class", "another-class"))
        .overrideTheme(_.withAccentColor(RGBA.Blue))

    assertEquals(checkbox.classNames, Set("custom-class", "another-class"))

    val actual = checkbox.toElem.toString
    assert(
      actual.contains("""class="custom-class another-class"""") ||
        actual.contains("""class="another-class custom-class""""),
      "Should contain class attribute with both classes"
    )
  }

  test("Should update checkbox state correctly via Toggled message to checked") {
    val checkbox = Checkbox(testKey).unchecked

    val updatedResult = checkbox.update(CheckboxMsg.Toggled(testKey, true))
    assertEquals(updatedResult.unsafeGet.isChecked, true)
  }

  test("Should update checkbox state correctly via Toggled message to unchecked") {
    val checkbox = Checkbox(testKey).checked

    val updatedResult = checkbox.update(CheckboxMsg.Toggled(testKey, false))
    assertEquals(updatedResult.unsafeGet.isChecked, false)
  }

  test("Should ignore messages for different keys") {
    val checkbox = Checkbox(testKey).unchecked
    val otherKey = UIKey("other-checkbox")

    val result = checkbox.update(CheckboxMsg.Toggled(otherKey, true))
    assertEquals(result.unsafeGet.isChecked, false)
  }

  test("Should generate checkbox styles with accent color") {
    val checkboxTheme = CheckboxTheme.default.withAccentColor(RGBA.Red)
    val styles        = checkboxTheme.toCheckboxStyles(Theme.Default.default)

    assert(clue(styles.asString).contains("accent-color"), "Should contain accent-color style")
  }

  test("Should generate checkbox styles without accent color when not set") {
    val checkboxTheme = CheckboxTheme.default.noAccentColor
    val styles        = checkboxTheme.toCheckboxStyles(Theme.Default.default)

    assert(!clue(styles.asString).contains("accent-color"), "Should not contain accent-color style")
  }

  test("Should generate disabled checkbox styles with not-allowed cursor") {
    val checkboxTheme = CheckboxTheme.default
    val styles        = checkboxTheme.toDisabledCheckboxStyles(Theme.Default.default)

    assert(clue(styles.asString).contains("cursor:not-allowed"), "Should contain not-allowed cursor")
  }

  test("Should generate label styles") {
    val checkboxTheme = CheckboxTheme.default.withLabelColor(RGBA.Red)
    val styles        = checkboxTheme.toLabelStyles(Theme.Default.default)

    assert(clue(styles.asString).contains("color:rgba(255, 0, 0, 255)"), "Should contain red label color")
    assert(clue(styles.asString).contains("cursor:pointer"), "Should contain pointer cursor")
  }

  test("Should generate disabled label styles") {
    val checkboxTheme = CheckboxTheme.default.withDisabledLabelColor(RGBA.fromHex("#808080"))
    val styles        = checkboxTheme.toDisabledLabelStyles(Theme.Default.default)

    assert(clue(styles.asString).contains("color:rgba(128, 128, 128, 255)"), "Should contain gray label color")
    assert(clue(styles.asString).contains("cursor:not-allowed"), "Should contain not-allowed cursor")
  }

}
