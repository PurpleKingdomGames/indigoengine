package indigo.render.pipeline.displayprocessing.utils

import indigo.core.utils.QuickCache
import indigo.render.pipeline.datatypes.DisplayCloneBatch
import indigo.scenegraph.CloneBatch

object CloneBatchConversion:

  def cloneBatchDataToDisplayEntities(batch: CloneBatch)(using QuickCache[DisplayCloneBatch]): DisplayCloneBatch =
    if batch.staticBatchKey.isDefined then
      QuickCache(batch.staticBatchKey.get.toString) {
        new DisplayCloneBatch(
          id = batch.id,
          cloneData = batch.cloneData
        )
      }
    else
      new DisplayCloneBatch(
        id = batch.id,
        cloneData = batch.cloneData
      )
