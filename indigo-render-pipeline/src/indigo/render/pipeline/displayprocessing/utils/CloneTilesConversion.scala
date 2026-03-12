package indigo.render.pipeline.displayprocessing.utils

import indigo.core.utils.QuickCache
import indigo.render.pipeline.datatypes.DisplayCloneTiles
import indigo.scenegraph.CloneTiles

object CloneTilesConversion:

  def cloneTilesDataToDisplayEntities(batch: CloneTiles)(using QuickCache[DisplayCloneTiles]): DisplayCloneTiles =
    if batch.staticBatchKey.isDefined then
      QuickCache(batch.staticBatchKey.get.toString) {
        new DisplayCloneTiles(
          id = batch.id,
          cloneData = batch.cloneData
        )
      }
    else
      new DisplayCloneTiles(
        id = batch.id,
        cloneData = batch.cloneData
      )
