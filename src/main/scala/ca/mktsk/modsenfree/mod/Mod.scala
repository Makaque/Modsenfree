package ca.mktsk.modsenfree.mod

import java.io.File

case class Mod(id: String, displayName: String, enabled: Boolean, file: File)

object Mod {

  case class FieldNameMod(id: String, displayName: String, enabled: String, file: String)

  val field = FieldNameMod("id", "displayName", "enabled", "file")
  val display = FieldNameMod("Id", "Name", "Enabled", "Location")
}
