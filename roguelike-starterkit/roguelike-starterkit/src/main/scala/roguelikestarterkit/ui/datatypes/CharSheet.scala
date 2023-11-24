package roguelikestarterkit.ui.datatypes

import indigo.*

final case class CharSheet(
    assetName: AssetName,
    size: Size,
    charCrops: Batch[(Int, Int, Int, Int)]
):
  val charSize: Int = size.width
