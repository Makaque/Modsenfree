package ca.mktsk.modsenfree.utils

import javafx.concurrent.{WorkerStateEvent, Task => jTask}

object Task {
  def apply[T](callFunc: => T): Task[T] = new Task(callFunc)
}

class Task[T](callFunc: => T) extends jTask[T]{
  private var t: Option[T] = None
  override def call(): T = {
    t = Some(callFunc)
    t.get
  }

  def onSuccess(successCall: (WorkerStateEvent, => T) => Unit): Task[T] = {
    println("succeeding")
      setOnSucceeded(e => successCall(e, t.get))

    return this
  }
}
