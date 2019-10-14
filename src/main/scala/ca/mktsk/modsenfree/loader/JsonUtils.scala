package ca.mktsk.modsenfree.loader

import java.io.File

import ca.mktsk.modsenfree.exceptions.MissingModDefinitionException
import ca.mktsk.modsenfree.mod.Mod
import ujson.Value

import scala.util.{Failure, Try}

object FileIO {
  def getFileContent(filename: String): Try[String] = {
    ???
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
      searchDirectory.listFiles.filter(_.isFile).find(_.getName == "mod.json")
        .getOrElse(new MissingModDefinitionException())
    }
  }

  def writeMod(mod: Mod): Try[Mod] = {
    ???
  }
}

object JsonUtils {
  def readMods(): List[Mod] = {
    val str = ""
    val json = ujson.read(str)
    json.arrOpt match {
      case None => List.empty
      case Some(arr) =>
        arr.ma
    }

  }
}
