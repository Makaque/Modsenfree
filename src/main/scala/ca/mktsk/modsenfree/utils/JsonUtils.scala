package ca.mktsk.modsenfree.utils

import java.io.File

import ca.mktsk.modsenfree.mod.Mod
import ujson.Value

import scala.util.Try

object JsonUtils {
  //  def readMods(): List[Mod] = {
  //    val str = ""
  //    val json = ujson.read(str)
  //    json.arrOpt match {
  //      case None => List.empty
  //      case Some(arr) =>
  //        arr.ma
  //    }
  //
  //  }

  def modToJson(mod: Mod): Value = {
    ujson.Obj(
      Mod.field.id -> ujson.Str(mod.id),
      Mod.field.displayName -> ujson.Str(mod.displayName),
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
      val enabled = json.obj.get(Mod.field.enabled).get.bool
//      val file = new File(json.obj.get("file").str)
      Mod(id, name, enabled, file)
    }
  }

  def jsonStringToMod(jsonString: String, file: File): Try[Mod] =
    Try {
      ujson.read(jsonString)
    }.flatMap(v => {
      jsonToMod(v, file)
    })

}
