package millindigo

object MillIndigo:

  object Utils {
    def findWorkspace: os.Path =
      sys.env
        .get("MILL_WORKSPACE_ROOT")
        .map(os.Path(_))
        .getOrElse(os.pwd)
  }
