package tyrian.classic

object aliases:

  // -- platform aliases

  type Cmd[F[_], Msg] = tyrian.platform.Cmd[F, Msg]
  val Cmd: tyrian.platform.Cmd.type = tyrian.platform.Cmd

  type Sub[F[_], Msg] = tyrian.platform.Sub[F, Msg]
  val Sub: tyrian.platform.Sub.type = tyrian.platform.Sub

export aliases.*
