package indigo.scenegraph

trait SceneNodeVisitor[A]:

  def visitBlankEntity(node: BlankEntity): A
  def visitClip(node: Clip[?]): A
  def visitCloneBatch(node: CloneBatch): A
  def visitCloneTiles(node: CloneTiles): A
  def visitEntityNode(node: EntityNode[?]): A
  def visitGraphic(node: Graphic[?]): A
  def visitGroup(node: Group): A
  def visitMutants(node: Mutants): A
  def visitShapeBox(node: Shape.Box): A
  def visitShapeCircle(node: Shape.Circle): A
  def visitShapeLine(node: Shape.Line): A
  def visitShapePolygon(node: Shape.Polygon): A
  def visitSprite(node: Sprite[?]): A
  def visitText(node: Text[?]): A
