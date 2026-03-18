package indigoextras.subsystems

import indigo.core.dice.Dice
import indigo.core.time.GameTime
import indigo.shared.Context
import indigo.shared.subsystems.SubSystemContext
import indigoengine.shared.datatypes.Seconds

object FakeSubSystemFrameContext:

  def context(sides: Int): SubSystemContext[Unit] =
    SubSystemContext.fromContext(
      Context.initial
        .modifyFrame(
          _.withDice(Dice.loaded(sides))
        )
    )

  def context(sides: Int, time: Seconds): SubSystemContext[Unit] =
    SubSystemContext.fromContext(
      Context.initial
        .modifyFrame(
          _.withDice(Dice.loaded(sides))
            .withTime(GameTime.is(time))
        )
    )

  def context(sides: Int, time: Seconds, delta: Seconds): SubSystemContext[Unit] =
    SubSystemContext.fromContext(
      Context.initial
        .modifyFrame(
          _.withDice(Dice.loaded(sides))
            .withTime(GameTime.withDelta(time, delta))
        )
    )
