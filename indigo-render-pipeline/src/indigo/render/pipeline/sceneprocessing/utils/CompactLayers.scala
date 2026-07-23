package indigo.render.pipeline.sceneprocessing.utils

import indigo.core.render.Magnification
import indigo.scenegraph.Layer
import indigo.scenegraph.LayerEntry
import indigoengine.shared.collections.Batch

import scala.annotation.tailrec

object CompactLayers:

  /** Compact layers by squashing layers that have the same properties.
    *
    * Note: Layer Entries are already compacted because they get merged by key in an earlier step.
    */
  def compactLayers(layerEntries: Batch[LayerEntry]): Batch[(Batch[Layer.Content], Magnification)] =
    // Step 1: Unwrap the stacks, no compacting
    val step1 =
      layerEntries.flatMap {
        case LayerEntry(_, _, cfg) if cfg.isHidden =>
          Batch.empty

        case LayerEntry(_, layer: Layer.Content, cfg) =>
          Batch((Batch(layer), cfg.giveMagnification))

        case LayerEntry(_, stack: Layer.Stack, cfg) =>
          val ls = stack.toBatch
          if ls.isEmpty then Batch.empty
          else Batch((ls, cfg.giveMagnification))
      }

    // Step 2: Squash the outer batch, where the magnification is the same.
    val step2 =
      compactByMagnification(step1)

    // Step 3: Compact Content layers within each sub-group.
    val step3 =
      step2.map { case (layers, mag) => (compactContentLayers(layers), mag) }

    step3

  def compactByMagnification(
      entries: Batch[(Batch[Layer.Content], Magnification)]
  ): Batch[(Batch[Layer.Content], Magnification)] =
    @tailrec
    def rec(
        remaining: Batch[(Batch[Layer.Content], Magnification)],
        currentLayers: Batch[Layer.Content],
        currentMagnification: Magnification,
        acc: Batch[(Batch[Layer.Content], Magnification)]
    ): Batch[(Batch[Layer.Content], Magnification)] =
      if remaining.length == 0 then acc :+ (currentLayers, currentMagnification)
      else
        val head = remaining.head
        val tail = remaining.tail

        if head._2 == currentMagnification then rec(tail, currentLayers ++ head._1, currentMagnification, acc)
        else rec(tail, head._1, head._2, acc :+ (currentLayers, currentMagnification))

    if entries.length < 2 then entries
    else
      val head = entries.head
      val tail = entries.tail

      rec(tail, head._1, head._2, Batch.empty)

  def compactContentLayers(contentLayers: Batch[Layer.Content]): Batch[Layer.Content] =
    @tailrec
    def rec(remaining: Batch[Layer.Content], current: Layer.Content, acc: Batch[Layer.Content]): Batch[Layer.Content] =
      if remaining.length == 0 then acc :+ current
      else
        val head = remaining.head
        val tail = remaining.tail

        if canCompactLayers(current, head) then
          rec(
            tail,
            current.copy(nodes = current.nodes ++ head.nodes, cloneBlanks = current.cloneBlanks ++ head.cloneBlanks),
            acc
          )
        else rec(tail, head, acc :+ current)

    if contentLayers.length < 2 then contentLayers
    else
      val head = contentLayers.head
      val tail = contentLayers.tail

      rec(tail, head, Batch.empty)

  /** The rule is that if the two layers have all the same properties, ignoring the scene nodes and clone blanks, then
    * we can compact them.
    */
  def canCompactLayers(a: Layer.Content, b: Layer.Content): Boolean =
    a.lights == b.lights && a.blending == b.blending && a.camera == b.camera
