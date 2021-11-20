package demo

import indigo._

object Assets:

  val tileMap        = AssetName("Anikki_square_10x10")
  val textFragShader = AssetName("terminal text frag")

  val assets: Set[AssetType] =
    Set(
      AssetType.Image(tileMap, AssetPath("assets/" + tileMap.toString + ".png")),
      AssetType.Text(textFragShader, AssetPath("assets/shaders/text.frag"))
    )

end Assets
