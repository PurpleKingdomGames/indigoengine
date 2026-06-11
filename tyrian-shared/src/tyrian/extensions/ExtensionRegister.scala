package tyrian.extensions

import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.mutable.KVP
import indigoengine.shared.datatypes.Seconds
import indigoengine.shared.typeclass.Monoid
import tyrian.Action
import tyrian.GlobalMsg
import tyrian.Result
import tyrian.Watcher

import scala.util.control.NonFatal

final class ExtensionRegister[GraphicsContext, View](using m: Monoid[View]):

  private val stateMap: KVP[Object] = KVP.empty

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var registeredExtensions: Batch[RegisteredExtension[GraphicsContext, View]] = Batch()

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def register(newExtensions: Batch[Extension[GraphicsContext, View]]): Batch[Action] =
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

  def hasGraphicalExtensions: Boolean =
    registeredExtensions.exists(_.isGraphical)

  private def initialiseExtension(
      extension: Extension[GraphicsContext, View]
  ): Result[RegisteredExtension[GraphicsContext, View]] = {
    val key = extension.id.toString
    val isGraphical: Boolean =
      extension match
        case _: Extension.Graphical[_, _] => true
        case _                            => false
    val res = RegisteredExtension(key, extension, isGraphical)

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

  def view: View =
    m.combineAll(
      registeredExtensions
        .map { rss =>
          rss.extension.view(
            stateMap.getUnsafe(rss.id).asInstanceOf[rss.extension.ExtensionModel]
          )
        }
    )

  def watchers: Batch[Watcher] =
    registeredExtensions
      .flatMap: rss =>
        val key       = rss.id
        val extension = rss.extension

        val model: extension.ExtensionModel = stateMap.getUnsafe(key).asInstanceOf[extension.ExtensionModel]

        extension.watchers(model)

  def draw(ctx: Option[GraphicsContext], runningTime: Seconds): Unit =
    registeredExtensions
      .foreach: rss =>
        val key       = rss.id
        val extension = rss.extension

        extension match
          case ext: Extension.Standard[_] =>
            ()

          case ext: Extension.Graphical[_, _] =>
            val model: ext.ExtensionModel = stateMap.getUnsafe(key).asInstanceOf[ext.ExtensionModel]

            ctx
              .orElse(ext.provideContext(model))
              .foreach: ctx =>
                val updated = ext.draw(ctx, runningTime, model)
                stateMap.update(key, updated.asInstanceOf[Object])

  def size: Int =
    registeredExtensions.length

  def teardown: Unit =
    registeredExtensions
      .foreach: rss =>
        val key       = rss.id
        val extension = rss.extension

        val model: extension.ExtensionModel =
          stateMap.getUnsafe(key).asInstanceOf[extension.ExtensionModel]

        try extension.teardown(model)
        catch
          case NonFatal(e) =>
            println(s"Extension teardown failed [${key}]: ${e.getMessage}\n${e.getStackTrace().take(3).mkString("\n")}")
