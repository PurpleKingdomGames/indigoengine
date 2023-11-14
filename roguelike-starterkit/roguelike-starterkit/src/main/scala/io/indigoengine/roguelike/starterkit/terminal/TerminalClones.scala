package io.indigoengine.roguelike.starterkit.terminal

import indigo.*
import io.indigoengine.roguelike.starterkit.Tile

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
    SceneUpdateFragment(clones).addCloneBlanks(blanks)

object TerminalClones:
  def empty: TerminalClones =
    TerminalClones(Batch.empty, Batch.empty)
