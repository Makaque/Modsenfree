package ca.mktsk.modsenfree.app


import java.io.File

import ca.mktsk.modsenfree.exceptions.{Exceptions, NotDirectoryException, SettingsLoadException}
import ca.mktsk.modsenfree.io.{FileIO, Interop, PatcherMessage}
import ca.mktsk.modsenfree.mod.ObservableMod
import ca.mktsk.modsenfree.utils.{Constants, JsonUtils, Settings, Task}
import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.cell.CheckBoxTableCell
import javafx.scene.control._

import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try


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

  var settings: Settings = _

  def patchJob: Boolean => Try[String] = Interop.patchJob(settings)

  @FXML
  def initialize(): Unit = {
    //    patchButton.setText("Patch")

    FileIO.getFileContent(new File(Constants.settingsFileLocation))
      .map(settingsContents => {
        settings = Settings(settingsContents)
      })
      .recover {
        case t: Throwable => throw new SettingsLoadException
      }
      .map(_ => {
        refreshMessagePanelLabel()
        refreshPatchButton()
        loadModTable()
      })
      .recover({
        case settingsLoadError: SettingsLoadException =>
          Platform.runLater(() => errorAlert(Constants.settingsLoadFailMessage))
        case t: Throwable =>
          Platform.runLater(() => errorAlert("Unexpected error. Failed to load application"))
      })
  }

  def modChanged(oMod: ObservableMod): Unit = {
    println("Changed" + oMod.name)
    val tryWrite = FileIO.writeMod(ObservableMod.asMod(oMod))
    if (tryWrite.isFailure) {
      errorAlert("Couldn't save " + oMod.name)
    }
  }

  private def guessPatchedStatus(button: Button): Boolean =
    button.getText != settings.patchButtonPatchText

  private def patchButtonBusyText(isPatched: Boolean): String =
    settings.patchButtonBusyText

  private def patchButtonText(isPatched: Boolean): String =
    if (isPatched) settings.patchButtonUnpatchText else settings.patchButtonPatchText

  private def isPatched = Interop.isPatched(settings.patcherExecutable, settings.gameAssembly)

  def refreshPatchButton(): Unit = {
    val refreshJob = Task {
      isPatched.get
    }
      .onSuccess((e, p) => patchButton.setText(patchButtonText(p)))
      .onFailed((e, t) => {
        patchButton.setText((settings.patchButtonFailPatchCheckText))
        patchButton.setDisable(true)
        messagePanelLabel.setText(settings.patchButtonFailPatchCheckText)
      })
    Future(refreshJob.run())
  }

  def refreshMessagePanelLabel(): Unit = {
    messagePanelLabel.setText("Ready")
  }

  def errorFilesMessage(list: List[(File, Throwable)]): String = {
    list.map(_._1).foldRight("") { case (file, acc) => acc + file.getCanonicalPath + System.lineSeparator() }
  }

  def errorAlert(message: String): Unit = {
    new Alert(AlertType.ERROR, message, ButtonType.OK).showAndWait()
  }

  def reportFailedModLoads(
                            definitionFiles: List[(File, Throwable)],
                            readFiles: List[(File, Throwable)],
                            parseFiles: List[(File, Throwable)]
                          ): Unit = {
    if (definitionFiles.nonEmpty) {
      val msg = "These directories in the mods directory do not have a " + settings.modDefinitionFilename + " file:" + System.lineSeparator() +
        errorFilesMessage(definitionFiles)
      errorAlert(msg)
    }
    if (readFiles.nonEmpty) {
      val msg = "Could not read " + settings.modDefinitionFilename + " file from the following mods:" + System.lineSeparator() +
        errorFilesMessage(readFiles)
      errorAlert(msg)
    }
    if (parseFiles.nonEmpty) {
      val msg = "The " + settings.modDefinitionFilename + " file for these mods could not be parsed:" + System.lineSeparator() +
        errorFilesMessage(parseFiles)
      errorAlert(msg)
    }
  }

  def loadModTable(): Unit = {
    val loadTableJob = Task {

      val modDirectories = FileIO.getSubdirectories(new File(settings.modSearchDirectory))
        .getOrElse {
          throw NotDirectoryException("Could not find mod directory")
        }

      val attemptModDefinitionFiles = modDirectories
        .map(modDir => (modDir, FileIO.getModDefinitionFile(modDir, settings.modDefinitionFilename)))

      val (failedModDefinitionFiles, modDefinitionFiles) = Exceptions.splitWithCause(attemptModDefinitionFiles)

      val (failedModRead, modStrings) = Exceptions.splitWithCause(modDefinitionFiles.map(file =>
        (file, FileIO.getFileContent(file).map(s => (s, file))
        )))

      val (failedModParse, mods) = Exceptions.splitWithCause(modStrings.map { case (ms, f) => (f, JsonUtils.jsonStringToMod(ms, f)) })

      val observableMods = mods.map(mod => ObservableMod.fromMod(mod))

      Platform.runLater(() => reportFailedModLoads(failedModDefinitionFiles, failedModRead, failedModParse))

      observableMods
    }
      .onSuccess((e, observableMods) => {
        val modData: ObservableList[ObservableMod] = FXCollections.observableList(observableMods.asJava)
        observableMods.foreach(oMod => {
          oMod.enabled.addListener(_ => modChanged(oMod))
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


    val patchTask = new Task[Boolean] {

      override def call(): Boolean = {
        val patched = isPatched.get
        if (patched != isPatchedGuess) {
          throw new RuntimeException("Tried to patch/unpatch when already done")
        } else {
          val patchAttempt = patchJob(patched)
            .map(result => PatcherMessage.withName(result))
            .map(patchMessage => {
              println("patch message " + patchMessage)
              updateMessage(patchMessage.toString)
            })
          if (patchAttempt.isFailure) {
            throw patchAttempt.failed.get
          }
          isPatched.get
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
        t.printStackTrace()
      })
      .onUpdateMessage(msg => {
        println("onUpdateMessage " + msg)
        messagePanelLabel.setText(msg)
      })

    Future(patchTask.run())
  }

  def refreshViewClicked(e: ActionEvent): Unit = {
    initialize()
    println("refreshed")
  }


}