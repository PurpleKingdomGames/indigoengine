package indigo.shaders.library

import indigo.shaders.library.IndigoUV.*
import ultraviolet.syntax.*

import scala.annotation.nowarn

object Clip:

  trait Env extends VertexEnvReference {
    val CLIP_SHEET_FRAME_COUNT: highp[Float]    = 0.0f
    val CLIP_SHEET_FRAME_DURATION: highp[Float] = 0.0f
    val CLIP_SHEET_WRAP_AT: highp[Float]        = 0.0f
    val CLIP_SHEET_ARRANGEMENT: highp[Float]    = 0.0f
    val CLIP_SHEET_START_OFFSET: highp[Float]   = 0.0f
    val CLIP_PLAY_DIRECTION: highp[Float]       = 0.0f
    val CLIP_PLAYMODE_START_TIME: highp[Float]  = 0.0f
    val CLIP_PLAYMODE_TIMES: highp[Float]       = 0.0f
  }
  object Env:
    val reference: Env = new Env {}

  case class IndigoClipData(
      CLIP_SHEET_FRAME_COUNT: highp[Float],
      CLIP_SHEET_FRAME_DURATION: highp[Float],
      CLIP_SHEET_WRAP_AT: highp[Float],
      CLIP_SHEET_ARRANGEMENT: highp[Float], // 0 = horizontal, 1 = vertical
      CLIP_SHEET_START_OFFSET: highp[Float],
      CLIP_PLAY_DIRECTION: highp[Float], // 0 = forward, 1 = backward, 2 = ping pong
      CLIP_PLAYMODE_START_TIME: highp[Float],
      CLIP_PLAYMODE_TIMES: highp[Float]
  )

  /** Maps a frame index onto a cell in a sprite sheet.
    *
    * Deliberately uses integer arithmetic, and derives the column from the row so the two can never disagree. Float
    * `mod` / `floor` division is inexact on some drivers, and lands one column past the edge of the sheet when the
    * frame index is an exact multiple of the wrap width.
    */
  inline def frameToSheetCell: (Int, Int, Int) => vec2 =
    (currentFrame: Int, wrapAt: Int, arrangement: Int) =>
      val w   = max(wrapAt, 1)
      val f   = max(currentFrame, 0)
      val row = f / w
      val col = f - (row * w)

      // 0 = horizontal, 1 = vertical
      if arrangement == 1 then vec2(row.toFloat, col.toFloat) else vec2(col.toFloat, row.toFloat)

  @nowarn("msg=unused")
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  inline def vertex =
    Shader[Env] { env =>
      // Proxy
      val _frameToSheetCell: (Int, Int, Int) => vec2 =
        frameToSheetCell

      ubo[IndigoClipData]

      def calcCurrentFrame(clipTotalTime: Float): Int = {
        val t: Float =
          val tt = max(env.TIME - env.CLIP_PLAYMODE_START_TIME, 0.0f)
          if env.CLIP_PLAYMODE_TIMES.toInt > 0 then
            min(tt, (clipTotalTime * env.CLIP_PLAYMODE_TIMES) - (env.CLIP_SHEET_FRAME_DURATION * 0.5f))
          else tt

        val frameDuration: Float = max(env.CLIP_SHEET_FRAME_DURATION, 0.0001f)
        val totalFrames: Int     = max(round(clipTotalTime / frameDuration).toInt, 1)
        val tick: Int            = max(floor(t / frameDuration).toInt, 0)

        tick - ((tick / totalFrames) * totalFrames)
      }

      def vertex(v: vec4): vec4 = {
        val direction: Int =
          val d = round(env.CLIP_PLAY_DIRECTION).toInt
          // Can't ping pong if there aren't enough frames.
          if d >= 2 && env.CLIP_SHEET_FRAME_COUNT.toInt <= 2 then 1 else d

        val frameCount: Int = max(round(env.CLIP_SHEET_FRAME_COUNT).toInt, 1)

        var clipTotalTime: Float = 0.0f
        var currentFrame: Int    = 0

        // 0 = forward, 1 = backward, 2 = ping pong, 3 = smooth ping pong
        direction match
          case 0 =>
            clipTotalTime = env.CLIP_SHEET_FRAME_COUNT * env.CLIP_SHEET_FRAME_DURATION
            currentFrame = calcCurrentFrame(clipTotalTime)

          case 1 =>
            clipTotalTime = env.CLIP_SHEET_FRAME_COUNT * env.CLIP_SHEET_FRAME_DURATION
            currentFrame = frameCount - 1 - calcCurrentFrame(clipTotalTime)

          case 2 =>
            clipTotalTime = (env.CLIP_SHEET_FRAME_COUNT + env.CLIP_SHEET_FRAME_COUNT) * env.CLIP_SHEET_FRAME_DURATION
            currentFrame = calcCurrentFrame(clipTotalTime)

            if currentFrame >= frameCount then currentFrame = (frameCount * 2) - 1 - currentFrame

          case 3 =>
            clipTotalTime =
              (env.CLIP_SHEET_FRAME_COUNT + (env.CLIP_SHEET_FRAME_COUNT - 2.0f)) * env.CLIP_SHEET_FRAME_DURATION
            currentFrame = calcCurrentFrame(clipTotalTime)

            if currentFrame >= frameCount then currentFrame = (frameCount * 2) - 2 - currentFrame

          case _ =>
            clipTotalTime = 0.0f
            currentFrame = 0

        val frame: Int = currentFrame + round(env.CLIP_SHEET_START_OFFSET).toInt

        env.UV = env.UV + _frameToSheetCell(
          frame,
          round(env.CLIP_SHEET_WRAP_AT).toInt,
          round(env.CLIP_SHEET_ARRANGEMENT).toInt
        )
        v
      }
    }
