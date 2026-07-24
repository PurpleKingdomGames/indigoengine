package indigo.shared.formats

import indigo.core.animation.*
import indigo.core.assets.AssetName
import indigo.core.datatypes.*
import indigo.core.dice.Dice
import indigo.scenegraph.*
import indigo.scenegraph.materials.Material
import indigoengine.shared.datatypes.Millis
import indigoengine.shared.datatypes.Radians

@SuppressWarnings(Array("scalafix:DisableSyntax.noValPatterns"))
class AsepriteTests extends munit.FunSuite {

  test("should be able to convert the loaded definition into a renderable Sprite object") {
    val SpriteAndAnimations(sprite, animation) =
      AsepriteSampleData.aseprite
        .toSpriteAndAnimations(Dice.loaded(0), AsepriteSampleData.imageAssetRef)
        .get

    assertEquals(sprite.bindingKey, AsepriteSampleData.sprite.bindingKey)
    assertEquals(sprite.animationKey, AsepriteSampleData.sprite.animationKey)

    assertEquals(animation.cycles.length, 1)
    assertEquals(animation.currentCycleLabel, CycleLabel("lights"))
    assertEquals(animation.cycles.find(c => c.label == animation.currentCycleLabel).get.frames.length, 3)
  }

  test("should be able to convert the loaded definition into a Map of Clip instances") {
    val actual: Either[AsepriteError, Map[CycleLabel, Clip[Material.Bitmap]]] =
      AsepriteSampleData.aseprite
        .toClips(AsepriteSampleData.imageAssetRef)

    assert(clue(actual.isRight))

    val clips: Map[CycleLabel, Clip[Material.Bitmap]] =
      actual.toOption.get

    assertEquals(clips.size, 1)
    assert(clue(clips.contains(CycleLabel("lights"))))

    val clip: Clip[Material.Bitmap] = clips.get(CycleLabel("lights")).get

    assertEquals(clip.sheet.frameCount, 3)
    assert(clue(clip.sheet.frameDuration ~== Millis(100).toSeconds))
    assertEquals(clip.sheet.arrangement, ClipSheetArrangement.Horizontal)
    assertEquals(clip.sheet.wrapAt, 2)
    assertEquals(clip.sheet.startOffset, 0)
    assertEquals(clip.playMode, ClipPlayMode.loop)
  }

  test("Sprite frames carry spriteSourceSize as offset (trimmed exports)") {
    val trimmedAseprite =
      Aseprite(
        frames = List(
          AsepriteFrame(
            filename = "f0",
            frame = AsepriteRectangle(24, 139, 21, 33),
            rotated = false,
            trimmed = true,
            spriteSourceSize = AsepriteRectangle(8, 2, 15, 27),
            sourceSize = AsepriteSize(32, 32),
            duration = 100
          ),
          AsepriteFrame(
            filename = "f1",
            frame = AsepriteRectangle(2, 242, 19, 30),
            rotated = false,
            trimmed = true,
            spriteSourceSize = AsepriteRectangle(9, 5, 13, 24),
            sourceSize = AsepriteSize(32, 32),
            duration = 100
          )
        ),
        meta = AsepriteMeta(
          app = "aseprite",
          version = "1.3",
          image = None,
          format = "RGBA8888",
          size = AsepriteSize(128, 276),
          scale = "1",
          frameTags = List(
            AsepriteFrameTag("walk", 0, 1, "forward", None, None, None)
          ),
          slices = None
        )
      )

    val SpriteAndAnimations(_, animation) =
      trimmedAseprite.toSpriteAndAnimations(Dice.loaded(0), AssetName("trimmed")).get

    val frames =
      animation.cycles.find(_.label == CycleLabel("walk")).get.frames

    assertEquals(frames.toBatch.toList.length, 2)
    assertEquals(frames.toBatch.toList.head.offset, Point(8, 2))
    assertEquals(frames.toBatch.toList(1).offset, Point(9, 5))
  }

  test("toClips rejects trimmed exports with a clear error") {
    val trimmed =
      Aseprite(
        frames = List(
          AsepriteFrame(
            filename = "f0",
            frame = AsepriteRectangle(0, 0, 21, 33),
            rotated = false,
            trimmed = true,
            spriteSourceSize = AsepriteRectangle(8, 2, 15, 27),
            sourceSize = AsepriteSize(32, 32),
            duration = 100
          )
        ),
        meta = AsepriteMeta(
          app = "aseprite",
          version = "1.3",
          image = None,
          format = "RGBA8888",
          size = AsepriteSize(21, 33),
          scale = "1",
          frameTags = List(AsepriteFrameTag("a", 0, 0, "forward", None, None, None)),
          slices = None
        )
      )

    trimmed.toClips(AssetName("x")) match
      case Left(err) =>
        assert(clue(err.message).toLowerCase.contains("trim"))
      case Right(_) =>
        fail("Expected Left for trimmed aseprite")
  }

  test("toClips rejects sheets with packed/duplicate frame positions") {
    val packed =
      Aseprite(
        frames = List(
          AsepriteFrame("f0", AsepriteRectangle(0, 0, 32, 32), false, false, AsepriteRectangle(0, 0, 32, 32), AsepriteSize(32, 32), 100),
          AsepriteFrame("f1", AsepriteRectangle(32, 0, 32, 32), false, false, AsepriteRectangle(0, 0, 32, 32), AsepriteSize(32, 32), 100),
          AsepriteFrame("f2", AsepriteRectangle(0, 0, 32, 32), false, false, AsepriteRectangle(0, 0, 32, 32), AsepriteSize(32, 32), 100)
        ),
        meta = AsepriteMeta(
          app = "aseprite",
          version = "1.3",
          image = None,
          format = "RGBA8888",
          size = AsepriteSize(64, 32),
          scale = "1",
          frameTags = List(AsepriteFrameTag("a", 0, 2, "forward", None, None, None)),
          slices = None
        )
      )

    packed.toClips(AssetName("x")) match
      case Left(err) =>
        assert(clue(err.message).toLowerCase.contains("packed") || clue(err.message).toLowerCase.contains("duplicate") || clue(err.message).toLowerCase.contains("share"))
      case Right(_) =>
        fail("Expected Left for packed aseprite")
  }

  test("toClips computes wrapAt from frame stride, not meta.size") {
    val padded =
      Aseprite(
        frames = List(
          AsepriteFrame("f0", AsepriteRectangle(0, 0, 32, 32), false, false, AsepriteRectangle(0, 0, 32, 32), AsepriteSize(32, 32), 100),
          AsepriteFrame("f1", AsepriteRectangle(32, 0, 32, 32), false, false, AsepriteRectangle(0, 0, 32, 32), AsepriteSize(32, 32), 100),
          AsepriteFrame("f2", AsepriteRectangle(64, 0, 32, 32), false, false, AsepriteRectangle(0, 0, 32, 32), AsepriteSize(32, 32), 100),
          AsepriteFrame("f3", AsepriteRectangle(0, 32, 32, 32), false, false, AsepriteRectangle(0, 0, 32, 32), AsepriteSize(32, 32), 100),
          AsepriteFrame("f4", AsepriteRectangle(32, 32, 32, 32), false, false, AsepriteRectangle(0, 0, 32, 32), AsepriteSize(32, 32), 100),
          AsepriteFrame("f5", AsepriteRectangle(64, 32, 32, 32), false, false, AsepriteRectangle(0, 0, 32, 32), AsepriteSize(32, 32), 100)
        ),
        meta = AsepriteMeta(
          app = "aseprite",
          version = "1.3",
          image = None,
          format = "RGBA8888",
          size = AsepriteSize(200, 64),
          scale = "1",
          frameTags = List(AsepriteFrameTag("a", 0, 5, "forward", None, None, None)),
          slices = None
        )
      )

    val clips = padded.toClips(AssetName("x")).toOption.get
    val clip  = clips(CycleLabel("a"))
    assertEquals(clip.sheet.wrapAt, 3)
  }

  test("toSpriteAndAnimations reverses frames when direction is 'reverse'") {
    val src =
      Aseprite(
        frames = List(
          AsepriteFrame("f0", AsepriteRectangle(0, 0, 10, 10), false, false, AsepriteRectangle(0, 0, 10, 10), AsepriteSize(10, 10), 100),
          AsepriteFrame("f1", AsepriteRectangle(10, 0, 10, 10), false, false, AsepriteRectangle(0, 0, 10, 10), AsepriteSize(10, 10), 100),
          AsepriteFrame("f2", AsepriteRectangle(20, 0, 10, 10), false, false, AsepriteRectangle(0, 0, 10, 10), AsepriteSize(10, 10), 100)
        ),
        meta = AsepriteMeta(
          app = "aseprite",
          version = "1.3",
          image = None,
          format = "RGBA8888",
          size = AsepriteSize(30, 10),
          scale = "1",
          frameTags = List(AsepriteFrameTag("rev", 0, 2, "reverse", None, None, None)),
          slices = None
        )
      )

    val SpriteAndAnimations(_, animation) =
      src.toSpriteAndAnimations(Dice.loaded(0), AssetName("x")).get

    val frames =
      animation.cycles.find(_.label == CycleLabel("rev")).get.frames.toBatch.toList

    assertEquals(frames.map(_.crop.position.x), List(20, 10, 0))
  }

  test("out-of-range frame tag yields no cycle (and logs)") {
    val bad =
      Aseprite(
        frames = List(
          AsepriteFrame("f0", AsepriteRectangle(0, 0, 10, 10), false, false, AsepriteRectangle(0, 0, 10, 10), AsepriteSize(10, 10), 100)
        ),
        meta = AsepriteMeta(
          app = "aseprite",
          version = "1.3",
          image = None,
          format = "RGBA8888",
          size = AsepriteSize(10, 10),
          scale = "1",
          frameTags = List(AsepriteFrameTag("oops", 0, 5, "forward", None, None, None)),
          slices = None
        )
      )

    assertEquals(bad.toSpriteAndAnimations(Dice.loaded(0), AssetName("x")), None)
  }

}

object AsepriteSampleData {

  val imageAssetRef: AssetName = AssetName("trafficlights")

  val aseprite: Aseprite =
    Aseprite(
      frames = List(
        AsepriteFrame(
          filename = "trafficlights 0.ase",
          frame = AsepriteRectangle(0, 0, 64, 64),
          rotated = false,
          trimmed = false,
          spriteSourceSize = AsepriteRectangle(0, 0, 64, 64),
          sourceSize = AsepriteSize(64, 64),
          duration = 100
        ),
        AsepriteFrame(
          filename = "trafficlights 1.ase",
          frame = AsepriteRectangle(64, 0, 64, 64),
          rotated = false,
          trimmed = false,
          spriteSourceSize = AsepriteRectangle(0, 0, 64, 64),
          sourceSize = AsepriteSize(64, 64),
          duration = 100
        ),
        AsepriteFrame(
          filename = "trafficlights 2.ase",
          frame = AsepriteRectangle(0, 64, 64, 64),
          rotated = false,
          trimmed = false,
          spriteSourceSize = AsepriteRectangle(0, 0, 64, 64),
          sourceSize = AsepriteSize(64, 64),
          duration = 100
        )
      ),
      meta = AsepriteMeta(
        app = "http://www.aseprite.org/",
        version = "1.1.13",
        image = None,
        format = "RGBA8888",
        size = AsepriteSize(128, 128),
        scale = "1",
        frameTags = List(
          AsepriteFrameTag(
            name = "lights",
            from = 0,
            to = 2,
            direction = "forward",
            color = None,
            data = None,
            repeat = None
          )
        ),
        slices = None
      )
    )

  val animationKey: AnimationKey = AnimationKey("0000000000000000")

  val sprite: Sprite[?] =
    Sprite(
      bindingKey = BindingKey("0000000000000000"),
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      animationKey = animationKey,
      ref = Point.zero,
      Function.const(None),
      Material.Bitmap(imageAssetRef)
    )

}
