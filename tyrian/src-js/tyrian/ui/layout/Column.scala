package tyrian.ui.layout

import indigoengine.shared.collections.Batch
import tyrian.ui.UIElement
import tyrian.ui.datatypes.LayoutDirection

object Column:

  def apply(children: UIElement[?, ?]*): Layout =
    Column(Batch.fromSeq(children))

  def apply(children: Batch[UIElement[?, ?]]): Layout =
    Layout(LayoutDirection.Column, children)
