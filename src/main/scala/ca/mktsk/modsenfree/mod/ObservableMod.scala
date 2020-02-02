package ca.mktsk.modsenfree.mod

import java.io.File
import java.lang.{Boolean => Jbool}

import ca.mktsk.modsenfree.utils.StringUtils
import javafx.beans.property.{BooleanProperty, SimpleBooleanProperty, SimpleStringProperty, StringProperty}


case class ObservableMod(id: String, name: StringProperty, enabled: BooleanProperty, file: File)


object ObservableMod {
  def asMod(observableMod: ObservableMod): Mod = {
    Mod(observableMod.id, observableMod.name.getValue, observableMod.enabled.getValue, observableMod.file)
  }

  def fromMod(mod: Mod): ObservableMod = {
    val id = mod.id
    val name = new SimpleStringProperty(mod.displayName)
    val enabled = new SimpleBooleanProperty(mod.enabled.asInstanceOf[Jbool], StringUtils.titleWordCap(Mod.field.enabled), mod.enabled.asInstanceOf[Jbool])
    val file = mod.file
    ObservableMod(id, name, enabled, file)
  }
}