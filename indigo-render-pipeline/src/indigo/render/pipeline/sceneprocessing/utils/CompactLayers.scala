package indigo.render.pipeline.sceneprocessing.utils

import indigo.scenegraph.Layer
import indigo.scenegraph.LayerEntry
import indigo.scenegraph.LayerKey
import indigoengine.shared.collections.Batch

import scala.annotation.tailrec

object CompactLayers:

  /** Compact layers by squashing layers that have the same properties.
    */
  def compactLayers(layerEntries: Batch[LayerEntry]): Batch[(Option[LayerKey], Batch[Layer.Content])] =
    layerEntries.map {
      case LayerEntry.NoKey(layer: Layer.Content) =>
        val ls = if layer.visible.getOrElse(true) then Batch(layer) else Batch.empty

        (None, ls)

      case LayerEntry.NoKey(stack: Layer.Stack) =>
        val ls = compactContentLayers(stack.toBatch)

        (None, ls)

      case LayerEntry.Keyed(key, layer: Layer.Content) =>
        val ls = if layer.visible.getOrElse(true) then Batch(layer) else Batch.empty

        (Option(key), ls)

      case LayerEntry.Keyed(key, stack: Layer.Stack) =>
        val ls = compactContentLayers(stack.toBatch)

        (Option(key), ls)
    }

  def compactContentLayers(contentLayers: Batch[Layer.Content]): Batch[Layer.Content] =
    @tailrec
    def rec(remaining: Batch[Layer.Content], current: Layer.Content, acc: Batch[Layer.Content]): Batch[Layer.Content] =
      if remaining.length == 0 then acc :+ current
      else
        val head = remaining.head
        val tail = remaining.tail

        if head.visible.exists(_ == false) then rec(tail, current, acc)
        else if canCompactLayers(current, head) then
          rec(
            tail,
            current.copy(nodes = current.nodes ++ head.nodes, cloneBlanks = current.cloneBlanks ++ head.cloneBlanks),
            acc
          )
        else rec(tail, head, acc :+ current)

    val contentLayersFirstIsVisible =
      contentLayers.dropWhile(l => l.visible.exists(_ == false))

    if contentLayersFirstIsVisible.length < 2 then contentLayersFirstIsVisible
    else
      val head = contentLayersFirstIsVisible.head
      val tail = contentLayersFirstIsVisible.tail

      rec(tail, head, Batch.empty)

  /** The rule is that if the two layers have all the same properties, ignoring the scene nodes and clone blanks, then
    * we can compact them.
    */
  def canCompactLayers(a: Layer.Content, b: Layer.Content): Boolean =
    a.lights == b.lights &&
      a.magnification == b.magnification &&
      a.visible == b.visible &&
      a.blending == b.blending &&
      a.camera == b.camera
