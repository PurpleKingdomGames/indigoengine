package indigo.render.pipeline.sceneprocessing

import indigo.core.events.GlobalEvent
import indigo.core.time.GameTime
import indigo.core.utils.QuickCache
import indigo.render.pipeline.assets.AssetMapping
import indigo.render.pipeline.datatypes.DisplayLayer
import indigo.render.pipeline.datatypes.DisplayObject
import indigo.render.pipeline.datatypes.ProcessedSceneData
import indigo.render.pipeline.displayprocessing.DisplayConversionResults
import indigo.render.pipeline.displayprocessing.DisplayObjectConversions
import indigo.render.pipeline.sceneprocessing.utils.*
import indigo.scenegraph.Blending
import indigo.scenegraph.SceneUpdateFragment
import indigo.scenegraph.materials.BlendMaterial
import indigo.scenegraph.registers.AnimationsRegister
import indigo.scenegraph.registers.BoundaryLocator
import indigo.scenegraph.registers.FontRegister
import indigo.shaders.ShaderData
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.mutable
import indigoengine.shared.datatypes.RGBA

final class SceneProcessor(
    boundaryLocator: BoundaryLocator,
    animationsRegister: AnimationsRegister,
    fontRegister: FontRegister
):

  private val displayObjectConverter: DisplayObjectConversions =
    new DisplayObjectConversions(boundaryLocator, animationsRegister, fontRegister)

  private given uniformsCache: QuickCache[Batch[Float]]             = QuickCache.empty
  private given staticCloneCache: QuickCache[Option[DisplayObject]] = QuickCache.empty

  // Called on asset load/reload to account for atlas rebuilding etc.
  def purgeCaches(): Unit = {
    displayObjectConverter.purgeCaches()
    uniformsCache.purgeAllNow()
    // staticCloneCache persists for the duration of the game, and should not be purged.
  }

  def processScene(
      gameTime: GameTime,
      scene: SceneUpdateFragment,
      assetMapping: AssetMapping,
      maxBatchSize: Int,
      inputEvents: => Batch[GlobalEvent],
      sendEvent: GlobalEvent => Unit
  ): ProcessedSceneData = {

    displayObjectConverter.prepareToProcessFrame(
      gameTime,
      assetMapping,
      maxBatchSize,
      inputEvents,
      sendEvent
    )

    val cloneBlankDisplayObjects: mutable.KVP[DisplayObject] =
      gatherCloneBlankDisplayObjects(scene)

    displayObjectConverter.updateConvertorCloneBlanks(cloneBlankDisplayObjects)

    val (displayLayers, layerCloneBlanks) =
      SceneProcessor.makeDisplayLayers(
        scene,
        inputEvents,
        sendEvent,
        displayObjectConverter
      )

    val sceneBlend: ShaderData =
      scene.blendMaterial.getOrElse(BlendMaterial.Normal).toShaderData

    displayObjectConverter.purgeEachFrame()

    layerCloneBlanks.toBatch.foreach { case (k, v) =>
      cloneBlankDisplayObjects.update(k, v)
    }

    new ProcessedSceneData(
      displayLayers.toBatch,
      cloneBlankDisplayObjects.toKVP,
      sceneBlend.shaderId,
      MergeUniformData.mergeShaderToUniformData(sceneBlend),
      scene.camera
    )
  }

  // TODO: Build the KVP more efficiently. Remove flatMap's, foldLefts, and ++'s.
  private def gatherCloneBlankDisplayObjects(
      scene: SceneUpdateFragment
  ): mutable.KVP[DisplayObject] =
    (scene.cloneBlanks ++ scene.layers.flatMap(_.layer.gatherCloneBlanks))
      .foldLeft(mutable.KVP.empty[DisplayObject]) { (acc, blank) =>
        val maybeDO =
          if blank.isStatic then
            QuickCache(blank.id.toString) {
              displayObjectConverter.cloneBlankToDisplayObject(blank)
            }
          else displayObjectConverter.cloneBlankToDisplayObject(blank)

        maybeDO match
          case None => acc
          case Some(displayObject) =>
            acc.update(blank.id.toString, displayObject)
            acc
      }

object SceneProcessor:

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while"))
  private[sceneprocessing] def makeDisplayLayers(
      scene: SceneUpdateFragment,
      inputEvents: => Batch[GlobalEvent],
      sendEvent: GlobalEvent => Unit,
      displayObjectConverter: DisplayObjectConversions
  )(using QuickCache[Batch[Float]]): (mutable.Batch[DisplayLayer], mutable.KVP[DisplayObject]) =
    val compacted      = CompactLayers.compactLayers(scene.layers)
    val layerResults   = mutable.Batch.empty[DisplayLayer]
    val allCloneBlanks = mutable.KVP.empty[DisplayObject]

    var i = 0
    while i < compacted.length do
      val (maybeLayerKey, contentLayers) = compacted(i)

      var j = 0
      while j < contentLayers.length do
        val content    = contentLayers(j)
        val blending   = content.blending.getOrElse(Blending.Normal)
        val shaderData = blending.blendMaterial.toShaderData

        val conversionResults: DisplayConversionResults =
          displayObjectConverter
            .processSceneNodes(
              content.nodes,
              inputEvents,
              sendEvent
            )

        layerResults.append(
          DisplayLayer(
            maybeLayerKey,
            conversionResults.displayEntities,
            BuildLightingData.makeLightsData(scene.lights ++ content.lights),
            blending.clearColor.getOrElse(RGBA.Zero),
            content.magnification,
            blending.entity,
            blending.layer,
            shaderData.shaderId,
            MergeUniformData.mergeShaderToUniformData(shaderData),
            content.camera
          )
        )

        val cbs = conversionResults.clones
        var k   = 0
        while k < cbs.length do
          val kvp = cbs(k)
          allCloneBlanks.update(kvp.id, kvp.displayObject)
          k += 1
        end while

        j += 1
      end while

      i += 1
    end while

    (layerResults, allCloneBlanks)
