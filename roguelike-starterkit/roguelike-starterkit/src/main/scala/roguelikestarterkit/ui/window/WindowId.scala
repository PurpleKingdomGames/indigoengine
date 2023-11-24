package roguelikestarterkit.ui.window

opaque type WindowId = String
object WindowId:
  def apply(id: String): WindowId               = id
  extension (id: WindowId) def toString: String = id
