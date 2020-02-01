package ca.mktsk.modsenfree.utils

import javafx.concurrent.{WorkerStateEvent, Task => jTask}
import javafx.event.EventHandler

object Task {
  def apply[T](callFunc: => T): Task[T] = new Task[T]{
    override def call(): T = callFunc
  }
}

abstract class Task[T] extends jTask[T] {
  private type ActionMethod = EventHandler[WorkerStateEvent] => Unit
  private type ActionEventHandler = EventHandler[WorkerStateEvent]
  private var updMsg: String => Unit = s => {}


//  override def call(): T = callFunc

  private def onAction(actionMethod: ActionMethod)(actionCall: ActionEventHandler): Task[T] = {
    actionMethod(actionCall)
    this
  }

  def onSuccess(successCall: (WorkerStateEvent, => T) => Unit): Task[T] = {
    setOnSucceeded(e => successCall(e, getValue))
    this
  }

  def onRunning(runningCall: ActionEventHandler): Task[T] = onAction(setOnRunning)(runningCall)
  def onCancelled(cancelledCall: ActionEventHandler) : Task[T] = onAction(setOnCancelled)(cancelledCall)
  def onScheduled(scheduledCall: ActionEventHandler): Task[T] = onAction(setOnScheduled)(scheduledCall)
  def onFailed(failedCall: (WorkerStateEvent, => Throwable) => Unit): Task[T] = {
    setOnFailed(e => failedCall(e, getException))
    this
  }


  override def updateMessage(message: String): Unit = {
    super.updateMessage(message)
    updMsg(message)
  }

  def onUpdateMessage(messageCall: String => Unit): Task[T] = {
    updMsg = messageCall
    this
  }
}
