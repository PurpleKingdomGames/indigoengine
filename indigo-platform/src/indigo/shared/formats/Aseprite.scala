package indigo.shared.formats

import indigo.core.animation.Animation
import indigo.core.animation.AnimationKey
import indigo.core.animation.Cycle
import indigo.core.animation.CycleLabel
import indigo.core.animation.Frame
import indigo.core.assets.AssetName
import indigo.core.datatypes.BindingKey
import indigo.core.datatypes.Flip
import indigo.core.datatypes.Point
import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.Size
import indigo.core.datatypes.Vector2
import indigo.core.dice.Dice
import indigo.core.utils.IndigoLogger
import indigo.scenegraph.Clip
import indigo.scenegraph.ClipPlayDirection
import indigo.scenegraph.ClipPlayMode
import indigo.scenegraph.ClipSheet
import indigo.scenegraph.ClipSheetArrangement
import indigo.scenegraph.Sprite
import indigo.scenegraph.materials.Material
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.NonEmptyBatch
import indigoengine.shared.datatypes.Millis
import indigoengine.shared.datatypes.Radians

opaque type AsepriteError = String
object AsepriteError:
  def apply(message: String): AsepriteError = message

  extension (e: AsepriteError)
    def message: String  = e
    def toMessage: String = e

  given CanEqual[AsepriteError, AsepriteError] = CanEqual.derived

final case class Aseprite(frames: List[AsepriteFrame], meta: AsepriteMeta) derives CanEqual:
  def toSpriteAndAnimations(dice: Dice, assetName: AssetName): Option[SpriteAndAnimations] =
    Aseprite.toSpriteAndAnimations(this, dice, assetName)

  def toClips(assetName: AssetName): Either[AsepriteError, Map[CycleLabel, Clip[Material.Bitmap]]] =
    Aseprite.toClips(this, assetName)

final case class AsepriteFrame(
    filename: String,
    frame: AsepriteRectangle,
    rotated: Boolean,
    trimmed: Boolean,
    spriteSourceSize: AsepriteRectangle,
    sourceSize: AsepriteSize,
    duration: Int
) derives CanEqual

final case class AsepriteRectangle(x: Int, y: Int, w: Int, h: Int) derives CanEqual:
  def position: Point = Point(x, y)
  def size: Size      = Size(w, h)

final case class AsepriteMeta(
    app: String,
    version: String,
    image: Option[String],
    format: String,
    size: AsepriteSize,
    scale: String,
    frameTags: List[AsepriteFrameTag],
    slices: Option[List[AsepriteSlice]]
) derives CanEqual

final case class AsepriteSize(w: Int, h: Int) derives CanEqual:
  def toSize: Size = Size(w, h)

final case class AsepriteFrameTag(
    name: String,
    from: Int,
    to: Int,
    direction: String,
    color: Option[String],
    data: Option[String],
    repeat: Option[String]
) derives CanEqual

final case class AsepriteSlice(
    name: String,
    color: Option[String],
    data: Option[String],
    keys: List[AsepriteSliceKey]
) derives CanEqual

final case class AsepriteSliceKey(
    frame: Int,
    bounds: AsepriteRectangle,
    center: Option[AsepriteRectangle],
    pivot: Option[AsepritePoint]
) derives CanEqual

final case class AsepritePoint(x: Int, y: Int) derives CanEqual

final case class SpriteAndAnimations(sprite: Sprite[Material.Bitmap], animations: Animation) derives CanEqual:
  def modifySprite(alter: Sprite[Material.Bitmap] => Sprite[Material.Bitmap]): SpriteAndAnimations =
    this.copy(sprite = alter(sprite))

object Aseprite:

  def toSpriteAndAnimations(aseprite: Aseprite, dice: Dice, assetName: AssetName): Option[SpriteAndAnimations] =
    extractCycles(aseprite) match {
      case Nil =>
        IndigoLogger.info("No animation frames found in Aseprite")
        None
      case x :: xs =>
        val animations: Animation =
          Animation(
            animationKey = AnimationKey.fromDice(dice),
            currentCycleLabel = x.label,
            cycles = NonEmptyBatch.pure(x, Batch.fromList(xs))
          )
        Option(
          SpriteAndAnimations(
            Sprite(
              bindingKey = BindingKey.fromDice(dice),
              material = Material.Bitmap(assetName),
              animationKey = animations.animationKey,
              animationActions = Batch.empty,
              eventHandlerEnabled = false,
              eventHandler = Function.const(None),
              position = Point(0, 0),
              rotation = Radians.zero,
              scale = Vector2.one,
              ref = Point(0, 0),
              flip = Flip.default
            ),
            animations
          )
        )
    }

  def toClips(aseprite: Aseprite, assetName: AssetName): Either[AsepriteError, Map[CycleLabel, Clip[Material.Bitmap]]] =
    extractClipData(aseprite).map { clipDataList =>
      clipDataList.map { clipData =>
        clipData.label ->
          Clip(
            size = clipData.size,
            sheet = clipData.sheet,
            playMode = clipData.playMode,
            material = Material.Bitmap(assetName),
            eventHandlerEnabled = false,
            eventHandler = Function.const(None),
            position = Point.zero,
            rotation = Radians.zero,
            scale = Vector2.one,
            ref = Point.zero,
            flip = Flip.default
          )
      }.toMap
    }

  private def extractCycles(aseprite: Aseprite): List[Cycle] =
    aseprite.meta.frameTags
      .map { frameTag =>
        extractFrames(frameTag, aseprite.frames) match {
          case Nil =>
            IndigoLogger.info(s"Failed to extract cycle with frameTag: ${frameTag.toString()}")
            None
          case x :: xs =>
            Option(
              Cycle.create(frameTag.name, NonEmptyBatch.pure(x, Batch.fromList(xs)))
            )
        }
      }
      .collect { case Some(s) => s }

  private def extractFrames(frameTag: AsepriteFrameTag, asepriteFrames: List[AsepriteFrame]): List[Frame] =
    if frameTag.from < 0 || frameTag.to >= asepriteFrames.length || frameTag.from > frameTag.to then {
      IndigoLogger.error(
        s"Aseprite tag '${frameTag.name}' has out-of-range frame indices: from=${frameTag.from}, to=${frameTag.to}, frames.length=${asepriteFrames.length}"
      )
      Nil
    } else {
      val baseFrames =
        asepriteFrames.slice(frameTag.from, frameTag.to + 1).map { aseFrame =>
          Frame(
            crop = Rectangle(
              position = Point(aseFrame.frame.x, aseFrame.frame.y),
              size = Size(aseFrame.frame.w, aseFrame.frame.h)
            ),
            duration = Millis(aseFrame.duration.toLong),
            offset = Point(aseFrame.spriteSourceSize.x, aseFrame.spriteSourceSize.y)
          )
        }
      applyDirection(frameTag.direction, baseFrames)
    }

  private def applyDirection(direction: String, frames: List[Frame]): List[Frame] =
    direction match
      case "reverse" =>
        frames.reverse
      case "pingpong" if frames.length > 2 =>
        frames ++ frames.reverse.drop(1).dropRight(1)
      case "pingpong_reverse" if frames.length > 2 =>
        frames.reverse ++ frames.drop(1).dropRight(1)
      case _ =>
        frames

  private def directionToClipPlayDirection(direction: String): ClipPlayDirection =
    direction match
      case "reverse"          => ClipPlayDirection.Backward
      case "pingpong"         => ClipPlayDirection.PingPong
      case "pingpong_reverse" => ClipPlayDirection.PingPong
      case _                  => ClipPlayDirection.Forward

  private def extractClipData(aseprite: Aseprite): Either[AsepriteError, List[ClipData]] = {
    val frames = aseprite.frames
    if frames.exists(_.trimmed) then {
      val msg =
        "Cannot convert trimmed Aseprite export to Clips. Re-export with the 'Trim' option disabled, or use toSpriteAndAnimations instead."
      IndigoLogger.error(msg)
      Left(AsepriteError(msg))
    } else
      frames match
        case Nil =>
          val msg = "No frames were found during Aseprite conversion to Clips"
          IndigoLogger.error(msg)
          Left(AsepriteError(msg))

        case f :: Nil =>
          Right(
            aseprite.meta.frameTags.map { frameTag =>
              ClipData(
                label = CycleLabel(frameTag.name),
                size = f.frame.size,
                sheet = ClipSheet(
                  frameCount = (frameTag.to - frameTag.from) + 1,
                  frameDuration = Millis(f.duration.toLong).toSeconds,
                  wrapAt = 1,
                  arrangement = ClipSheetArrangement.Horizontal,
                  startOffset = frameTag.from
                ),
                playMode = ClipPlayMode.Loop(directionToClipPlayDirection(frameTag.direction))
              )
            }
          )

        case f1 :: f2 :: _ =>
          val duplicate = frames
            .map(f => (f.frame.x, f.frame.y))
            .groupBy(identity)
            .find(_._2.length > 1)

          duplicate match
            case Some(((dx, dy), _)) =>
              val msg =
                s"Cannot convert Aseprite to Clips: multiple frames share atlas position ($dx, $dy). " +
                  "Aseprite has packed duplicate frames - disable 'Merge Duplicates' on export, or use toSpriteAndAnimations."
              IndigoLogger.error(msg)
              Left(AsepriteError(msg))

            case None =>
              val arrangement: ClipSheetArrangement =
                if f2.frame.x > f1.frame.x then ClipSheetArrangement.Horizontal
                else ClipSheetArrangement.Vertical

              val cellW = f1.frame.w
              val cellH = f1.frame.h

              val wrapAt: Int = arrangement match
                case ClipSheetArrangement.Horizontal =>
                  frames.indexWhere(_.frame.y != f1.frame.y) match
                    case -1 => frames.length
                    case n  => n
                case ClipSheetArrangement.Vertical =>
                  frames.indexWhere(_.frame.x != f1.frame.x) match
                    case -1 => frames.length
                    case n  => n

              val misaligned = frames.zipWithIndex.find { case (frame, idx) =>
                val (expectedX, expectedY) = arrangement match
                  case ClipSheetArrangement.Horizontal =>
                    ((idx % wrapAt) * cellW, (idx / wrapAt) * cellH)
                  case ClipSheetArrangement.Vertical =>
                    ((idx / wrapAt) * cellW, (idx % wrapAt) * cellH)
                frame.frame.x != expectedX ||
                frame.frame.y != expectedY ||
                frame.frame.w != cellW ||
                frame.frame.h != cellH
              }

              misaligned match
                case Some((frame, idx)) =>
                  val msg =
                    s"Cannot convert Aseprite to Clips: frame $idx at (${frame.frame.x}, ${frame.frame.y}, ${frame.frame.w}x${frame.frame.h}) " +
                      s"is not aligned to the expected ${cellW}x${cellH} grid (wrapAt=$wrapAt, arrangement=$arrangement)."
                  IndigoLogger.error(msg)
                  Left(AsepriteError(msg))

                case None =>
                  Right(
                    aseprite.meta.frameTags.map { frameTag =>
                      ClipData(
                        label = CycleLabel(frameTag.name),
                        size = f1.frame.size,
                        sheet = ClipSheet(
                          frameCount = (frameTag.to - frameTag.from) + 1,
                          frameDuration = Millis(f1.duration.toLong).toSeconds,
                          wrapAt = wrapAt,
                          arrangement = arrangement,
                          startOffset = frameTag.from
                        ),
                        playMode = ClipPlayMode.Loop(directionToClipPlayDirection(frameTag.direction))
                      )
                    }
                  )
  }

final case class ClipData(label: CycleLabel, size: Size, sheet: ClipSheet, playMode: ClipPlayMode)
