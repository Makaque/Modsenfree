package ca.mktsk.modsenfree.utils

import java.io.File

object StringUtils {

  def titleWordCap(title: String): String = {
    if (title.isEmpty) ""
    else title.take(1).toUpperCase() + title.drop(1).toLowerCase()
  }

  def titleCap(title: String): String = {
    title.split(" ").map(titleWordCap).mkString(" ")
  }

  def toUnixSeparatedPath(path: String): String = {
    path.replaceAllLiterally(File.separator, "/")
  }

}
