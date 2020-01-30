package ca.mktsk.modsenfree.app

//import javafx.concurrent.Task

import ca.mktsk.modsenfree.io.Interop
import ca.mktsk.modsenfree.mod.Constants
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

  private def guessPatchedStatus(button: Button): Boolean =
    button.getText != Constants.patchButtonPatchText

  private def patchButtonBusyText(isPatched: Boolean): String =
  //    if (isPatched) Constants.patchButtonUnpatchingText else Constants.patchButtonPatchingText
    Constants.patchButtonBusyText

  private def patchButtonText(isPatched: Boolean): String =
    if (isPatched) Constants.patchButtonUnpatchText else Constants.patchButtonPatchText

  def patchButtonClicked(e: ActionEvent): Unit = {
    val isPatchedGuess = guessPatchedStatus(patchButton)
    def isPatched = Interop.isPatched(Constants.patcherExecutable, Constants.gameAssembly)

    val t = Task {
      Thread.sleep(5000)
      val patched = isPatched
      if (patched.get != isPatchedGuess){
        throw new RuntimeException("Tried to patch/unpatch when already done")
      }

    }
      .onScheduled { e =>
        patchButton.setDisable(true)
        patchButton.setText(patchButtonBusyText(isPatched.get))
      }
      .onSuccess((e, s) => {
        patchButton.setDisable(false)
        patchButton.setText(patchButtonText(isPatched.get))
      })
      .onFailed((e, t) => {
        patchButton.setDisable(false)
        patchButton.setText(patchButtonText(isPatched.get))
        messagePanelLabel.setText(Constants.patchButtonFailPatchCheckText)
        println("Couldn't handle patch")
      })
    Future(t.run())
  }

}