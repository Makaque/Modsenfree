package ca.mktsk.modsenfree.exceptions

case class SettingsLoadException(msg: String = "", cause: Throwable = None.orNull) extends Exception
