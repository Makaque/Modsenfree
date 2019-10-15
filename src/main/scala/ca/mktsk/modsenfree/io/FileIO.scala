package ca.mktsk.modsenfree.io

import java.io.{File, PrintWriter}

import ca.mktsk.modsenfree.exceptions.{MissingModDefinitionException, NotDirectoryException}
import ca.mktsk.modsenfree.mod.Mod
import ca.mktsk.modsenfree.utils.JsonUtils

import scala.io.Source
import scala.util.{Failure, Try}

object FileIO {
  def getFileContent(file: File): Try[String] = {
    Try {
      Source.fromFile(file).mkString
    }
  }

  def getSubdirectories(searchDirectory: File): Try[List[File]] = {
    if (!searchDirectory.isDirectory) Failure(NotDirectoryException(s"$searchDirectory is not a directory"))
    else Try {
      System.out.println("Search Directory")
      System.out.println(searchDirectory.getAbsolutePath)
      System.out.println(searchDirectory.listFiles)
      searchDirectory.listFiles().filter(_.isDirectory).toList
    }
  }

  def getModDefinitionFile(searchDirectory: File, definitionFilename: String): Try[File] = {
    if (!searchDirectory.isDirectory) Failure(NotDirectoryException(s"$searchDirectory is not a directory"))
    else Try {
      searchDirectory.listFiles.filter(_.isFile).find(_.getName == definitionFilename)
        .getOrElse(throw MissingModDefinitionException())
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