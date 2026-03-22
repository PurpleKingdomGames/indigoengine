package indigo.platform.assets

import indigo.core.datatypes.Point
import indigo.core.datatypes.PowerOfTwo
import indigo.core.utils.IndigoLogger

sealed trait AtlasQuadTree:
  val size: PowerOfTwo
  def canAccommodate(requiredSize: PowerOfTwo): Boolean
  def insert(tree: AtlasQuadTree): AtlasQuadTree

  def +(other: AtlasQuadTree): AtlasQuadTree = AtlasQuadTree.append(this, other)

  def toTextureCoordsList(offset: Point): List[TextureAndCoords]

object AtlasQuadTree:

  def identity: AtlasQuadTree = AtlasQuadEmpty(PowerOfTwo._2)

  def append(first: AtlasQuadTree, second: AtlasQuadTree): AtlasQuadTree =
    TextureAtlasFunctionsShared.mergeTrees(first, second, PowerOfTwo.Max).getOrElse(first)

final case class AtlasQuadNode(size: PowerOfTwo, atlas: AtlasSum) extends AtlasQuadTree derives CanEqual:
  def canAccommodate(requiredSize: PowerOfTwo): Boolean =
    if (size < requiredSize) false
    else atlas.canAccommodate(requiredSize)

  def insert(tree: AtlasQuadTree): AtlasQuadTree =
    this.copy(atlas = atlas match {
      case AtlasTexture(_) => this.atlas

      case d @ AtlasQuadDivision(AtlasQuadEmpty(s), _, _, _) if s == tree.size =>
        d.copy(q1 = tree)
      case d @ AtlasQuadDivision(_, AtlasQuadEmpty(s), _, _) if s == tree.size =>
        d.copy(q2 = tree)
      case d @ AtlasQuadDivision(_, _, AtlasQuadEmpty(s), _) if s == tree.size =>
        d.copy(q3 = tree)
      case d @ AtlasQuadDivision(_, _, _, AtlasQuadEmpty(s)) if s == tree.size =>
        d.copy(q4 = tree)

      case d @ AtlasQuadDivision(AtlasQuadEmpty(s), _, _, _) if s > tree.size =>
        d.copy(q1 = TextureAtlasFunctionsShared.createEmptyTree(s).insert(tree))
      case d @ AtlasQuadDivision(_, AtlasQuadEmpty(s), _, _) if s > tree.size =>
        d.copy(q2 = TextureAtlasFunctionsShared.createEmptyTree(s).insert(tree))
      case d @ AtlasQuadDivision(_, _, AtlasQuadEmpty(s), _) if s > tree.size =>
        d.copy(q3 = TextureAtlasFunctionsShared.createEmptyTree(s).insert(tree))
      case d @ AtlasQuadDivision(_, _, _, AtlasQuadEmpty(s)) if s > tree.size =>
        d.copy(q4 = TextureAtlasFunctionsShared.createEmptyTree(s).insert(tree))

      case d @ AtlasQuadDivision(AtlasQuadNode(_, _), _, _, _) if d.q1.canAccommodate(tree.size) =>
        d.copy(q1 = d.q1.insert(tree))
      case d @ AtlasQuadDivision(_, AtlasQuadNode(_, _), _, _) if d.q2.canAccommodate(tree.size) =>
        d.copy(q2 = d.q2.insert(tree))
      case d @ AtlasQuadDivision(_, _, AtlasQuadNode(_, _), _) if d.q3.canAccommodate(tree.size) =>
        d.copy(q3 = d.q3.insert(tree))
      case d @ AtlasQuadDivision(_, _, _, AtlasQuadNode(_, _)) if d.q4.canAccommodate(tree.size) =>
        d.copy(q4 = d.q4.insert(tree))

      case _ =>
        IndigoLogger.info("Unexpected failure to insert tree")
        this.atlas
    })

  def toTextureCoordsList(offset: Point): List[TextureAndCoords] =
    atlas match {
      case AtlasTexture(imageRef) =>
        List(TextureAndCoords(imageRef, offset))

      case AtlasQuadDivision(q1, q2, q3, q4) =>
        q1.toTextureCoordsList(offset) ++
          q2.toTextureCoordsList(offset + size.halved.toPoint.withY(0)) ++
          q3.toTextureCoordsList(offset + size.halved.toPoint.withX(0)) ++
          q4.toTextureCoordsList(offset + size.halved.toPoint)

    }

  def toTextureMap: TextureMap =
    TextureMap(size, toTextureCoordsList(Point.zero))

final case class AtlasQuadEmpty(size: PowerOfTwo) extends AtlasQuadTree derives CanEqual:
  def canAccommodate(requiredSize: PowerOfTwo): Boolean = size >= requiredSize
  def insert(tree: AtlasQuadTree): AtlasQuadTree        = this

  def toTextureCoordsList(offset: Point): List[TextureAndCoords] = Nil
