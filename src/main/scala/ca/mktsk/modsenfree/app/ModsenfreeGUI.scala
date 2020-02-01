package ca.mktsk.modsenfree.app

//import javafx.concurrent.Task

import ca.mktsk.modsenfree.io.{Interop, PatcherMessage}
import ca.mktsk.modsenfree.mod.Constants
import ca.mktsk.modsenfree.utils.Task
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, MenuItem}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class ModsenfreeGUI {

  @FXML
  var patchButton: Button = new Button()

  @FXML
  var messagePanelLabel: Label = new Label()

  @FXML
  var refreshMenuItem: MenuItem = new MenuItem()

  @FXML
  def initialize(): Unit = {
//    patchButton.setText("Patch")
    refreshMessagePanelLabel()
    refreshPatchButton()
  }

  private def guessPatchedStatus(button: Button): Boolean =
    button.getText != Constants.patchButtonPatchText

  private def patchButtonBusyText(isPatched: Boolean): String =
  //    if (isPatched) Constants.patchButtonUnpatchingText else Constants.patchButtonPatchingText
    Constants.patchButtonBusyText

  private def patchButtonText(isPatched: Boolean): String =
    if (isPatched) Constants.patchButtonUnpatchText else Constants.patchButtonPatchText

  private def patchButtonJob(isPatched: Boolean) = Interop.patch
  private def isPatched = Interop.isPatched(Constants.patcherExecutable, Constants.gameAssembly)

  def refreshPatchButton(): Unit = {
    val refreshJob = Task{
      isPatched.get
    }
      .onSuccess((e,p) => patchButton.setText(patchButtonText(p)))
      .onFailed((e,t) => {
        patchButton.setText((Constants.patchButtonFailPatchCheckText))
        patchButton.setDisable(true)
        messagePanelLabel.setText(Constants.patchButtonFailPatchCheckText)
      })
    Future(refreshJob.run())
  }

  def refreshMessagePanelLabel(): Unit = {
    messagePanelLabel.setText("Ready")
  }

  def patchButtonClicked(e: ActionEvent): Unit = {
    val isPatchedGuess = guessPatchedStatus(patchButton)


    val patchJob = new Task[Boolean] {

      override def call(): Boolean = {
        val patched = isPatched.get
        if (patched != isPatchedGuess) {
          throw new RuntimeException("Tried to patch/unpatch when already done")
        } else {
          Thread.sleep(5000)
          Interop.patchJob(patched)
            .map(result => PatcherMessage.withName(result))
            .map(patchMessage => updateMessage(patchMessage.toString))
          isPatched.getOrElse(!patched)
        }
      }
    }
      .onScheduled { e =>
        patchButton.setDisable(true)
        patchButton.setText(patchButtonBusyText(isPatchedGuess))
      }
      .onSuccess((e, patchStatus) => {
        patchButton.setDisable(false)
        patchButton.setText(patchButtonText(patchStatus))
      })
      .onFailed((e, t) => {
        patchButton.setDisable(false)
        patchButton.setText(patchButtonText(isPatchedGuess))
        messagePanelLabel.setText(t.getMessage)
        println("Couldn't handle patch")
      })

    Future(patchJob.run())
  }

  def refreshViewClicked(e: ActionEvent): Unit ={
    refreshMessagePanelLabel()
    refreshPatchButton()
    println("refreshed")
  }

}