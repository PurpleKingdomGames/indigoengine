package indigoplugin.datatypes

import io.circe.*

// format: off
final case class Aseprite(frames: List[AsepriteFrame], meta: AsepriteMeta) {
  def render: String =
    s"""Aseprite(${frames.map(_.render).mkString("List(", ",", ")")}, ${meta.render})"""
}
object Aseprite{

  private def decodeFrameAt(c: ACursor, filenameDefault: String): Decoder.Result[AsepriteFrame] =
    for {
      filename         <- c.downField("filename").as[Option[String]].map(_.getOrElse(filenameDefault))
      frame            <- c.downField("frame").as[AsepriteRectangle]
      rotated          <- c.downField("rotated").as[Boolean]
      trimmed          <- c.downField("trimmed").as[Boolean]
      spriteSourceSize <- c.downField("spriteSourceSize").as[AsepriteRectangle]
      sourceSize       <- c.downField("sourceSize").as[AsepriteSize]
      duration         <- c.downField("duration").as[Int]
    } yield AsepriteFrame(filename, frame, rotated, trimmed, spriteSourceSize, sourceSize, duration)

  // Exposed for AsepriteFrame.decodeAsepriteFrame as a fallback when no key is available.
  private[datatypes] def decodeFrameWithoutKey(c: ACursor): Decoder.Result[AsepriteFrame] =
    decodeFrameAt(c, "")

  implicit val decodeAseprite: Decoder[Aseprite] =
    new Decoder[Aseprite] {
      final def apply(c: HCursor): Decoder.Result[Aseprite] = {
        val framesCursor = c.downField("frames")
        val arrayResult: Decoder.Result[List[AsepriteFrame]] =
          framesCursor.as[List[AsepriteFrame]]
        val frames: Decoder.Result[List[AsepriteFrame]] =
          arrayResult match {
            case Right(fs) => Right(fs)
            case Left(_) =>
              framesCursor.focus match {
                case Some(json) if json.isObject =>
                  val pairs = json.asObject.fold(List.empty[(String, Json)])(_.toList)
                  pairs.foldLeft[Decoder.Result[List[AsepriteFrame]]](Right(Nil)) { (acc, kv) =>
                    val (key, value) = kv
                    for {
                      soFar <- acc
                      next  <- decodeFrameAt(value.hcursor, key)
                    } yield soFar :+ next
                  }
                case _ =>
                  arrayResult
              }
          }
        for {
          fs   <- frames
          meta <- c.downField("meta").as[AsepriteMeta]
        } yield Aseprite(fs, meta)
      }
    }
}

final case class AsepriteFrame(
    filename: String,
    frame: AsepriteRectangle,
    rotated: Boolean,
    trimmed: Boolean,
    spriteSourceSize: AsepriteRectangle,
    sourceSize: AsepriteSize,
    duration: Int
) {
  def render: String =
    s"""AsepriteFrame("${RenderHelpers.escape(filename)}", ${frame.render}, $rotated, $trimmed, ${spriteSourceSize.render}, ${sourceSize.render}, $duration)"""
}
object AsepriteFrame {

  implicit val decodeAsepriteFrame: Decoder[AsepriteFrame] =
    new Decoder[AsepriteFrame] {
      final def apply(c: HCursor): Decoder.Result[AsepriteFrame] =
        Aseprite.decodeFrameWithoutKey(c)
    }

}

final case class AsepriteRectangle(x: Int, y: Int, w: Int, h: Int) {
  def render: String =
    s"""AsepriteRectangle($x, $y, $w, $h)"""
}
object AsepriteRectangle {

  implicit val decodeAsepriteRectangle: Decoder[AsepriteRectangle] =
    new Decoder[AsepriteRectangle] {
      final def apply(c: HCursor): Decoder.Result[AsepriteRectangle] =
        for {
          x <- c.downField("x").as[Int]
          y <- c.downField("y").as[Int]
          w <- c.downField("w").as[Int]
          h <- c.downField("h").as[Int]
        } yield AsepriteRectangle(x, y, w, h)
    }

}

final case class AsepritePoint(x: Int, y: Int) {
  def render: String =
    s"""AsepritePoint($x, $y)"""
}
object AsepritePoint {

  implicit val decodeAsepritePoint: Decoder[AsepritePoint] =
    new Decoder[AsepritePoint] {
      final def apply(c: HCursor): Decoder.Result[AsepritePoint] =
        for {
          x <- c.downField("x").as[Int]
          y <- c.downField("y").as[Int]
        } yield AsepritePoint(x, y)
    }

}

final case class AsepriteMeta(
    app: String,
    version: String,
    image: Option[String],
    format: String,
    size: AsepriteSize,
    scale: String,
    frameTags: List[AsepriteFrameTag],
    slices: Option[List[AsepriteSlice]]
) {
  def render: String =
    s"""AsepriteMeta("${RenderHelpers.escape(app)}", "${RenderHelpers.escape(version)}", ${RenderHelpers.renderOptString(image)}, "${RenderHelpers.escape(format)}", ${size.render}, "${RenderHelpers.escape(scale)}", ${frameTags.map(_.render).mkString("List(", ",", ")")}, ${RenderHelpers.renderOptList(slices.map(_.map(_.render)))})"""
}
object AsepriteMeta {

  implicit val decodeAsepriteMeta: Decoder[AsepriteMeta] =
    new Decoder[AsepriteMeta] {
      final def apply(c: HCursor): Decoder.Result[AsepriteMeta] =
        for {
          app       <- c.downField("app").as[String]
          version   <- c.downField("version").as[String]
          image     <- c.downField("image").as[Option[String]]
          format    <- c.downField("format").as[String]
          size      <- c.downField("size").as[AsepriteSize]
          scale     <- c.downField("scale").as[String]
          frameTags <- c.downField("frameTags").as[Option[List[AsepriteFrameTag]]].map(_.getOrElse(Nil))
          slices    <- c.downField("slices").as[Option[List[AsepriteSlice]]]
        } yield AsepriteMeta(app, version, image, format, size, scale, frameTags, slices)
    }

}

final case class AsepriteSize(w: Int, h: Int) {
  def render: String =
    s"""AsepriteSize($w, $h)"""
}
object AsepriteSize {

  implicit val decodeAsepriteSize: Decoder[AsepriteSize] =
    new Decoder[AsepriteSize] {
      final def apply(c: HCursor): Decoder.Result[AsepriteSize] =
        for {
          w <- c.downField("w").as[Int]
          h <- c.downField("h").as[Int]
        } yield AsepriteSize(w, h)
    }

}

final case class AsepriteFrameTag(
    name: String,
    from: Int,
    to: Int,
    direction: String,
    color: Option[String],
    data: Option[String],
    repeat: Option[String]
) {
  def render: String =
    s"""AsepriteFrameTag("${RenderHelpers.escape(name)}", $from, $to, "${RenderHelpers.escape(direction)}", ${RenderHelpers.renderOptString(color)}, ${RenderHelpers.renderOptString(data)}, ${RenderHelpers.renderOptString(repeat)})"""
}
object AsepriteFrameTag {

  implicit val decodeAsepriteFrameTag: Decoder[AsepriteFrameTag] =
    new Decoder[AsepriteFrameTag] {
      final def apply(c: HCursor): Decoder.Result[AsepriteFrameTag] =
        for {
          name      <- c.downField("name").as[String]
          from      <- c.downField("from").as[Int]
          to        <- c.downField("to").as[Int]
          direction <- c.downField("direction").as[Option[String]].map(_.getOrElse("forward"))
          color     <- c.downField("color").as[Option[String]]
          data      <- c.downField("data").as[Option[String]]
          repeat    <- c.downField("repeat").as[Option[String]]
        } yield AsepriteFrameTag(name, from, to, direction, color, data, repeat)
    }

}

final case class AsepriteSlice(
    name: String,
    color: Option[String],
    data: Option[String],
    keys: List[AsepriteSliceKey]
) {
  def render: String =
    s"""AsepriteSlice("${RenderHelpers.escape(name)}", ${RenderHelpers.renderOptString(color)}, ${RenderHelpers.renderOptString(data)}, ${keys.map(_.render).mkString("List(", ",", ")")})"""
}
object AsepriteSlice {

  implicit val decodeAsepriteSlice: Decoder[AsepriteSlice] =
    new Decoder[AsepriteSlice] {
      final def apply(c: HCursor): Decoder.Result[AsepriteSlice] =
        for {
          name  <- c.downField("name").as[String]
          color <- c.downField("color").as[Option[String]]
          data  <- c.downField("data").as[Option[String]]
          keys  <- c.downField("keys").as[Option[List[AsepriteSliceKey]]].map(_.getOrElse(Nil))
        } yield AsepriteSlice(name, color, data, keys)
    }

}

final case class AsepriteSliceKey(
    frame: Int,
    bounds: AsepriteRectangle,
    center: Option[AsepriteRectangle],
    pivot: Option[AsepritePoint]
) {
  def render: String =
    s"""AsepriteSliceKey($frame, ${bounds.render}, ${RenderHelpers.renderOpt(center.map(_.render))}, ${RenderHelpers.renderOpt(pivot.map(_.render))})"""
}
object AsepriteSliceKey {

  implicit val decodeAsepriteSliceKey: Decoder[AsepriteSliceKey] =
    new Decoder[AsepriteSliceKey] {
      final def apply(c: HCursor): Decoder.Result[AsepriteSliceKey] =
        for {
          frame  <- c.downField("frame").as[Int]
          bounds <- c.downField("bounds").as[AsepriteRectangle]
          center <- c.downField("center").as[Option[AsepriteRectangle]]
          pivot  <- c.downField("pivot").as[Option[AsepritePoint]]
        } yield AsepriteSliceKey(frame, bounds, center, pivot)
    }

}

private[datatypes] object RenderHelpers {

  def escape(s: String): String =
    s.replace("\\", "\\\\").replace("\"", "\\\"")

  def renderOptString(opt: Option[String]): String =
    opt match {
      case Some(s) => s"""Some("${escape(s)}")"""
      case None    => "None"
    }

  def renderOpt(opt: Option[String]): String =
    opt match {
      case Some(s) => s"Some($s)"
      case None    => "None"
    }

  def renderOptList(opt: Option[List[String]]): String =
    opt match {
      case Some(xs) => xs.mkString("Some(List(", ",", "))")
      case None     => "None"
    }

}
// format: on
