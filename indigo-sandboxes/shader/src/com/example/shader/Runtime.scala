package com.example.shader

import indigo.BasicGameRuntime

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object Runtime extends BasicGameRuntime(ShaderGame())
