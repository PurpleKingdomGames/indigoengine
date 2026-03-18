package tyrian.ui

import indigoengine.shared.optics.Lens
import tyrian.GlobalMsg
import tyrian.Result
import tyrian.ui.theme.ThemeOverride

trait UIElement[Component, ComponentTheme]:

  def classNames: Set[String]
  def withClassNames(classes: Set[String]): Component
  def withClassNames(classes: String*): Component    = withClassNames(classes.toSet)
  def addClassNames(classes: Set[String]): Component = withClassNames(classNames ++ classes)
  def addClassNames(classes: String*): Component     = addClassNames(classes.toSet)

  def id: Option[String]
  def withId(id: String): Component

  def themeOverride: ThemeOverride[ComponentTheme]
  def themeLens: Lens[Theme.Default, ComponentTheme]
  def withThemeOverride(value: ThemeOverride[ComponentTheme]): Component
  def noTheme: Component =
    withThemeOverride(ThemeOverride.NoTheme)
  def useDefaultTheme: Component =
    withThemeOverride(ThemeOverride.NoOverride)
  def overrideTheme(modify: ComponentTheme => ComponentTheme): Component =
    withThemeOverride(ThemeOverride.Override(modify))

  /** Warning: Should not be called directly. User provided implementation of a function to render the UIElement into a
    * Tyrian Elem[GlobalMsg] with the given theme, however, the correct way to render a UIElement is to call `toElem`,
    * which applies any theme overrides.
    */
  def view: Theme ?=> tyrian.Elem[GlobalMsg]

  /** Renders the current element to into a Tyrian Elem[GlobalMsg] with the given theme and theme overrides.
    */
  def toElem: Theme ?=> tyrian.Elem[GlobalMsg] =
    val overriddenTheme =
      summon[Theme] match
        case t @ Theme.None =>
          t

        case t: Theme.Default =>
          applyThemeOverrides(t)

    view(using overriddenTheme)

  def applyThemeOverrides(theme: Theme.Default): Theme =
    themeOverride match
      case ThemeOverride.RemoveTheme() =>
        Theme.None

      case ThemeOverride.DoNotOverride() =>
        theme

      case ThemeOverride.Override[ComponentTheme](modify) =>
        themeLens.set(theme, modify(themeLens.get(theme)))

object UIElement:

  trait Stateful[Component, ComponentTheme] extends UIElement[Component, ComponentTheme]:

    def key: UIKey
    def withKey(value: UIKey): Component

    def update: GlobalMsg => Result[Component]
