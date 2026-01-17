package tyrian.ui.elements.stateful.textarea

import indigoengine.shared.optics.Lens
import tyrian.Elem
import tyrian.EmptyAttribute
import tyrian.GlobalMsg
import tyrian.Result
import tyrian.ui.UIElement
import tyrian.ui.UIKey
import tyrian.ui.elements.stateful.input.InputTheme
import tyrian.ui.elements.stateful.input.TextInputMsg
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeOverride

final case class TextArea(
    placeholder: String,
    isDisabled: Boolean,
    isReadOnly: Boolean,
    value: String,
    rows: Option[Int],
    cols: Option[Int],
    resize: ResizeMode,
    key: UIKey,
    classNames: Set[String],
    id: Option[String],
    themeOverride: ThemeOverride[InputTheme]
) extends UIElement.Stateful[TextArea, InputTheme]:

  def withPlaceholder(placeholder: String): TextArea =
    this.copy(placeholder = placeholder)

  def withDisabled(disabled: Boolean): TextArea =
    this.copy(isDisabled = disabled)
  def enabled: TextArea =
    withDisabled(false)
  def disabled: TextArea =
    withDisabled(true)

  def withReadonly(readOnly: Boolean): TextArea =
    this.copy(isReadOnly = readOnly)
  def readOnly: TextArea =
    withReadonly(true)
  def editable: TextArea =
    withReadonly(false)

  def withValue(value: String): TextArea =
    this.copy(value = value)

  def withRows(rows: Int): TextArea =
    this.copy(rows = Some(rows))

  def withCols(cols: Int): TextArea =
    this.copy(cols = Some(cols))

  def withResize(resize: ResizeMode): TextArea =
    this.copy(resize = resize)

  def withKey(value: UIKey): TextArea =
    this.copy(key = value)

  def withClassNames(classes: Set[String]): TextArea =
    this.copy(classNames = classes)

  def withId(id: String): TextArea =
    this.copy(id = Some(id))

  def themeLens: Lens[Theme.Default, InputTheme] =
    Lens(
      _.elements.input,
      (t, i) => t.withInputTheme(i)
    )

  def withThemeOverride(value: ThemeOverride[InputTheme]): TextArea =
    this.copy(themeOverride = value)

  def update: GlobalMsg => Result[TextArea] =
    case TextInputMsg.Changed(_key, v) if _key == key =>
      Result(this.copy(value = v))

    case TextInputMsg.Clear(_key) if _key == key =>
      Result(this.copy(value = ""))

    case _ =>
      Result(this)

  def view: Theme ?=> Elem[GlobalMsg] =
    TextArea.toHtml(this)

object TextArea:

  import tyrian.Html.*
  import tyrian.Style

  def apply(key: UIKey): TextArea =
    TextArea(
      placeholder = "",
      isDisabled = false,
      isReadOnly = false,
      value = "",
      rows = scala.None,
      cols = scala.None,
      resize = ResizeMode.Vertical,
      key,
      Set.empty,
      id = scala.None,
      ThemeOverride.NoOverride
    )

  def toHtml(t: TextArea)(using theme: Theme): tyrian.Elem[GlobalMsg] =
    val disabledAttr =
      if t.isDisabled then attribute("disabled", "true")
      else EmptyAttribute

    val readonlyAttr =
      if t.isReadOnly then attribute("readonly", "true")
      else EmptyAttribute

    val rowsAttr =
      t.rows.fold(EmptyAttribute)(r => attribute("rows", r.toString))

    val colsAttr =
      t.cols.fold(EmptyAttribute)(c => attribute("cols", c.toString))

    val styles =
      theme match
        case Theme.None =>
          Style.empty

        case tt: Theme.Default =>
          val baseStyles =
            if t.isDisabled then tt.elements.input.toDisabledStyles(theme)
            else tt.elements.input.toStyles(theme)
          baseStyles |+| Style("resize" -> t.resize.toCSSValue)

    val classAttribute =
      if t.classNames.isEmpty then EmptyAttribute
      else cls := t.classNames.mkString(" ")

    val idAttribute =
      t.id.fold(EmptyAttribute)(id.:=.apply)

    val textareaAttrs = List(
      tyrian.Html.placeholder := t.placeholder,
      onInput((s: String) => TextInputMsg.Changed(t.key, s)),
      disabledAttr,
      readonlyAttr,
      rowsAttr,
      colsAttr,
      style(styles),
      classAttribute,
      idAttribute
    )

    textarea(textareaAttrs*)(t.value)
