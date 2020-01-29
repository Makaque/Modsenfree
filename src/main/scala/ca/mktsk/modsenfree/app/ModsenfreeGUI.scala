package ca.mktsk.modsenfree.app

import javafx.concurrent.Task
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label}


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

  class PatchButtonTask(patchButton: Button) extends Task[Unit]{
    override def call(): Unit = {

    }

  }

  def patchButtonClicked(e: ActionEvent): Unit = {
    println("Patched")
    val bla = new Task[Void]() {
      override def call(): Void = {

        return null;
      }
    }
  }

}