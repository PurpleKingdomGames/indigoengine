package com.example.sandbox

final case class SandboxGameModel(loggedLocales: Boolean)

object SandboxModel:

  def initialModel: SandboxGameModel =
    SandboxGameModel(loggedLocales = false)
