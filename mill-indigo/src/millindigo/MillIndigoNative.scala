package indigoplugin

import indigoplugin.IndigoOptions
import mill.*
import mill.scalalib.ScalaModule

import scala.annotation.nowarn

trait MillIndigoNative extends ScalaModule:

  /** Location of your Indigo game's assets folder. */
  def indigoAssets: Task[PathRef]

  /** Configuration options for your Indigo game. */
  def indigoOptions(assetsDirectory: os.Path): IndigoOptions

  /** Indigo source code generators */
  def indigoGenerators(assetsDirectory: os.Path): IndigoGenerators

  @nowarn("msg=unused")
  private def _indigoGeneratedSources = Task {
    val assetFolder = indigoAssets()

    indigoGenerators(assetFolder.path)
      .toSourcePaths(indigoOptions(assetFolder.path), assetFolder.path, Task.dest)

    Seq(PathRef(Task.dest))
  }

  @nowarn("msg=unused")
  override def generatedSources =
    Task {
      val custom: Seq[PathRef] =
        _indigoGeneratedSources()
          .map: p =>
            val from: os.Path = p.path
            val to            = Task.dest / from.last

            os.copy(from, to)
            PathRef(to)

      super.generatedSources() ++ custom
    }

