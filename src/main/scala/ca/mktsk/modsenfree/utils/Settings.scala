package ca.mktsk.modsenfree.utils

import java.io.{BufferedReader, FileOutputStream, StringReader}
import java.util.Properties

import scala.util.Properties

object Settings {
  //  var get: Settings = _
  //  def apply(settingsContents: String): Unit = {
  //    get = new Settings(settingsContents)
  //  }
  def apply(settingsContents: String): Settings = {
    val properties: Properties = {
      val props = new Properties()
      props.load(new StringReader(settingsContents))
      props
    }
    new Settings(settingsContents, properties)
  }

  def save(propertiesPath: String)(settings: Settings): Unit = {
    settings.properties.store(new FileOutputStream(propertiesPath), null)
  }
}

class Settings(val settingsContents: String, private val properties: Properties) {
//  private val properties: Properties = {
//    val props = new Properties()
//    props.load(new StringReader(settingsContents))
//    props
//  }

  def set(property: String, value: String): Settings = {
    val updated = new Settings(settingsContents, properties)
    updated.properties.setProperty(property, value)
    updated
  }

  val modSearchDirectory: String = properties.getProperty("modSearchDirectory")
  val modSearchDirectoryFromInstall: String = properties.getProperty("modSearchDirectoryFromInstall")
  val modDefinitionFilename: String = properties.getProperty("modDefinitionFilename")
  val title: String = properties.getProperty("programTitle")
  val patcherExecutable: String = properties.getProperty("patcherExecutable")

  val patchCommand: String = properties.getProperty("patchCommand")
  val unpatchCommand: String = properties.getProperty("unpatchCommand")
  val isPatchedCommand: String = properties.getProperty("isPatchedCommand")

  val gameInstallLocation: String = properties.getProperty("gameInstallLocation")
  val gameAssemblyRead: String = properties.getProperty("gameAssemblyRead")
  val gameAssemblyWrite: String = properties.getProperty("gameAssemblyWrite")
  val gameAssemblyFromInstall: String = properties.getProperty("gameAssemblyFromInstall")
  val gameClassToPatch: String = properties.getProperty("gameClassToPatch")
  val gameMethodToPatch: String = properties.getProperty("gameMethodToPatch")

  val patchAssembly: String = properties.getProperty("patchAssembly")
  val patchClass: String = properties.getProperty("patchClass")
  val patchMethod: String = properties.getProperty("patchMethod")
  val patchDependencyResolver: String = properties.getProperty("patchDependencyResolver")

  val patchButtonPatchText: String = properties.getProperty("patchButtonPatchText")
  val patchButtonUnpatchText: String = properties.getProperty("patchButtonUnpatchText")
  val patchButtonFailPatchCheckText: String = properties.getProperty("patchButtonFailPatchCheckText")
  val patchButtonPatchingText: String = properties.getProperty("patchButtonPatchingText")
  val patchButtonUnpatchingText: String = properties.getProperty("patchButtonUnpatchingText")
  val patchButtonBusyText: String = properties.getProperty("patchButtonBusyText")
}
