package tyrian.classic

object aliases:

  val Routing: tyrian.Routing.type = tyrian.Routing

  type Location = tyrian.Location
  val Location: tyrian.Location.type = tyrian.Location

  type Html[+M] = tyrian.Html[M]
  val Html: tyrian.Html.type = tyrian.Html

  val CSS: tyrian.CSS.type = tyrian.CSS

  val SVG: tyrian.SVG.type = tyrian.SVG

  val Empty: tyrian.Empty.type = tyrian.Empty

  type Elem[+M] = tyrian.Elem[M]
  val Elem: tyrian.Elem.type = tyrian.Elem

  type Text = tyrian.Text
  val Text: tyrian.Text.type = tyrian.Text

  type CustomElem[+M] = tyrian.CustomElem[M]

  type Tag[+M] = tyrian.Tag[M]
  val Tag: tyrian.Tag.type = tyrian.Tag

  type RawTag[+M] = tyrian.RawTag[M]
  val RawTag: tyrian.RawTag.type = tyrian.RawTag

  type CustomHtml[+M] = tyrian.CustomHtml[M]

  type HtmlEntity = tyrian.HtmlEntity
  val HtmlEntity: tyrian.HtmlEntity.type = tyrian.HtmlEntity

  // -- platform aliases

  type Cmd[F[_], Msg] = tyrian.platform.Cmd[F, Msg]
  val Cmd: tyrian.platform.Cmd.type = tyrian.platform.Cmd

  type Sub[F[_], Msg] = tyrian.platform.Sub[F, Msg]
  val Sub: tyrian.platform.Sub.type = tyrian.platform.Sub

export aliases.*
