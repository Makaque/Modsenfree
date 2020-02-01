package ca.mktsk.modsenfree.mod

import java.io.File

import ca.mktsk.modsenfree.utils.StringUtils
import scalafx.beans.property.{BooleanProperty, StringProperty}
import java.lang.{Boolean => Jbool}



case class ObservableMod(id: String, name: StringProperty, enabled: BooleanProperty, file: File)


object ObservableMod {
  def asMod(observableMod: ObservableMod): Mod = {
    Mod(observableMod.id, observableMod.name.value, observableMod.enabled.value, observableMod.file)
  }

  def fromMod(mod: Mod): ObservableMod = {
    val id = mod.id
    val name = StringProperty(mod.displayName)
    val enabled = new BooleanProperty(mod.enabled.asInstanceOf[Jbool], StringUtils.titleWordCap(Mod.field.enabled), mod.enabled.asInstanceOf[Jbool])
    val file = mod.file
    ObservableMod(id, name, enabled, file)
  }
}