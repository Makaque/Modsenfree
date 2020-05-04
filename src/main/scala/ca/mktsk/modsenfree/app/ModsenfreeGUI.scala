package ca.mktsk.modsenfree.app


import java.io.File
import java.nio.file.Files

import ca.mktsk.modsenfree.exceptions.{Exceptions, NotDirectoryException, SettingsLoadException}
import ca.mktsk.modsenfree.io.{FileIO, Interop, PatcherMessage}
import ca.mktsk.modsenfree.mod.ObservableMod
import ca.mktsk.modsenfree.utils._
import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.control.cell.CheckBoxTableCell
import javafx.scene.layout.BorderPane
import javafx.stage.DirectoryChooser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.util.Try


class ModsenfreeGUI {

  @FXML
  var rootPane = new BorderPane

  @FXML
  var patchButton = new Button

  @FXML
  var messagePanelLabel = new Label

  @FXML
  var gameFolderMenuItem = new MenuItem

  @FXML
  var installModMenuItem = new MenuItem

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

    def recoverError: PartialFunction[Throwable, Unit] = {
      case settingsLoadError: SettingsLoadException =>
        Platform.runLater(() => errorAlert(Constants.settingsLoadFailMessage))
      case t: Throwable =>
        Platform.runLater(() => errorAlert("Unexpected error. Failed to load application"))
    }

    val trySettings = FileIO.getFileContent(new File(Constants.settingsFileLocation))
      .map(settingsContents => {
        //        println(settingsContents)
        settings = Settings(settingsContents)
      })
      .recover {
        case t: Throwable => throw new SettingsLoadException
      }

    trySettings
      .map(_ => {
        refreshMessagePanelLabel()
      })
      .recover(recoverError)

    trySettings
      .map(_ => {
        refreshPatchButton()
      })
      .recover(recoverError)

    trySettings
      .map(_ => {
        loadModTable()
      })
      .recover(recoverError)
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

  private def isPatched = {
    val p = Interop.isPatched(settings)()
    println("isPatched " + p)
    p
  }

  def refreshPatchButton(): Unit = {
    val refreshJob = Task {
      isPatched.get
    }
      .onSuccess((e, p) => {
        patchButton.setText(patchButtonText(p))
        patchButton.setDisable(false)
      })
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

      modTable.getItems.clear()
      println("clearing mod table")
      println(modTable.getItems)

      val tryModDirectories = FileIO.getSubdirectories(new File(settings.modSearchDirectory))

      if (tryModDirectories.isFailure) {
        throw tryModDirectories.failed.get
      }

      val modDirectories = tryModDirectories
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
          oMod.enabled.addListener(_ => modChanged(oMod)) // Shows error in IDE but compiles anyway
        })
        modEnabledColumn.setCellValueFactory(cell => cell.getValue.enabled)
        modEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(modEnabledColumn))
        modEnabledColumn.setEditable(true)
        modNameColumn.setCellValueFactory(cell => cell.getValue.name)
        modTable.getItems.addAll(modData)
        println("set mod table")
        println(modTable.getItems)
      })
      .onFailed((e, t) => {
        messagePanelLabel.setText(t.getMessage)
        Platform.runLater(() => {
          println(t.getMessage)
          t.printStackTrace()
        })
        //        throw new Exception(t.getMessage)
      })

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

  def gameFolderMenuItemClicked(e: ActionEvent): Unit = {
    val directoryChooser = new DirectoryChooser
    directoryChooser.setInitialDirectory(new File(settings.gameInstallLocation))
    Option[File](directoryChooser.showDialog(rootPane.getScene.getWindow))
      .foreach(gameFolder => {
        val newSettings = settings
          .set("gameInstallLocation", StringUtils.toUnixSeparatedPath(gameFolder.getAbsolutePath))
          .set("gameAssemblyRead", StringUtils.toUnixSeparatedPath(gameFolder.getAbsolutePath + File.separator + settings.gameAssemblyFromInstall))
          .set("gameAssemblyWrite", StringUtils.toUnixSeparatedPath(gameFolder.getAbsolutePath + File.separator + settings.gameAssemblyFromInstall))
          .set("modSearchDirectory", StringUtils.toUnixSeparatedPath(gameFolder.getAbsolutePath + File.separator + settings.modSearchDirectoryFromInstall))
        Try {
          Settings.save(Constants.settingsFileLocation)(newSettings)
        }.recover {
          case throwable: Throwable => errorAlert("Failed to save settings changes")
        }.map(_ => initialize())
        println("abs path: " + gameFolder.getAbsolutePath)
      })
  }

  def copyFile(file: File, to: File): File = {
    Files.copy(file.toPath, to.toPath).toFile
  }

  def copyDir(dir: File, to: File): Unit = {
    copyFile(dir, to)
    dir.listFiles().foreach(f => {
      val dest = new File(to.getAbsolutePath + File.separator + f.getName)
      if (!dest.exists()) { // This is not ideal. Figure out your logic
        if (f.isDirectory) {
          println("file: " + f.getAbsolutePath)
          println("to: " + dest)
          copyDir(f, dest)
        } else {
          println("file: " + f.getAbsolutePath)
          println("to: " + to.getAbsolutePath)
          copyFile(f, dest)
        }
      }
    })
  }

  def installMod(modFolder: File): Unit = {
    // Check if mod exists in mod directory
    // Copy contents to mod directory
    // modSearchDirectory is a bad name for what it is
    val modDirectory = new File(settings.modSearchDirectory)
    val installedModFolder = new File(modDirectory.getAbsolutePath + File.separator + modFolder.getName)
    if (!installedModFolder.exists()) {

      copyDir(modFolder, installedModFolder)
    }
  }

  def installModMenuItemClicked(e: ActionEvent): Unit = {
    println("Install mod")
    val directoryChooser = new DirectoryChooser
    val chosen = Option[File](directoryChooser.showDialog(rootPane.getScene.getWindow))
    if (chosen.nonEmpty) {
      val modDirectory = new File(settings.modSearchDirectory)
      if (!modDirectory.exists()) {
        Files.createDirectory(modDirectory.toPath)
      }
    }
    chosen
      .foreach(modFolder => {
        installMod(modFolder)
      })
    initialize()
  }


}