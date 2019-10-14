package ca.mktsk.modsenfree.loader

import java.io.{File, PrintWriter}

import ca.mktsk.modsenfree.exceptions.MissingModDefinitionException
import ca.mktsk.modsenfree.mod.Mod
import ujson.Value

import scala.io.Source
import scala.util.{Failure, Try}

object FileIO {
  def getFileContent(file: File): Try[String] = {
    Try {
      Source.fromFile(file).mkString
    }
  }

  def getSubdirectories(searchDirectory: File): Try[List[File]] = {
    if (!searchDirectory.isDirectory) Failure(new Exception(s"${searchDirectory} is not a directory"))
    else Try {
      searchDirectory.listFiles().filter(_.isDirectory).toList
    }
  }

  def getModDefinitionFile(searchDirectory: File, definitionFilename: String): Try[File] = {
    if (!searchDirectory.isDirectory) Failure(new Exception(s"${searchDirectory} is not a directory"))
    else Try {
      searchDirectory.listFiles.filter(_.isFile).find(_.getName == definitionFilename)
        .getOrElse(new MissingModDefinitionException())
    }
  }

  def writeMod(mod: Mod): Try[Mod] = {
    Try {
      val writer = new PrintWriter(mod.file)
      writer.write(JsonUtils.modToJson(mod).toString())
      mod
    }
  }
}

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
      val name = json.obj.get("name").str
      val enabled = json.obj.get("enabled").bool
      val file = new File(json.obj.get("file").str)
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
