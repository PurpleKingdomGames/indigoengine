package indigo.json.core

import indigo.shared.formats.Aseprite
import indigo.shared.formats.AsepriteFrame
import indigo.shared.formats.AsepriteFrameTag
import indigo.shared.formats.AsepriteMeta
import indigo.shared.formats.AsepritePoint
import indigo.shared.formats.AsepriteRectangle
import indigo.shared.formats.AsepriteSize
import indigo.shared.formats.AsepriteSlice
import indigo.shared.formats.AsepriteSliceKey
import indigo.shared.formats.TileSet
import indigo.shared.formats.TiledLayer
import indigo.shared.formats.TiledMap
import indigo.shared.formats.TiledTerrain
import indigo.shared.formats.TiledTerrainCorner
import io.circe.ACursor
import io.circe.Decoder
import io.circe.HCursor
import io.circe.Json

object CirceJsonEncodersAndDecoders {

  private def decodeAsepriteFrameAt(c: ACursor, filenameDefault: String): Decoder.Result[AsepriteFrame] =
    for {
      filename         <- c.downField("filename").as[Option[String]].map(_.getOrElse(filenameDefault))
      frame            <- c.downField("frame").as[AsepriteRectangle]
      rotated          <- c.downField("rotated").as[Boolean]
      trimmed          <- c.downField("trimmed").as[Boolean]
      spriteSourceSize <- c.downField("spriteSourceSize").as[AsepriteRectangle]
      sourceSize       <- c.downField("sourceSize").as[AsepriteSize]
      duration         <- c.downField("duration").as[Int]
    } yield AsepriteFrame(filename, frame, rotated, trimmed, spriteSourceSize, sourceSize, duration)

  implicit val decodeAsepriteFrame: Decoder[AsepriteFrame] =
    new Decoder[AsepriteFrame] {
      final def apply(c: HCursor): Decoder.Result[AsepriteFrame] =
        decodeAsepriteFrameAt(c, "")
    }

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

  implicit val decodeAsepritePoint: Decoder[AsepritePoint] =
    new Decoder[AsepritePoint] {
      final def apply(c: HCursor): Decoder.Result[AsepritePoint] =
        for {
          x <- c.downField("x").as[Int]
          y <- c.downField("y").as[Int]
        } yield AsepritePoint(x, y)
    }

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

  implicit val decodeAsepriteSize: Decoder[AsepriteSize] =
    new Decoder[AsepriteSize] {
      final def apply(c: HCursor): Decoder.Result[AsepriteSize] =
        for {
          w <- c.downField("w").as[Int]
          h <- c.downField("h").as[Int]
        } yield AsepriteSize(w, h)
    }

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

  implicit val decodeAseprite: Decoder[Aseprite] =
    new Decoder[Aseprite] {
      final def apply(c: HCursor): Decoder.Result[Aseprite] = {
        val framesCursor = c.downField("frames")
        val arrayResult: Decoder.Result[List[AsepriteFrame]] =
          framesCursor.as[List[AsepriteFrame]]
        val frames: Decoder.Result[List[AsepriteFrame]] =
          arrayResult match
            case Right(fs) =>
              Right(fs)
            case Left(_) =>
              framesCursor.focus match
                case Some(json) if json.isObject =>
                  val pairs = json.asObject.fold(List.empty[(String, Json)])(_.toList)
                  pairs.foldLeft[Decoder.Result[List[AsepriteFrame]]](Right(Nil)) { (acc, kv) =>
                    val (key, value) = kv
                    for {
                      soFar <- acc
                      next  <- decodeAsepriteFrameAt(value.hcursor, key)
                    } yield soFar :+ next
                  }
                case _ =>
                  arrayResult
        for {
          fs   <- frames
          meta <- c.downField("meta").as[AsepriteMeta]
        } yield Aseprite(fs, meta)
      }
    }

  implicit val decodeTiledTerrain: Decoder[TiledTerrain] =
    new Decoder[TiledTerrain] {
      final def apply(c: HCursor): Decoder.Result[TiledTerrain] =
        for {
          name <- c.downField("name").as[String]
          tile <- c.downField("tile").as[Int]
        } yield TiledTerrain(name, tile)
    }

  implicit val decodeTiledTerrainCorner: Decoder[TiledTerrainCorner] =
    new Decoder[TiledTerrainCorner] {
      final def apply(c: HCursor): Decoder.Result[TiledTerrainCorner] =
        for {
          terrain <- c.downField("terrain").as[List[Int]]
        } yield TiledTerrainCorner(terrain)
    }

  implicit val decodeTileSet: Decoder[TileSet] =
    new Decoder[TileSet] {
      final def apply(c: HCursor): Decoder.Result[TileSet] =
        for {
          columns     <- c.downField("columns").as[Option[Int]]
          firstgid    <- c.downField("firstgid").as[Int]
          image       <- c.downField("image").as[Option[String]]
          imageheight <- c.downField("imageheight").as[Option[Int]]
          imagewidth  <- c.downField("imagewidth").as[Option[Int]]
          margin      <- c.downField("margin").as[Option[Int]]
          name        <- c.downField("name").as[Option[String]]
          spacing     <- c.downField("spacing").as[Option[Int]]
          terrains    <- c.downField("terrains").as[Option[List[TiledTerrain]]]
          tilecount   <- c.downField("tilecount").as[Option[Int]]
          tileheight  <- c.downField("tileheight").as[Option[Int]]
          tiles       <- c.downField("tiles").as[Option[Map[String, TiledTerrainCorner]]]
          tilewidth   <- c.downField("tilewidth").as[Option[Int]]
          source      <- c.downField("source").as[Option[String]]
        } yield TileSet(
          columns,
          firstgid,
          image,
          imageheight,
          imagewidth,
          margin,
          name,
          spacing,
          terrains,
          tilecount,
          tileheight,
          tiles,
          tilewidth,
          source
        )
    }

  implicit val decodeTiledLayer: Decoder[TiledLayer] =
    new Decoder[TiledLayer] {
      final def apply(c: HCursor): Decoder.Result[TiledLayer] =
        for {
          name    <- c.downField("name").as[String]
          data    <- c.downField("data").as[List[Int]]
          x       <- c.downField("x").as[Int]
          y       <- c.downField("y").as[Int]
          width   <- c.downField("width").as[Int]
          height  <- c.downField("height").as[Int]
          opacity <- c.downField("opacity").as[Double]
          typ     <- c.downField("type").as[String]
          visible <- c.downField("visible").as[Boolean]
        } yield TiledLayer(
          name,
          data,
          x,
          y,
          width,
          height,
          opacity,
          typ,
          visible
        )
    }

  implicit val decodeTiledMap: Decoder[TiledMap] =
    new Decoder[TiledMap] {
      final def apply(c: HCursor): Decoder.Result[TiledMap] =
        for {
          width           <- c.downField("width").as[Int]
          height          <- c.downField("height").as[Int]
          infinite        <- c.downField("infinite").as[Boolean]
          layers          <- c.downField("layers").as[List[TiledLayer]]
          nextobjectid    <- c.downField("nextobjectid").as[Int]
          orientation     <- c.downField("orientation").as[String]
          renderorder     <- c.downField("renderorder").as[String]
          tiledversion    <- c.downField("tiledversion").as[String]
          tilewidth       <- c.downField("tilewidth").as[Int]
          tileheight      <- c.downField("tileheight").as[Int]
          tilesets        <- c.downField("tilesets").as[List[TileSet]]
          `type`          <- c.downField("type").as[String]
          hexsidelength   <- c.downField("hexsidelength").as[Option[Int]]
          staggeraxis     <- c.downField("staggeraxis").as[Option[String]]
          staggerindex    <- c.downField("staggerindex").as[Option[String]]
          backgroundcolor <- c.downField("backgroundcolor").as[Option[String]]
        } yield TiledMap(
          width,
          height,
          infinite,
          layers,
          nextobjectid,
          orientation,
          renderorder,
          tiledversion,
          tilewidth,
          tileheight,
          tilesets,
          `type`,
          hexsidelength,
          staggeraxis,
          staggerindex,
          backgroundcolor
        )
    }

  implicit val decodeGlyphWrapper: Decoder[GlyphWrapper] =
    new Decoder[GlyphWrapper] {
      final def apply(c: HCursor): Decoder.Result[GlyphWrapper] =
        for {
          glyphs <- c.downField("glyphs").as[List[Glyph]]
        } yield GlyphWrapper(glyphs)
    }

  implicit val decodeGlyph: Decoder[Glyph] =
    new Decoder[Glyph] {
      final def apply(c: HCursor): Decoder.Result[Glyph] =
        for {
          char <- c.downField("char").as[String]
          x    <- c.downField("x").as[Int]
          y    <- c.downField("y").as[Int]
          w    <- c.downField("w").as[Int]
          h    <- c.downField("h").as[Int]
        } yield Glyph(char, x, y, w, h)
    }

}
