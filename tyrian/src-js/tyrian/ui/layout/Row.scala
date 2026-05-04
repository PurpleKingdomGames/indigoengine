package tyrian.ui.layout

import indigoengine.shared.collections.Batch
import tyrian.ui.UIElement
import tyrian.ui.datatypes.LayoutDirection

object Row:

  def apply(children: UIElement[?, ?]*): Layout =
    Row(Batch.fromSeq(children))

  def apply(children: Batch[UIElement[?, ?]]): Layout =
    Layout(LayoutDirection.Row, children)
