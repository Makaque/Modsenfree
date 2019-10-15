package ca.mktsk.modsenfree.utils

object StringUtils {

  def titleWordCap(title: String): String = {
    if(title.isEmpty) ""
    else title.take(1).toUpperCase() + title.drop(0).toLowerCase()
  }

}
