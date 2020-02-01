package ca.mktsk.modsenfree.app

//import javafx.concurrent.Task

import java.io.File

import ca.mktsk.modsenfree.exceptions.{Exceptions, NotDirectoryException}
import ca.mktsk.modsenfree.io.{FileIO, Interop, PatcherMessage}
import ca.mktsk.modsenfree.mod.ObservableMod
import ca.mktsk.modsenfree.utils.{Constants, JsonUtils, Task}
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.cell.CheckBoxTableCell
import javafx.scene.control.{Button, Label, MenuItem, TableColumn, TableView}
import scalafx.collections.ObservableBuffer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ModsenfreeGUI {

  @FXML
  var patchButton = new Button

  @FXML
  var messagePanelLabel = new Label

  @FXML
  var refreshMenuItem = new MenuItem

  @FXML
  var modTable = new TableView[ObservableMod]

  @FXML
  var modEnabledColumn = new TableColumn[ObservableMod, java.lang.Boolean]

  @FXML
  var modNameColumn = new TableColumn[ObservableMod, String]


  @FXML
  def initialize(): Unit = {
    //    patchButton.setText("Patch")
    refreshMessagePanelLabel()
    refreshPatchButton()
    loadModTable()
  }

  def modChanged(oMod: ObservableMod): Unit = {
    println("Changed" + oMod.name)
    val tryWrite = FileIO.writeMod(ObservableMod.asMod(oMod))
    if (tryWrite.isFailure) {
      UIComponents.errorAlert("Couldn't save " + oMod.name)
    }
  }

  private def guessPatchedStatus(button: Button): Boolean =
    button.getText != Constants.patchButtonPatchText

  private def patchButtonBusyText(isPatched: Boolean): String =
  //    if (isPatched) Constants.patchButtonUnpatchingText else Constants.patchButtonPatchingText
    Constants.patchButtonBusyText

  private def patchButtonText(isPatched: Boolean): String =
    if (isPatched) Constants.patchButtonUnpatchText else Constants.patchButtonPatchText

  private def patchButtonJob(isPatched: Boolean) = Interop.patch()

  private def isPatched = Interop.isPatched(Constants.patcherExecutable, Constants.gameAssembly)

  def refreshPatchButton(): Unit = {
    val refreshJob = Task {
      isPatched.get
    }
      .onSuccess((e, p) => patchButton.setText(patchButtonText(p)))
      .onFailed((e, t) => {
        patchButton.setText((Constants.patchButtonFailPatchCheckText))
        patchButton.setDisable(true)
        messagePanelLabel.setText(Constants.patchButtonFailPatchCheckText)
      })
    Future(refreshJob.run())
  }

  def refreshMessagePanelLabel(): Unit = {
    messagePanelLabel.setText("Ready")
  }

  def errorFilesMessage(list: List[(File, Throwable)]): String = {
    list.map(_._1).foldRight("") { case (file, acc) => acc + file.getCanonicalPath + System.lineSeparator() }
  }

  def reportFailedModLoads(
                            definitionFiles: List[(File, Throwable)],
                            readFiles: List[(File, Throwable)],
                            parseFiles: List[(File, Throwable)]
                          ): Unit = {
    if (definitionFiles.nonEmpty) {
      val msg = "These directories in the mods directory do not have a " + Constants.modDefinitionFilename + " file:" + System.lineSeparator() +
        errorFilesMessage(definitionFiles)
      UIComponents.errorAlert(msg)
    }
    if (readFiles.nonEmpty) {
      val msg = "Could not read " + Constants.modDefinitionFilename + " file from the following mods:" + System.lineSeparator() +
        errorFilesMessage(readFiles)
      UIComponents.errorAlert(msg)
    }
    if (parseFiles.nonEmpty) {
      val msg = "The " + Constants.modDefinitionFilename + " file for these mods could not be parsed:" + System.lineSeparator() +
        errorFilesMessage(parseFiles)
      UIComponents.errorAlert(msg)
    }
  }

  def loadModTable(): Unit = {
    val loadTableJob = Task {

      val modDirectories = FileIO.getSubdirectories(new File(Constants.modSearchDirectory))
        .getOrElse {
          throw NotDirectoryException("Could not find mod directory")
        }

      val attemptModDefinitionFiles = modDirectories
        .map(modDir => (modDir, FileIO.getModDefinitionFile(modDir, Constants.modDefinitionFilename)))

      val (failedModDefinitionFiles, modDefinitionFiles) = Exceptions.splitWithCause(attemptModDefinitionFiles)

      val (failedModRead, modStrings) = Exceptions.splitWithCause(modDefinitionFiles.map(file =>
        (file, FileIO.getFileContent(file).map(s => (s, file))
        )))

      val (failedModParse, mods) = Exceptions.splitWithCause(modStrings.map { case (ms, f) => (f, JsonUtils.jsonStringToMod(ms, f)) })

      val observableMods = mods.map(mod => ObservableMod.fromMod(mod))

      val modData: ObservableBuffer[ObservableMod] = ObservableBuffer(observableMods)

      reportFailedModLoads(failedModDefinitionFiles, failedModRead, failedModParse)

      modData

    }
      .onSuccess((e,modData) => {
        modData.foreach(oMod => {
          oMod.enabled.onChange(modChanged(oMod))
        })
        modEnabledColumn.setCellValueFactory(cell => cell.getValue.enabled)
        modEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(modEnabledColumn))
        modEnabledColumn.setEditable(true)
        modNameColumn.setCellValueFactory(cell => cell.getValue.name)
        modTable.setItems(modData)
      })
      .onFailed((e, t) => messagePanelLabel.setText(t.getMessage))

    Future(loadTableJob.run())
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

  def refreshViewClicked(e: ActionEvent): Unit = {
    refreshMessagePanelLabel()
    refreshPatchButton()
    loadModTable()
    println("refreshed")
  }


}