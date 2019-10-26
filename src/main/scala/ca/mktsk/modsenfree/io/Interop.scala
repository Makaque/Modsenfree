package ca.mktsk.modsenfree.io

import ca.mktsk.modsenfree.mod.Constants

import scala.sys.process.Process
import scala.util.Try

object Interop {
  def isPatched(patcherExecutable: String, dllToPatch: String): Try[Boolean] = Try {
    //    throw new Exception
    true
  }

  def patcher(command: String): Try[String] = Try {
    Thread.sleep(3000)
    Process(s"${Constants.patcherExecutable} $command ${Constants.gameAssembly}").!!.trim
  }

  def patch: Try[String] = patcher(Constants.patchCommand)

  def unpatch: Try[String] = patcher(Constants.unpatchCommand)
}
