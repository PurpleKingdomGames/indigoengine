package tyrian.extensions

import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.mutable.KVP
import tyrian.Action
import tyrian.GlobalMsg
import tyrian.Result
import tyrian.TerminalFragment
import tyrian.Watcher

final class ExtensionRegister {

  private val stateMap: KVP[Object] = KVP.empty

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var registeredExtensions: Batch[RegisteredExtension] = Batch()

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def register(newExtensions: Batch[Extension]): Batch[Action] =
    newExtensions.map(initialiseExtension).sequence match {
      case oe @ Result.Error(e, _) =>
        println("Error during subsystem setup - Halting.")
        println("Crash report:")
        println(oe.reportCrash)
        throw e

      case Result.Next(toBeRegistered, actions) =>
        registeredExtensions = registeredExtensions ++ toBeRegistered
        actions
    }

  private def initialiseExtension(extension: Extension): Result[RegisteredExtension] = {
    val key = extension.id.toString
    val res = RegisteredExtension(key, extension)

    extension.init.map { model =>
      stateMap.update(key, model.asInstanceOf[Object])

      res
    }
  }

  def update(globalMsg: GlobalMsg): Result[Batch[Action]] =
    val results: Batch[Result[Batch[Action]]] =
      registeredExtensions
        .map: rss =>
          val key       = rss.id
          val extension = rss.extension

          val model: extension.ExtensionModel = stateMap.getUnsafe(key).asInstanceOf[extension.ExtensionModel]

          extension.update(model)(globalMsg) match
            case Result.Error(e, _) =>
              Result.raiseError(e)

            case Result.Next(state, actions) =>
              stateMap.update(key, state.asInstanceOf[Object])
              Result(actions)

    results.foldLeft(Result(Batch.empty[Action])) { (acc, next) =>
      acc.flatMap(accActions => next.map(actions => accActions ++ actions))
    }

  def view: TerminalFragment =
    registeredExtensions
      .map { rss =>
        rss.extension.view(
          stateMap.getUnsafe(rss.id).asInstanceOf[rss.extension.ExtensionModel]
        )
      }
      .foldLeft(TerminalFragment.empty)(_ |+| _)

  def watchers: Batch[Watcher] =
    registeredExtensions
      .flatMap: rss =>
        val key       = rss.id
        val extension = rss.extension

        val model: extension.ExtensionModel = stateMap.getUnsafe(key).asInstanceOf[extension.ExtensionModel]

        extension.watchers(model)

  def size: Int =
    registeredExtensions.length

}

final case class RegisteredExtension(id: String, extension: Extension) derives CanEqual
