package ca.mktsk.modsenfree.utils

import java.io.File

import ca.mktsk.modsenfree.mod.Mod
import ujson.Value

import scala.util.Try

object JsonUtils {

  def modToJson(mod: Mod): Value = {
    ujson.Obj(
      Mod.field.id -> ujson.Str(mod.id),
      Mod.field.displayName -> ujson.Str(mod.displayName),
      Mod.field.assemblyName -> ujson.Str(mod.assemblyName),
      Mod.field.enabled -> ujson.Bool(mod.enabled)
    )
  }

  def jsonToMod(json: Value, file: File): Try[Mod] = {
    Try {
      System.out.println(json)
      System.out.println(json.obj)
      System.out.println(json.obj.get(Mod.field.displayName))
      System.out.println(json.obj.keySet)
      val id = json.obj.get(Mod.field.id).get.str
      val name = json.obj.get(Mod.field.displayName).get.str
      val assembly = json.obj.get(Mod.field.assemblyName).get.str
      val enabled = json.obj.get(Mod.field.enabled).getOrElse(ujson.Bool(false)).bool
      Mod(id, name, assembly, enabled, file)
    }
  }

  def jsonStringToMod(jsonString: String, file: File): Try[Mod] =
    Try {
      ujson.read(jsonString)
    }.flatMap(v => {
      jsonToMod(v, file)
    })

}
