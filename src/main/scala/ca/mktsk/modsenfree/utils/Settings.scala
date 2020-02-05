package ca.mktsk.modsenfree.utils

import java.io.{BufferedReader, StringReader}
import java.util.Properties

import scala.util.Properties
object Settings{
  var get: Settings = _
  def init(settingsContents: String): Unit = {
    get = new Settings(settingsContents)
  }
}

class Settings(settingsContents: String) {
  private val properties: Properties = {
    val props = new Properties()
    props.load(new StringReader(settingsContents))
    props
  }

  val modSearchDirectory: String = properties.getProperty("modSearchDirectory")
  val modDefinitionFilename: String = properties.getProperty("modDefinitionFilename")
  val title: String = properties.getProperty("programTitle")
  val patcherExecutable: String = properties.getProperty("patcherExecutable")

  val patchCommand: String = properties.getProperty("patchCommand")
  val unpatchCommand: String = properties.getProperty("unpatchCommand")
  val isPatchedCommand: String = properties.getProperty("isPatchedCommand")

  val gameAssembly: String = properties.getProperty("gameAssembly")
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
