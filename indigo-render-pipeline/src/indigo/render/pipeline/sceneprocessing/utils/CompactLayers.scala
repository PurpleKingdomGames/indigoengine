package indigo.render.pipeline.sceneprocessing.utils

import indigo.core.render.Magnification
import indigo.scenegraph.Layer
import indigo.scenegraph.LayerEntry
import indigoengine.shared.collections.Batch

import scala.annotation.tailrec

object CompactLayers:

  /** Compact layers by squashing layers that have the same properties.
    *
    * Note: Layer Entries are already compacted because they get merged by key in an earler step.
    */
  def compactLayers(layerEntries: Batch[LayerEntry]): Batch[(Batch[Layer.Content], Magnification)] =
    layerEntries.flatMap {
      case LayerEntry(_, _, cfg) if cfg.isHidden =>
        Batch.empty

      case LayerEntry(_, layer: Layer.Content, cfg) =>
        Batch((Batch(layer), cfg.giveMagnification))

      case LayerEntry(_, stack: Layer.Stack, cfg) =>
        val ls = compactContentLayers(stack.toBatch)
        if ls.isEmpty then Batch.empty
        else Batch((ls, cfg.giveMagnification))
    }

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
