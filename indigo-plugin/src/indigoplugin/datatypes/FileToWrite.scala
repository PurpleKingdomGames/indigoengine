package indigoplugin.datatypes

import os.*

@SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
final case class FileToWrite(name: String, contents: String, folderPath: RelPath = RelPath.rel)
