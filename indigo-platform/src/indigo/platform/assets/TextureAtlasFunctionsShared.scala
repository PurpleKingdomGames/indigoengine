package indigo.platform.assets

import indigo.core.datatypes.PowerOfTwo
import indigo.core.utils.IndigoLogger

import scala.annotation.tailrec

object TextureAtlasFunctionsShared:

  /** Type fails all over the place, no guarantee that this list is in the right order... so instead of just going
    * through the set until we find a bigger value, we have to filter and fold all
    */
  def pickPowerOfTwoSizeFor(supportedSizes: Set[PowerOfTwo], width: Int, height: Int): PowerOfTwo =
    supportedSizes
      .filter(s => s.value >= width && s.value >= height)
      .foldLeft(PowerOfTwo.Max)(PowerOfTwo.min)

  def isTooBig(max: PowerOfTwo, width: Int, height: Int): Boolean =
    if (width > max.value || height > max.value) true else false

  val inflateAndSortByPowerOfTwo: List[ImageRef] => List[TextureDetails] = images =>
    images
      .map(i =>
        TextureDetails(
          i,
          pickPowerOfTwoSizeFor(TextureAtlas.supportedSizes, i.width, i.height),
          i.tag
        )
      )
      .sortBy(_.size.value)
      .reverse

  def groupTexturesIntoAtlasBuckets(max: PowerOfTwo): List[TextureDetails] => List[List[TextureDetails]] =
    list => {
      val runningTotal: List[TextureDetails] => Int = _.map(_.size.value).sum

      @tailrec
      def createBuckets(
          remaining: List[TextureDetails],
          current: List[TextureDetails],
          rejected: List[TextureDetails],
          acc: List[List[TextureDetails]],
          maximum: PowerOfTwo
      ): List[List[TextureDetails]] =
        (remaining, rejected) match {
          case (Nil, Nil) =>
            current :: acc

          case (Nil, x :: xs) =>
            createBuckets(x :: xs, Nil, Nil, current :: acc, maximum)

          case (x :: xs, _) if x.size >= maximum =>
            createBuckets(xs, current, rejected, List(x) :: acc, maximum)

          case (x :: xs, _) if runningTotal(current) + x.size.value > maximum.value * 2 =>
            createBuckets(xs, current, x :: rejected, acc, maximum)

          case (x :: xs, _) =>
            createBuckets(xs, x :: current, rejected, acc, maximum)

        }

      def sortAndGroupByTag: List[TextureDetails] => List[(String, List[TextureDetails])] =
        _.groupBy(_.tag.map(_.toString).getOrElse("")).toList.sortBy(_._1)

      sortAndGroupByTag(list).flatMap { case (_, tds) =>
        createBuckets(tds, Nil, Nil, Nil, max)
      }
    }

  def convertTextureDetailsToTree(textureDetails: TextureDetails): AtlasQuadTree =
    AtlasQuadNode(textureDetails.size, AtlasTexture(textureDetails.imageRef))

  def mergeTrees(a: AtlasQuadTree, b: AtlasQuadTree, max: PowerOfTwo): Option[AtlasQuadTree] =
    (a, b) match {
      case (AtlasQuadEmpty(_), AtlasQuadEmpty(_)) =>
        Some(a)

      case (AtlasQuadNode(_, _), AtlasQuadEmpty(_)) =>
        Some(a)

      case (AtlasQuadEmpty(_), AtlasQuadNode(_, _)) =>
        Some(b)

      case (AtlasQuadNode(_, _), AtlasQuadNode(sizeB, _)) if a.canAccommodate(sizeB) =>
        mergeTreeBIntoA(a, b)

      case (AtlasQuadNode(sizeA, _), AtlasQuadNode(_, _)) if b.canAccommodate(sizeA) =>
        mergeTreeBIntoA(b, a)

      case (AtlasQuadNode(sizeA, _), AtlasQuadNode(sizeB, _)) if sizeA >= sizeB && sizeA.doubled <= max =>
        mergeTreeBIntoA(createEmptyTree(calculateSizeNeededToHouseAB(sizeA, sizeB)), a).flatMap { c =>
          mergeTreeBIntoA(c, b)
        }

      case (AtlasQuadNode(sizeA, _), AtlasQuadNode(sizeB, _)) if sizeB >= sizeA && sizeB.doubled <= max =>
        mergeTreeBIntoA(createEmptyTree(calculateSizeNeededToHouseAB(sizeA, sizeB)), b).flatMap { c =>
          mergeTreeBIntoA(c, a)
        }

      case _ =>
        IndigoLogger.info("Could not merge trees")
        None
    }

  def mergeTreeBIntoA(a: AtlasQuadTree, b: AtlasQuadTree): Option[AtlasQuadTree] =
    if (!a.canAccommodate(b.size) && !b.canAccommodate(a.size)) None
    else
      Option {
        if (a.canAccommodate(b.size)) a.insert(b) else b.insert(a)
      }

  def calculateSizeNeededToHouseAB(sizeA: PowerOfTwo, sizeB: PowerOfTwo): PowerOfTwo =
    if (sizeA >= sizeB) sizeA.doubled else sizeB.doubled

  def createEmptyTree(size: PowerOfTwo): AtlasQuadNode =
    AtlasQuadNode(size, AtlasQuadDivision.empty(size.halved))
