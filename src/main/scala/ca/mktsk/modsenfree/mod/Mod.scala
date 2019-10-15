package ca.mktsk.modsenfree.mod

import java.io.File

case class Mod(name: String, enabled: Boolean, file: File)

object Mod {
  val nameFieldName = "name"
  val enabledFieldName = "enabled"
  val fileFieldName = "file"

}
