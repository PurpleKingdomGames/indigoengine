package io.indigoengine.roguelike.starterkit.terminal

import indigo.*
import io.indigoengine.roguelike.starterkit.Tile

import scala.annotation.tailrec

final case class TerminalClones(blanks: Batch[CloneBlank], clones: Batch[CloneTiles]):
  def |+|(other: TerminalClones): TerminalClones =
    combine(other)
  def combine(other: TerminalClones): TerminalClones =
    TerminalClones(blanks ++ other.blanks, clones ++ other.clones)

object TerminalClones:
  def empty: TerminalClones =
    TerminalClones(Batch.empty, Batch.empty)
