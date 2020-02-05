package ca.mktsk.modsenfree.io

import ca.mktsk.modsenfree.utils.{Constants, Settings}

import scala.sys.process.Process
import scala.util.Try

object Interop {
  def isPatched(patcherExecutable: String, dllToPatch: String): Try[Boolean] = Try {
    false
  }

  def patcher(command: String): Try[String] = Try {
    Process(Seq(
      Settings.get.patcherExecutable,
      command,
      Settings.get.gameAssembly,
      Settings.get.gameClassToPatch,
      Settings.get.gameMethodToPatch,
      Settings.get.patchAssembly,
      Settings.get.patchClass,
      Settings.get.patchMethod,
      Settings.get.patchDependencyResolver
    )).!!.trim
  }

  def responseMessage(response: String): PatcherMessage.Value = PatcherMessage.withName(response.trim)

  def patch(): Try[String] = patcher(Settings.get.patchCommand)

  def unpatch(): Try[String] = patcher(Settings.get.unpatchCommand)

  def patchJob(isPatched: Boolean): Try[String] = if (isPatched) unpatch() else patch()
}
