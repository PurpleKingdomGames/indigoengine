package roguelikestarterkit.terminal

import indigo.*
import roguelikestarterkit.Tile

import scala.annotation.tailrec

/** Represents the output of converting a Terminal into clones ready for rendering. You must add the
  * clones to you scene and register the clone blanks.
  */
final case class TerminalClones(blanks: Batch[CloneBlank], clones: Batch[CloneTiles]):
  def |+|(other: TerminalClones): TerminalClones =
    combine(other)
  def combine(other: TerminalClones): TerminalClones =
    TerminalClones(blanks ++ other.blanks, clones ++ other.clones)

  def toSceneUpdateFragment: SceneUpdateFragment =
    SceneUpdateFragment(
      Batch(LayerEntry(Layer.Content(clones))),
      Batch.empty,
      None,
      None,
      blanks,
      None
    )

  def toLayer: Layer.Content =
    Layer.Content(
      clones,
      Batch.empty,
      None,
      None,
      None,
      None,
      blanks,
      None
    )

object TerminalClones:
  def empty: TerminalClones =
    TerminalClones(Batch.empty, Batch.empty)
