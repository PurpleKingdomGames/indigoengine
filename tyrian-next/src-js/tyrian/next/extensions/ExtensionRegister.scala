package tyrian.next.extensions

import indigoengine.shared.collections.Batch
import tyrian.next.Action
import tyrian.next.GlobalMsg
import tyrian.next.HtmlFragment
import tyrian.next.Result
import tyrian.next.Watcher

import scalajs.js

final class ExtensionRegister {

  private val stateMap: js.Dictionary[Object] = js.Dictionary.empty

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var registeredExtensions: js.Array[RegisteredExtension] = js.Array()

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def register(newExtensions: Batch[Extension]): Batch[Action] =
    newExtensions.map(initialiseExtension).sequence match {
      case oe @ Result.Error(e, _) =>
        println("Error during subsystem setup - Halting.")
        println("Crash report:")
        println(oe.reportCrash)
        throw e

      case Result.Next(toBeRegistered, actions) =>
        registeredExtensions = registeredExtensions ++ toBeRegistered.toJSArray
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
      Batch.fromJSArray(
        registeredExtensions
          .map: rss =>
            val key       = rss.id
            val extension = rss.extension

            val model: extension.ExtensionModel = stateMap(key).asInstanceOf[extension.ExtensionModel]

            extension.update(model)(globalMsg) match
              case Result.Error(e, _) =>
                Result.raiseError(e)

              case Result.Next(state, actions) =>
                stateMap.update(key, state.asInstanceOf[Object])
                Result(actions)
      )

    results.foldLeft(Result(Batch.empty[Action])) { (acc, next) =>
      acc.flatMap(accActions => next.map(actions => accActions ++ actions))
    }

  def view: HtmlFragment =
    registeredExtensions
      .map { rss =>
        rss.extension.view(
          stateMap(rss.id).asInstanceOf[rss.extension.ExtensionModel]
        )
      }
      .foldLeft(HtmlFragment.empty)(_ |+| _)

  def watchers: Batch[Watcher] =
    Batch
      .fromJSArray(registeredExtensions)
      .flatMap: rss =>
        val key       = rss.id
        val extension = rss.extension

        val model: extension.ExtensionModel = stateMap(key).asInstanceOf[extension.ExtensionModel]

        extension.watchers(model)

  def size: Int =
    registeredExtensions.length

}

final case class RegisteredExtension(id: String, extension: Extension) derives CanEqual
