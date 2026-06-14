package com.example.perf

import indigo.*
import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object Runtime extends BasicGameRuntime[Unit]:

  def game: Game[?, ?, ?] =
    PerfGame()

  def settings: Settings =
    Settings.default
      .withFrameRatePolicy(FrameRatePolicy.Unlimited)
      .allowContextMenu

  def eventMapping: PartialIso[GlobalMsg, GlobalEvent] =
    PartialIso.none

  def init(flags: Map[String, String]): Result[Unit] =
    Result(())

  def update(model: Unit): GlobalMsg => Result[Unit] =
    case _ => Result(())
