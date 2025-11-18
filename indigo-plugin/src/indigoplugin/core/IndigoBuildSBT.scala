package indigoplugin.core

import indigoplugin.IndigoOptions
import indigoplugin.utils.AsciiLogo

object IndigoBuildSBT {

  def build(
      scriptPathBase: os.Path,
      options: IndigoOptions,
      assetsDirectory: os.Path,
      baseDirectory: os.Path,
      outputFolderName: String,
      scriptNames: List[String]
  ): Unit = {

    println(AsciiLogo.logo)

    IndigoBuild.build(
      scriptPathBase,
      options,
      assetsDirectory,
      baseDirectory / "target" / outputFolderName,
      scriptNames
    )
  }

}
