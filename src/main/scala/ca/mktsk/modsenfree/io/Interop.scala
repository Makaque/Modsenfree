package ca.mktsk.modsenfree.io

import ca.mktsk.modsenfree.utils.{Constants, Settings}

import scala.sys.process.Process
import scala.util.Try

object Interop {
  def isPatched(patcherExecutable: String, dllToPatch: String): Try[Boolean] = Try {
    false
  }

  def patcher(settings: Settings)(command: String): Try[String] = Try {
    Process(Seq(
      settings.patcherExecutable,
      command,
      settings.gameAssembly,
      settings.gameClassToPatch,
      settings.gameMethodToPatch,
      settings.patchAssembly,
      settings.patchClass,
      settings.patchMethod,
      settings.patchDependencyResolver
    )).!!.trim
  }

  def responseMessage(response: String): PatcherMessage.Value = PatcherMessage.withName(response.trim)

  def patch(settings: Settings)(): Try[String] = patcher(settings)(settings.patchCommand)

  def unpatch(settings: Settings)(): Try[String] = patcher(settings)(settings.unpatchCommand)

  def patchJob(settings: Settings)(isPatched: Boolean): Try[String] = {
    if (isPatched) unpatch(settings)() else patch(settings)()
  }
}
