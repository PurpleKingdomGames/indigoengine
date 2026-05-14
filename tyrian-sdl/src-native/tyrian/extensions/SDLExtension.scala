package tyrian.extensions

import tyrian.Batch
import tyrian.GlobalMsg
import tyrian.Result
import tyrian.SDLContext
import tyrian.Seconds
import tyrian.TerminalFragment
import tyrian.Watcher

trait SDLExtension:

  type ExtensionModel

  def id: ExtensionId

  def init: Result[ExtensionModel]

  def update(model: ExtensionModel): GlobalMsg => Result[ExtensionModel]

  def view(model: ExtensionModel): TerminalFragment

  def watchers(model: ExtensionModel): Batch[Watcher]

  def onFrame(ctx: SDLContext, runningTime: Seconds, model: ExtensionModel): Unit
