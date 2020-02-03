package ca.mktsk.modsenfree.io

import ca.mktsk.modsenfree.utils.Constants

import scala.sys.process.Process
import scala.util.Try

object Interop {
  def isPatched(patcherExecutable: String, dllToPatch: String): Try[Boolean] = Try {
    false
  }

  def patcher(command: String): Try[String] = Try {
    Process(Seq(
      Constants.patcherExecutable,
      command,
      Constants.gameAssembly,
      Constants.gameClassToPatch,
      Constants.gameMethodToPatch,
      Constants.patchAssembly,
      Constants.patchClass,
      Constants.patchMethod,
      Constants.patchDependencyResolver
    )).!!.trim
  }

  def responseMessage(response: String): PatcherMessage.Value = PatcherMessage.withName(response.trim)

  def patch(): Try[String] = patcher(Constants.patchCommand)

  def unpatch(): Try[String] = patcher(Constants.unpatchCommand)

  def patchJob(isPatched: Boolean): Try[String] = if (isPatched) unpatch() else patch()
}
