package ca.mktsk.modsenfree.io

import ca.mktsk.modsenfree.exceptions.PatchedCheckException
import ca.mktsk.modsenfree.utils.{Constants, Settings, StringUtils}

import scala.sys.process.Process
import scala.util.Try

object Interop {
  def isPatched(settings: Settings)(): Try[Boolean] = patcher(settings)(settings.isPatchedCommand)
    .map(response => {
      println("response " + response)
      PatcherMessage.withName(response) match {
        case PatcherMessage.IS_PATCHED_TRUE => true
        case PatcherMessage.IS_PATCHED_FALSE => false
        case _ => throw PatchedCheckException("Failed check if patch is present")
      }
    })

  def patcher(settings: Settings)(command: String): Try[String] = Try {
    val cmd = Seq(
              settings.patcherExecutable,
      /*[0]*/ command,
      /*[1]*/ settings.gameAssemblyRead,
      /*[2]*/ settings.gameAssemblyWrite,
      /*[3]*/ settings.gameClassToPatch,
      /*[4]*/ settings.gameMethodToPatch,
      /*[5]*/ settings.patchAssembly,
      /*[6]*/ settings.patchClass,
      /*[7]*/ settings.patchMethod,
      /*[8]*/ settings.patchDependencyResolver
    ).map(StringUtils.quote)
    println("cmd: " + cmd)
    Process(cmd).!!.trim
  }

  def responseMessage(response: String): PatcherMessage.Value = PatcherMessage.withName(response.trim)

  def patch(settings: Settings)(): Try[String] = patcher(settings)(settings.patchCommand)

  def unpatch(settings: Settings)(): Try[String] = patcher(settings)(settings.unpatchCommand)

  def patchJob(settings: Settings)(isPatched: Boolean): Try[String] = {
    if (isPatched) unpatch(settings)() else patch(settings)()
  }
}
