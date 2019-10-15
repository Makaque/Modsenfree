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
      "name" -> ujson.Str(mod.name),
      "enabled" -> ujson.Bool(mod.enabled)
    )
  }

  def jsonToMod(json: Value, file: File): Try[Mod] = {
    Try {
      System.out.println(json)
      System.out.println(json.obj)
      System.out.println(json.obj.get("name"))
      System.out.println(json.obj.keySet)
      val name = json.obj.get("name").get.str
      val enabled = json.obj.get("enabled").get.bool
//      val file = new File(json.obj.get("file").str)
      Mod(name, enabled, file)
    }
  }

  def jsonStringToMod(jsonString: String, file: File): Try[Mod] =
    Try {
      ujson.read(jsonString)
    }.flatMap(v => {
      jsonToMod(v, file)
    })

}
