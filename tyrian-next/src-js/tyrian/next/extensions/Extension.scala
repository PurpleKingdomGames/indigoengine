package tyrian.next.extensions

import indigoengine.shared.collections.Batch
import tyrian.next.GlobalMsg
import tyrian.next.HtmlFragment
import tyrian.next.Outcome
import tyrian.next.Watcher

trait Extension:

  type ExtensionModel

  def id: ExtensionId

  def init: Outcome[ExtensionModel]

  def update(model: ExtensionModel): GlobalMsg => Outcome[ExtensionModel]

  def view(model: ExtensionModel): HtmlFragment

  def watchers(model: ExtensionModel): Batch[Watcher]
