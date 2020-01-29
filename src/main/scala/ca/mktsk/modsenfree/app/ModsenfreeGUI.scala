package ca.mktsk.modsenfree.app

//import javafx.concurrent.Task
import ca.mktsk.modsenfree.utils.Task
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class ModsenfreeGUI {

  @FXML
  var patchButton: Button = new Button()

  @FXML
  var messagePanelLabel: Label = new Label()

  @FXML
  def initialize(): Unit = {
    patchButton.setText("Patch")
    messagePanelLabel.setText("Ready")
  }

//  class PatchButtonTask(patchButton: Button) extends Task[Unit]{
//    override def call(): Unit = {
//
//    }
//
//  }

  def patchButtonClicked(e: ActionEvent): Unit = {
    println("Patched")
    val t = Task{
      Thread.sleep(1000)
      println("running")
      "Hello"
    }.onSuccess((e,s) => println(s))
    println("starting")
    Future(t.call())
  }

}