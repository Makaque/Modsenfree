package ca.mktsk.modsenfree.app


object PatcherMessage extends Enumeration {
  type Message = Value
  val ERROR, PATCH_SUCCESS, RESPONDING = Value
}