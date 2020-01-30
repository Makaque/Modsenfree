package ca.mktsk.modsenfree.mod

object Constants {
  val modSearchDirectory = "./mods"
  val modDefinitionFilename = "mod.json"
  val title = "Modsenfree Mod Loader"
  val patcherExecutable = "./src/main/cs/Patcher.exe"

  val patchCommand = "PATCH"
  val unpatchCommand = "UNPATCH"
  val isPatchedCommand = "IS_PATCHED"

  val gameAssembly = "./resources/Assembly-CSharp.dll"
  val gameClassToPatch = "MainCanvas"
  val gameMethodToPatch = "Awake"

  val patchAssembly = "./src/main/cs/Hook.dll"
  val patchClass = "HookNamespace.Hook"
  val patchMethod = "hookToInject"
  val patchDependencyResolver = "./resources/"

  val patchButtonPatchText = "Patch"
  val patchButtonUnpatchText = "Unpatch"
  val patchButtonFailPatchCheckText = "Can't Patch"
  val patchButtonPatchingText = "Patching"
  val patchButtonUnpatchingText = "Unpatching"
  val patchButtonBusyText = "Working"
}
