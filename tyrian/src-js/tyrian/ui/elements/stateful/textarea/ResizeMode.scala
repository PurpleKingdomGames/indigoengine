package tyrian.ui.elements.stateful.textarea

enum ResizeMode derives CanEqual:
  case None, Both, Horizontal, Vertical

  def toCSSValue: String =
    this match
      case None       => "none"
      case Both       => "both"
      case Horizontal => "horizontal"
      case Vertical   => "vertical"
