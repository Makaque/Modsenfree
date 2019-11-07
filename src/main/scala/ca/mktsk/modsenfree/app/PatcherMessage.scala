package ca.mktsk.modsenfree.app


object PatcherMessage extends Enumeration {
  type Message = Value
  val PATCH_SUCCESS,
  UNPATCH_SUCCESS,
  IS_PATCHED_TRUE,
  IS_PATCHED_FALSE,
  ERROR,
  MISSING_ASSEMBLY_ERROR,
  REPEAT_OPERATION_ERROR,
  INVALID_COMMAND_ERROR,
  TOO_FEW_ARGUMENTS_ERROR,
  UNIMPLEMENTED_ERROR = Value
}
