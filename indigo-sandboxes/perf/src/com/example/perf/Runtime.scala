package com.example.perf

import indigo.BasicGameRuntime

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object Runtime extends BasicGameRuntime(PerfGame())
