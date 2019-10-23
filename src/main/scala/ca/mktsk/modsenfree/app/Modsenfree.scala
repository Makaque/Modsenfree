package ca.mktsk.modsenfree.app


import java.io.File
import java.lang.{Boolean => Jbool}

import ca.mktsk.modsenfree.exceptions.{Exceptions, NotDirectoryException}
import ca.mktsk.modsenfree.io.FileIO
import ca.mktsk.modsenfree.mod.{Constants, Mod}
import ca.mktsk.modsenfree.utils.{JsonUtils, StringUtils}
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.beans.value.ObservableValue
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.control.cell.CheckBoxTableCell
import scalafx.scene.layout.{HBox, Pane, VBox}
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle

import scala.sys.process._
import scala.util.Try


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

  def patchButton: Button = new Button {
    text = "Patch"
    minWidth = 50
    onMouseClicked = e =>
      EventHandlers.patch.map(s=> println(s))
  }

  def modTableView(modData: ObservableBuffer[ObservableMod]): TableView[ObservableMod] = new TableView(modData) {
    editable = true
    columnResizePolicy = TableView.ConstrainedResizePolicy


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

  def patch: Try[String] = Try {
    Process("./src/main/cs/TestSharp.exe bla").!!.trim
  }


  def modChanged(oMod: ObservableMod): Unit = {
    println("Changed" + oMod.name)
    val tryWrite = FileIO.writeMod(ObservableMod.asMod(oMod))
    Try {
      val result = Process("./src/main/cs/TestSharp.exe bla").!!.trim
      println("result")
      println(result.length)
      println(result)
      PatcherMessage.withName(result) match {
        case PatcherMessage.RESPONDING => println("patcher responded")
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

      private val modTableView: TableView[ObservableMod] = UIComponents.modTableView(modData)

      root = new VBox() {
        children = Seq(patchButton, modTableView)
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


//new ScrollPane {
//  // fitToHeight = true
//  //          padding = Insets(50, 80, 50, 80)
//  content = new VBox {
//  //            padding = Insets(50, 80, 50, 80)
//  children =
//  (1 to 30).map { i =>
//  new Text {
//  text = Random.nextLong().toString
//  style = "-fx-font: italic bold 100pt sans-serif"
//}
//}
//}
//}