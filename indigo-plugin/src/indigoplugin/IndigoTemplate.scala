package indigoplugin

/** Tells Indigo whether to generate the default static site for your game, or use a custom provided template instead.
  */
enum IndigoTemplate derives CanEqual:
  /** Use the detault static site template */
  case Default

  /** Use a custom provided static site template */
  case Custom(inputs: IndigoTemplate.Inputs, outputs: IndigoTemplate.Outputs)

object IndigoTemplate:

  /** Input parameters for a custom static template
    *
    * @param templateSource
    *   The directory holding all the files and folders to be copied across to use in the template.
    */
  final case class Inputs(templateSource: os.Path)

  /** Output parameters for a custom static template
    *
    * @param assets
    *   The directory to copy assets into. This directory must exist in the folder specified in `Inputs#templateSource`.
    * @param gameScripts
    *   The directory to copy the compiled game script files into. This directory must exist in the folder specified in
    *   `Inputs#templateSource`.
    */
  final case class Outputs(
      assets: os.RelPath,
      gameScripts: os.RelPath
  )
