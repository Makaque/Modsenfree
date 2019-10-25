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
    Process(Seq(Constants.patcherExecutable, command)).!!.trim
  }

  def patch: Try[String] = Try {
    Thread.sleep(3000)
    Process(s"${Constants.patcherExecutable} ${Constants.patchCommand}").!!.trim
  }

  def unpatch: Try[String] = Try {
    Thread.sleep(3000)
    Process("./src/main/cs/TestSharp.exe bla").!!.trim
  }
}
