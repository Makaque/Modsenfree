package ca.mktsk.modsenfree.exceptions

case class PatchedCheckException(msg: String = "", cause: Throwable = None.orNull) extends Exception
