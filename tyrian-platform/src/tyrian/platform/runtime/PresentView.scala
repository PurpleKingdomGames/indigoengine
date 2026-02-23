package tyrian.platform.runtime

import cats.effect.kernel.Async
import cats.effect.kernel.Clock
import cats.effect.kernel.Ref
import cats.effect.std.Dispatcher
import tyrian.Location

trait PresentView[View[_], ViewState]:

  def draw[F[_], Model, Msg](
      dispatcher: Dispatcher[F],
      viewState: Ref[F, ViewState],
      model: Ref[F, Model],
      view: Model => View[Msg],
      onMsg: Msg => Unit,
      router: Location => Msg
  )(using F: Async[F], clock: Clock[F]): F[Unit]
