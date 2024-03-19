package roguelikestarterkit.ui.component

import indigo.*
import roguelikestarterkit.*

class ComponentGroupTests extends munit.FunSuite {

  given Component[String] = new Component[String] {
    def bounds(model: String): Bounds = Bounds(0, 0, model.length, 1)

    def updateModel[StartupData, ContextData](
        context: UiContext[StartupData, ContextData],
        model: String
    ): GlobalEvent => Outcome[String] =
      _ => Outcome(model)

    def present[StartupData, ContextData](
        context: UiContext[StartupData, ContextData],
        model: String
    ): Outcome[ComponentFragment] =
      Outcome(ComponentFragment.empty)

    def reflow(model: String): String =
      model
  }

  test("reflow should reapply the layout to all existing components") {
    val component1 = ComponentEntry(Coords(0, 0), "abc", summon[Component[String]])
    val component2 = ComponentEntry(Coords(10, 10), "def", summon[Component[String]])
    val group = ComponentGroup(
      Bounds(0, 0, 100, 100),
      ComponentLayout.Horizontal(Padding(5), Overflow.Wrap),
      Batch(component1, component2)
    )

    val actual = group.reflow.components.toList.map(_.offset)

    val expected =
      List(
        Coords(5, 5),
        Coords(18, 5) // It's like this: 5 |3| 5.5 |3| 5
      )

    assertEquals(actual, expected)
  }
}
