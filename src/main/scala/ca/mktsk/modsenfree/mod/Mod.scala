package ca.mktsk.modsenfree.mod

import java.io.File

case class Mod(id: String, displayName: String, assemblyName: String,  enabled: Boolean, file: File)

object Mod {

  case class FieldNameMod(id: String, displayName: String, assemblyName: String, enabled: String, file: String)

  val field = FieldNameMod("id", "displayName", "assemblyName", "enabled", "file")
  val display = FieldNameMod("Id", "Name", "Assembly Name", "Enabled", "Location")
}
