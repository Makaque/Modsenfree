package ca.mktsk.modsenfree.exceptions

case class NotDirectoryException (msg: String = "", cause: Throwable = None.orNull) extends Exception
