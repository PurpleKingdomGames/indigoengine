package tyrian.ui.elements.stateful.checkbox

import indigoengine.shared.optics.Lens
import tyrian.Elem
import tyrian.EmptyAttribute
import tyrian.GlobalMsg
import tyrian.Result
import tyrian.Tyrian
import tyrian.ui.UIElement
import tyrian.ui.UIKey
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeOverride

final case class Checkbox(
    isChecked: Boolean,
    isDisabled: Boolean,
    label: Option[String],
    key: UIKey,
    classNames: Set[String],
    id: Option[String],
    themeOverride: ThemeOverride[CheckboxTheme]
) extends UIElement.Stateful[Checkbox, CheckboxTheme]:

  def withChecked(checked: Boolean): Checkbox =
    this.copy(isChecked = checked)
  def checked: Checkbox =
    withChecked(true)
  def unchecked: Checkbox =
    withChecked(false)

  def withDisabled(disabled: Boolean): Checkbox =
    this.copy(isDisabled = disabled)
  def enabled: Checkbox =
    withDisabled(false)
  def disabled: Checkbox =
    withDisabled(true)

  def withLabel(label: String): Checkbox =
    this.copy(label = Some(label))
  def noLabel: Checkbox =
    this.copy(label = scala.None)

  def withKey(value: UIKey): Checkbox =
    this.copy(key = value)

  def withClassNames(classes: Set[String]): Checkbox =
    this.copy(classNames = classes)

  def withId(id: String): Checkbox =
    this.copy(id = Some(id))

  def themeLens: Lens[Theme.Default, CheckboxTheme] =
    Lens(
      _.elements.checkbox,
      (t, c) => t.withCheckboxTheme(c)
    )

  def withThemeOverride(value: ThemeOverride[CheckboxTheme]): Checkbox =
    this.copy(themeOverride = value)

  def update: GlobalMsg => Result[Checkbox] =
    case CheckboxMsg.Toggled(_key, newChecked) if _key == key =>
      Result(this.copy(isChecked = newChecked))

    case _ =>
      Result(this)

  def view: Theme ?=> Elem[GlobalMsg] =
    Checkbox.toHtml(this)

object Checkbox:

  import tyrian.Html.*
  import tyrian.Style

  def apply(key: UIKey): Checkbox =
    Checkbox(
      isChecked = false,
      isDisabled = false,
      label = scala.None,
      key,
      Set.empty,
      id = scala.None,
      ThemeOverride.NoOverride
    )

  def toHtml(c: Checkbox)(using theme: Theme): tyrian.Elem[GlobalMsg] =
    val disabledAttr =
      if c.isDisabled then attribute("disabled", "true")
      else EmptyAttribute

    val checkedAttr =
      if c.isChecked then attribute("checked", "true")
      else EmptyAttribute

    val checkboxStyles =
      theme match
        case Theme.None =>
          Style.empty

        case tt: Theme.Default =>
          if c.isDisabled then tt.elements.checkbox.toDisabledCheckboxStyles(theme)
          else tt.elements.checkbox.toCheckboxStyles(theme)

    val classAttribute =
      if c.classNames.isEmpty then EmptyAttribute
      else cls := c.classNames.mkString(" ")

    val idAttribute =
      c.id.fold(EmptyAttribute)(id.:=.apply)

    val checkboxInput = input(
      typ := "checkbox",
      onEvent(
        "change",
        (e: Tyrian.Event) => CheckboxMsg.Toggled(c.key, e.target.asInstanceOf[Tyrian.HTMLInputElement].checked)
      ),
      disabledAttr,
      checkedAttr,
      style(checkboxStyles),
      classAttribute,
      idAttribute
    )

    c.label match
      case scala.None =>
        checkboxInput

      case Some(labelText) =>
        val labelStyles =
          theme match
            case Theme.None =>
              Style.empty

            case tt: Theme.Default =>
              if c.isDisabled then tt.elements.checkbox.toDisabledLabelStyles(theme)
              else tt.elements.checkbox.toLabelStyles(theme)

        val containerStyles = Style(
          "display"     -> "inline-flex",
          "align-items" -> "center"
        )

        tyrian.Html.label(style(containerStyles))(
          checkboxInput,
          span(style(labelStyles))(labelText)
        )

enum CheckboxMsg extends GlobalMsg:
  case Toggled(id: UIKey, checked: Boolean)
