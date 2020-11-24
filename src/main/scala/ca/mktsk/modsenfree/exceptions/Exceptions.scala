package ca.mktsk.modsenfree.exceptions

import scala.util.{Failure, Success, Try}

object Exceptions {

  def filterFailures[U](list: List[Try[U]]): List[Throwable] = {
    list.collect { case Failure(t) => t }
  }

  def filterFailuresWithCause[U, V](list: List[(V, Try[U])]): List[(V, Throwable)] = {
    list.collect { case (v, Failure(t)) => (v, t) }
  }

  def filterSuccesses[U](list: List[Try[U]]): List[U] = {
    list.collect { case Success(u) => u }
  }

  def split[U](list: List[Try[U]]): (List[Throwable], List[U]) = {
    (filterFailures(list), filterSuccesses(list))
  }

  def splitWithCause[U, V](list: List[(V, Try[U])]): (List[(V, Throwable)], List[U]) = {
    (filterFailuresWithCause(list), filterSuccesses(list.map(_._2)))
  }
}
