package tyrian.extensions

import indigoengine.shared.collections.Batch
import tyrian.GlobalMsg
import tyrian.Result
import tyrian.TerminalFragment
import tyrian.Watcher

// TODO: Should this be an enum? Standard | Graphics[ContextType] (replacing SDLExtension)? Then it could drive WebGL too?
trait Extension:

  type ExtensionModel

  def id: ExtensionId

  def init: Result[ExtensionModel]

  def update(model: ExtensionModel): GlobalMsg => Result[ExtensionModel]

  def view(model: ExtensionModel): TerminalFragment

  def watchers(model: ExtensionModel): Batch[Watcher]
