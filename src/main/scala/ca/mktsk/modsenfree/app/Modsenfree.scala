package ca.mktsk.modsenfree.app


import java.io.File
import java.lang.{Boolean => Jbool}

import ca.mktsk.modsenfree.exceptions.{Exceptions, NotDirectoryException}
import ca.mktsk.modsenfree.io.{FileIO, Interop, PatcherMessage}
import ca.mktsk.modsenfree.mod.{Constants, Mod}
import ca.mktsk.modsenfree.utils.{JsonUtils, StringUtils}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.{JFXApp, Platform}
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.beans.value.ObservableValue
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.control.cell.CheckBoxTableCell
import scalafx.scene.layout.{BorderPane, Priority, VBox}

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process._
import scala.util.{Failure, Success, Try}


case class ObservableMod(id: String, name: StringProperty, enabled: BooleanProperty, file: File)


object ObservableMod {
  def asMod(observableMod: ObservableMod): Mod = {
    Mod(observableMod.id, observableMod.name.value, observableMod.enabled.value, observableMod.file)
  }

  def fromMod(mod: Mod): ObservableMod = {
    val id = mod.id
    val name = StringProperty(mod.displayName)
    val enabled = new BooleanProperty(mod.enabled.asInstanceOf[Jbool], StringUtils.titleWordCap(Mod.field.enabled), mod.enabled.asInstanceOf[Jbool])
    val file = mod.file
    ObservableMod(id, name, enabled, file)
  }
}

object UIComponents {

  def errorAlert(message: String): Unit = {
    new Alert(AlertType.Error, message, ButtonType.OK).showAndWait()
  }

  def successDialog(message: String): Unit = infoDialog(message)

  def infoDialog(message: String): Unit = {
    new Alert(AlertType.Information, message, ButtonType.OK).showAndWait()
  }

  def patchButton: Button = new Button {
    private val patchText = Constants.patchButtonPatchText
    private val unpatchText = Constants.patchButtonUnpatchText
    private val failPatchCheckText = Constants.patchButtonFailPatchCheckText
    private val patchingText = Constants.patchButtonPatchingText
    private var isPatched = Interop.isPatched(Constants.patcherExecutable, Constants.gameAssembly)

    private def working(): Unit = {
      text = patchingText
      disable = true
    }

    private def finished(): Unit = {
      isPatched = Interop.isPatched(Constants.patcherExecutable, Constants.gameAssembly)
      disable = false
      isPatched match {
        case Success(patched) => text = if (patched) unpatchText else patchText
        case Failure(exception) =>
          text = failPatchCheckText
          disable = true
      }
    }

    finished()

    minWidth = 50
    onMouseClicked = e => {
      Future(Platform.runLater(working()))(ExecutionContext.global)
        .flatMap { _ =>
          Future.fromTry {
            for {
              isP <- isPatched
              response <- if (isP) Interop.unpatch else Interop.patch
            } yield {
              println(response.trim + " equals " + PatcherMessage.PATCH_SUCCESS.toString + ": " + (response.trim.equals(PatcherMessage.PATCH_SUCCESS.toString)))
              println(response.trim.length)
              println(PatcherMessage.PATCH_SUCCESS.toString.length)
              (isP, PatcherMessage.withName(response.trim))
            }
          }
        }(ExecutionContext.global).map { case (isP, patcherMessage) =>
        println("mapping after patch")
        println(patcherMessage)
        patcherMessage match {
          case PatcherMessage.PATCH_SUCCESS => Platform.runLater(UIComponents.successDialog("patcher PATCH_SUCCESS,"))
          case PatcherMessage.UNPATCH_SUCCESS => Platform.runLater(UIComponents.successDialog("patcher UNPATCH_SUCCESS,"))
          case PatcherMessage.IS_PATCHED_TRUE => Platform.runLater(UIComponents.infoDialog("patcher IS_PATCHED_TRUE,"))
          case PatcherMessage.IS_PATCHED_FALSE => Platform.runLater(UIComponents.infoDialog("patcher IS_PATCHED_FALSE,"))
          case PatcherMessage.ERROR => Platform.runLater(UIComponents.errorAlert("patcher ERROR,"))
          case PatcherMessage.MISSING_ASSEMBLY_ERROR => Platform.runLater(UIComponents.errorAlert("patcher MISSING_ASSEMBLY_ERROR,"))
          case PatcherMessage.REPEAT_OPERATION_ERROR => Platform.runLater(UIComponents.errorAlert("patcher REPEAT_OPERATION_ERROR,"))
          case PatcherMessage.INVALID_COMMAND_ERROR => Platform.runLater(UIComponents.errorAlert("patcher INVALID_COMMAND_ERROR,"))
          case PatcherMessage.TOO_FEW_ARGUMENTS_ERROR => Platform.runLater(UIComponents.errorAlert("patcher TOO_FEW_ARGUMENTS_ERROR,"))
          case PatcherMessage.UNIMPLEMENTED_ERROR => Platform.runLater(UIComponents.errorAlert("patcher UNIMPLEMENTED_ERROR"))
          case _ => Platform.runLater(EventHandlers.genericFailure())
        }
      }(ExecutionContext.global)
        .recover { case _: Throwable =>
          println("recovering")
          Platform.runLater(errorAlert("Failed somehow"))
        }(ExecutionContext.global)
        .onComplete { _ =>
          println("onComplete")
          Platform.runLater {
            finished()
          }
        }(ExecutionContext.global)
    }
  }

  def modTableView(modData: ObservableBuffer[ObservableMod], followHeight: Option[ObservableValue[_, Number]] = None): TableView[ObservableMod] = new TableView(modData) {
    editable = true
    columnResizePolicy = TableView.ConstrainedResizePolicy
    followHeight.foreach { h =>
      prefHeight.bind(h)
    }


    private val nameColumn = new TableColumn[ObservableMod, String](Mod.display.displayName) {
      cellValueFactory = cdf => cdf.value.name
    }
    private val enabledColumn: TableColumn[ObservableMod, Jbool] = new TableColumn[ObservableMod, Jbool](Mod.display.enabled) {
      cellValueFactory = _.value.enabled
        .asInstanceOf[ObservableValue[Jbool, Jbool]]
      cellFactory = CheckBoxTableCell.forTableColumn(this)
      editable = true
      minWidth = 100
      maxWidth = 100
    }

    columns ++= Seq(
      enabledColumn,
      nameColumn
    )
  }
}

object EventHandlers {

  def genericSuccess(message: Option[String] = None): Unit = {
    UIComponents.errorAlert("Success!")
  }

  def genericFailure(): Unit = {
    UIComponents.errorAlert("Failed!")
  }

  def modChanged(oMod: ObservableMod): Unit = {
    println("Changed" + oMod.name)
    val tryWrite = FileIO.writeMod(ObservableMod.asMod(oMod))
    Try {
      val result = Process(s"${Constants.patcherExecutable} bla").!!.trim
      PatcherMessage.withName(result) match {
        case PatcherMessage.PATCH_SUCCESS => println("patcher PATCH_SUCCESS,")
        case PatcherMessage.UNPATCH_SUCCESS => println("patcher UNPATCH_SUCCESS,")
        case PatcherMessage.IS_PATCHED_TRUE => println("patcher IS_PATCHED_TRUE,")
        case PatcherMessage.IS_PATCHED_FALSE => println("patcher IS_PATCHED_FALSE,")
        case PatcherMessage.ERROR => println("patcher ERROR,")
        case PatcherMessage.MISSING_ASSEMBLY_ERROR => println("patcher MISSING_ASSEMBLY_ERROR,")
        case PatcherMessage.REPEAT_OPERATION_ERROR => println("patcher REPEAT_OPERATION_ERROR,")
        case PatcherMessage.INVALID_COMMAND_ERROR => println("patcher INVALID_COMMAND_ERROR,")
        case PatcherMessage.TOO_FEW_ARGUMENTS_ERROR => println("patcher TOO_FEW_ARGUMENTS_ERROR,")
        case PatcherMessage.UNIMPLEMENTED_ERROR => println("patcher UNIMPLEMENTED_ERROR")
        case _ => println("patcher didn't respond with responding")
      }
      println(result)
      result
    }.recover { case _: Throwable =>
      println("Couldn't figure out patcher message.")
    }
    if (tryWrite.isFailure) {
      UIComponents.errorAlert("Couldn't save " + oMod.name)
    }
  }
}

// How do I want this thing to function???
// look in mods subdirectory for available mods
// read json file from this application's directory
// mods present in mod folder get added to json
// json keeps track of enabled/disabled (enabled by default)
// mods display as list with checkboxes in javafx window
// button to patch/unpatch game file (cli C# file) test in linux with mono
object Modsenfree extends JFXApp {

  def errorFilesMessage(list: List[(File, Throwable)]): String = {
    list.map(_._1).foldRight("") { case (file, acc) => acc + file.getCanonicalPath + System.lineSeparator() }
  }

  private val modDirectories = FileIO.getSubdirectories(new File(Constants.modSearchDirectory))
    .recover { case _: Throwable => throw NotDirectoryException("Could not find mod directory") }

  private val attemptModDefinitionFiles = modDirectories.getOrElse(List.empty)
    .map(modDir => (modDir, FileIO.getModDefinitionFile(modDir, Constants.modDefinitionFilename)))

  private val (failedModDefinitionFiles, modDefinitionFiles) = Exceptions.splitWithCause(attemptModDefinitionFiles)

  private val (failedModRead, modStrings) = Exceptions.splitWithCause(modDefinitionFiles.map(file =>
    (file, FileIO.getFileContent(file).map(s => (s, file))
    )))

  private val (failedModParse, mods) = Exceptions.splitWithCause(modStrings.map { case (ms, f) => (f, JsonUtils.jsonStringToMod(ms, f)) })

  private val observableMods = mods.map(mod => ObservableMod.fromMod(mod))

  private val modData: ObservableBuffer[ObservableMod] = ObservableBuffer(observableMods)

  stage = new PrimaryStage {
    //    initStyle(StageStyle.Unified)
    title = Constants.title
    scene = new Scene(600, 600) {


      modData.foreach(oMod => {
        oMod.enabled.onChange(EventHandlers.modChanged(oMod))
      })

      root = new BorderPane {
        center = new VBox {
          vgrow = Priority.Always
          private val vBoxHeightProp = height
          children = {
            Seq(
              new ButtonBar {
                buttons += UIComponents.patchButton
              },
              new ScrollPane {
                fitToWidth = true
                fitToHeight = true
                content = UIComponents.modTableView(modData, Some(vBoxHeightProp))
              }
            )
          }
        }
      }
      //      root = modTableView

    }
  }
  if (failedModDefinitionFiles.nonEmpty) {
    val msg = "These directories in the mods directory do not have a " + Constants.modDefinitionFilename + " file:" + System.lineSeparator() +
      errorFilesMessage(failedModDefinitionFiles)
    UIComponents.errorAlert(msg)
  }
  if (failedModRead.nonEmpty) {
    val msg = "Could not read " + Constants.modDefinitionFilename + " file from the following mods:" + System.lineSeparator() +
      errorFilesMessage(failedModRead)
    UIComponents.errorAlert(msg)
  }
  if (failedModParse.nonEmpty) {
    val msg = "The " + Constants.modDefinitionFilename + " file for these mods could not be parsed:" + System.lineSeparator() +
      errorFilesMessage(failedModParse)
    UIComponents.errorAlert(msg)
  }
}
