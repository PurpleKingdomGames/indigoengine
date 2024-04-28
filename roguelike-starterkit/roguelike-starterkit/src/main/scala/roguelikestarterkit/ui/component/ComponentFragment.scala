package roguelikestarterkit.ui.component

import indigo.shared.collections.Batch
import indigo.shared.scenegraph.CloneBlank
import indigo.shared.scenegraph.Layer
import indigo.shared.scenegraph.SceneNode
import indigo.shared.scenegraph.SceneUpdateFragment

/** ComponentFragments represent the nodes and clone instances used for rendering a component. They
  * are like a cut-down version of a `SceneUpdateFragment`.
  */
final case class ComponentFragment(
    nodes: Batch[SceneNode],
    cloneBlanks: Batch[CloneBlank]
):
  import Batch.*

  def |+|(other: ComponentFragment): ComponentFragment =
    ComponentFragment.append(this, other)

  def withNodes(newNodes: Batch[SceneNode]): ComponentFragment =
    this.copy(nodes = newNodes)
  def withNodes(newNodes: SceneNode*): ComponentFragment =
    withNodes(newNodes.toBatch)
  def addNodes(moreNodes: Batch[SceneNode]): ComponentFragment =
    withNodes(nodes ++ moreNodes)
  def addNodes(moreNodes: SceneNode*): ComponentFragment =
    addNodes(moreNodes.toBatch)
  def ++(moreNodes: Batch[SceneNode]): ComponentFragment =
    addNodes(moreNodes)

  def addCloneBlanks(blanks: CloneBlank*): ComponentFragment =
    addCloneBlanks(blanks.toBatch)

  def addCloneBlanks(blanks: Batch[CloneBlank]): ComponentFragment =
    this.copy(cloneBlanks = cloneBlanks ++ blanks)

  def toSceneUpdateFragment: SceneUpdateFragment =
    SceneUpdateFragment(
      Batch(Layer(nodes)),
      Batch.empty,
      None,
      None,
      cloneBlanks,
      None
    )

object ComponentFragment:
  import Batch.*

  def apply(nodes: SceneNode*): ComponentFragment =
    ComponentFragment(nodes.toBatch)

  def apply(nodes: Batch[SceneNode]): ComponentFragment =
    ComponentFragment(nodes, Batch.empty)

  def apply(maybeNode: Option[SceneNode]): ComponentFragment =
    ComponentFragment(Batch.fromOption(maybeNode), Batch.empty)

  val empty: ComponentFragment =
    ComponentFragment(Batch.empty, Batch.empty)

  def append(a: ComponentFragment, b: ComponentFragment): ComponentFragment =
    ComponentFragment(
      a.nodes ++ b.nodes,
      a.cloneBlanks ++ b.cloneBlanks
    )
