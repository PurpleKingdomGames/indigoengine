package indigo.scenegraph

import indigo.core.datatypes.*
import indigo.core.events.GlobalEvent
import indigo.shaders.ToUniformBlock
import indigo.shaders.UniformBlock
import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.Radians

/** Represents many identical clones of the same clone blank, differentiated only by their shader data. Intended for use
  * with custom entities in particular.
  */
final case class Mutants(
    id: CloneId,
    uniformBlocks: Array[Batch[UniformBlock]]
) extends DependentNode[Mutants] derives CanEqual:

  lazy val scale: Vector2    = Vector2.one
  lazy val rotation: Radians = Radians.zero
  lazy val ref: Point        = Point.zero
  lazy val position: Point   = Point.zero
  lazy val flip: Flip        = Flip.default

  def withCloneId(newCloneId: CloneId): Mutants =
    this.copy(id = newCloneId)

  def addBlocks(additionalBlocks: Array[Batch[UniformBlock]]): Mutants =
    this.copy(uniformBlocks = uniformBlocks ++ additionalBlocks)

  val eventHandlerEnabled: Boolean                                  = false
  def eventHandler: ((Mutants, GlobalEvent)) => Option[GlobalEvent] = Function.const(None)

object Mutants:

  def apply[A](id: CloneId, uniformBlocks: Array[Batch[A]])(using toUBO: ToUniformBlock[A]): Mutants =
    Mutants(
      id,
      uniformBlocks.map(_.map(toUBO.toUniformBlock))
    )

  def apply(id: CloneId, uniformBlocks: Batch[UniformBlock]): Mutants =
    Mutants(
      id,
      Array(uniformBlocks)
    )

  def apply[A](id: CloneId, uniformBlocks: Batch[A])(using toUBO: ToUniformBlock[A]): Mutants =
    Mutants(
      id,
      Array(uniformBlocks.map(toUBO.toUniformBlock))
    )
