package indigo.render.pipeline.sceneprocessing

import indigo.core.events.GlobalEvent
import indigo.core.time.GameTime
import indigo.core.utils.QuickCache
import indigo.render.pipeline.assets.AssetMapping
import indigo.render.pipeline.datatypes.DisplayLayer
import indigo.render.pipeline.datatypes.DisplayObject
import indigo.render.pipeline.datatypes.ProcessedSceneData
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
import indigoengine.shared.collections.KVP
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
    // TODO: I've just added this, wasn't here before.. was it missed? How static is static. Assess.
    // staticCloneCache.purgeAllNow()
  }

  def processScene(
      gameTime: GameTime,
      scene: SceneUpdateFragment,
      assetMapping: AssetMapping,
      maxBatchSize: Int,
      inputEvents: => Batch[GlobalEvent],
      sendEvent: GlobalEvent => Unit
  ): ProcessedSceneData = {

    val cloneBlankDisplayObjects: mutable.KVP[DisplayObject] =
      gatherCloneBlankDisplayObjects(scene, gameTime, assetMapping)

    val displayLayers: Batch[(DisplayLayer, Batch[(String, DisplayObject)])] =
      makeDisplayLayers(
        scene,
        gameTime,
        assetMapping,
        maxBatchSize,
        inputEvents,
        sendEvent,
        cloneBlankDisplayObjects
      )

    val sceneBlend: ShaderData =
      scene.blendMaterial.getOrElse(BlendMaterial.Normal).toShaderData

    displayObjectConverter.purgeEachFrame()

    // TODO: Can this be more efficient?
    // Looks like splitting the diplayLayers into two batches might be good instead of traversing twice? (see next TODO)
    val cloneBlankDOs: KVP[DisplayObject] =
      cloneBlankDisplayObjects.toKVP.addAll(displayLayers.flatMap(_._2))

    new ProcessedSceneData(
      displayLayers.map(_._1), // TODO: Remove map
      cloneBlankDOs,
      sceneBlend.shaderId,
      MergeUniformData.mergeShaderToUniformData(sceneBlend),
      scene.camera
    )
  }

  // TODO: Build the KVP more efficiently. Remove flatMap's, foldLefts, and ++'s.
  private def gatherCloneBlankDisplayObjects(
      scene: SceneUpdateFragment,
      gameTime: GameTime,
      assetMapping: AssetMapping
  ): mutable.KVP[DisplayObject] =
    (scene.cloneBlanks ++ scene.layers.flatMap(_.layer.gatherCloneBlanks))
      .foldLeft(mutable.KVP.empty[DisplayObject]) { (acc, blank) =>
        val maybeDO =
          if blank.isStatic then
            QuickCache(blank.id.toString) {
              displayObjectConverter.cloneBlankToDisplayObject(blank, gameTime, assetMapping)
            }
          else displayObjectConverter.cloneBlankToDisplayObject(blank, gameTime, assetMapping)

        maybeDO match
          case None => acc
          case Some(displayObject) =>
            acc.update(blank.id.toString, displayObject)
            acc
      }

  // TODO: Be more efficient, remove allocations from flatMaps and zips and maps etc.
  private def makeDisplayLayers(
      scene: SceneUpdateFragment,
      gameTime: GameTime,
      assetMapping: AssetMapping,
      maxBatchSize: Int,
      inputEvents: => Batch[GlobalEvent],
      sendEvent: GlobalEvent => Unit,
      cloneBlankDisplayObjects: mutable.KVP[DisplayObject]
  ): Batch[(DisplayLayer, Batch[(String, DisplayObject)])] =
    CompactLayers
      .compactLayers(scene.layers)
      .flatMap((maybeLayerKey, contentLayers) =>
        contentLayers
          .map(
            (
              maybeLayerKey,
              _
            )
          )
      )
      .zipWithIndex
      .map { case (l, i) =>
        val blending   = l._2.blending.getOrElse(Blending.Normal)
        val shaderData = blending.blendMaterial.toShaderData

        val conversionResults = displayObjectConverter
          .processSceneNodes(
            l._2.nodes,
            gameTime,
            assetMapping,
            cloneBlankDisplayObjects,
            maxBatchSize,
            inputEvents,
            sendEvent
          )

        val layer = DisplayLayer(
          l._1,
          conversionResults._1,
          BuildLightingData.makeLightsData(scene.lights ++ l._2.lights),
          blending.clearColor.getOrElse(RGBA.Zero),
          l._2.magnification,
          blending.entity,
          blending.layer,
          shaderData.shaderId,
          MergeUniformData.mergeShaderToUniformData(shaderData),
          l._2.camera
        )

        (layer, conversionResults._2)
      }
