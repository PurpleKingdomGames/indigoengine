package tyrian.next.extensions

import indigoengine.shared.collections.Batch
import tyrian.next.GlobalMsg
import tyrian.next.HtmlFragment
import tyrian.next.Result
import tyrian.next.Watcher

trait Extension:

  type ExtensionModel

  def id: ExtensionId

  def init: Result[ExtensionModel]

  def update(model: ExtensionModel): GlobalMsg => Result[ExtensionModel]

  def view(model: ExtensionModel): HtmlFragment

  def watchers(model: ExtensionModel): Batch[Watcher]
