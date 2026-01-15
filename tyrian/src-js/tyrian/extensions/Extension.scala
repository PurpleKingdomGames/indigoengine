package tyrian.extensions

import indigoengine.shared.collections.Batch
import tyrian.GlobalMsg
import tyrian.HtmlFragment
import tyrian.Result
import tyrian.Watcher

trait Extension:

  type ExtensionModel

  def id: ExtensionId

  def init: Result[ExtensionModel]

  def update(model: ExtensionModel): GlobalMsg => Result[ExtensionModel]

  def view(model: ExtensionModel): HtmlFragment

  def watchers(model: ExtensionModel): Batch[Watcher]
