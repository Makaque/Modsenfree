package ca.mktsk.modsenfree.exceptions

case class MissingModDefinitionException(msg: String = "", cause: Throwable = None.orNull) extends Exception
