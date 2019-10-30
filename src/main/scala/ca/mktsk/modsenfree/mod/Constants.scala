package ca.mktsk.modsenfree.mod

object Constants {
  val modSearchDirectory = "./mods"
  val modDefinitionFilename = "mod.json"
  val title = "Modsenfree Mod Loader"
  val patcherExecutable = "./src/main/cs/Patcher.exe"

  val patchCommand = "PATCH"
  val unpatchCommand = "UNPATCH"
  val isPatchedCommand = "IS_PATCHED"

  val gameAssembly = "./Assembly-CSharp.dll"
  val gameClassToPatch = "MainCanvas"
  val gameMethodToPatch = "Awake"

  val patchAssembly = "./Hook.dll"
  val patchClass = "Hook"
  val patchMethod = "hookToInject"
}
