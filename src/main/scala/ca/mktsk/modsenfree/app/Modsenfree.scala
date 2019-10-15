package ca.mktsk.modsenfree.app


import java.io.File
import java.lang.{Boolean => Jbool}

import ca.mktsk.modsenfree.exceptions.{Exceptions, NotDirectoryException}
import ca.mktsk.modsenfree.io.FileIO
import ca.mktsk.modsenfree.mod.{Constants, Mod}
import ca.mktsk.modsenfree.utils.{JsonUtils, StringUtils}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.beans.value.ObservableValue
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.cell.CheckBoxTableCell
import scalafx.scene.control.{Alert, ButtonType, TableColumn, TableView}

import scala.util.Failure


case class ObservableMod(name: StringProperty, enabled: BooleanProperty, file: File)


object ObservableMod {
  def asMod(observableMod: ObservableMod): Mod = {
    Mod(observableMod.name.value, observableMod.enabled.value, observableMod.file)
  }

  def fromMod(mod: Mod): ObservableMod = {
    val name = StringProperty(mod.name)
    val enabled = new BooleanProperty(mod.enabled.asInstanceOf[Jbool], StringUtils.titleWordCap(Mod.enabledFieldName), mod.enabled.asInstanceOf[Jbool])
    val file = mod.file
    ObservableMod(name, enabled, file)
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

  stage = new PrimaryStage {
    //    initStyle(StageStyle.Unified)
    title = "Modsenfree Mod Loader"
    scene = new Scene(600, 600) {
      //      fill = Color.rgb(38, 38, 38)
      private val modDirectories = FileIO.getSubdirectories(new File(Constants.modSearchDirectory))
        .recover { case _: Throwable => throw NotDirectoryException("Could not find mod directory") }

      private val attemptModDefinitionFiles = modDirectories.getOrElse(List.empty)
        .map(modDir => (modDir, FileIO.getModDefinitionFile(modDir, Constants.modDefinitionFilename)))
      private val (failedModDefinitionFiles, modDefinitionFiles) = Exceptions.splitWithCause(attemptModDefinitionFiles)
      //      private val modDefinitionFiles: List[File] = attemptModDefinitionFiles.collect{ case (_, Success(file)) => file}
      private val (failedModRead, modStrings) = Exceptions.splitWithCause(modDefinitionFiles.map(file =>
        (file, FileIO.getFileContent(file).map(s => (s, file))
        )))

      private val (failedModParse, mods) = Exceptions.splitWithCause(modStrings.map { case (ms, f) => (f, JsonUtils.jsonStringToMod(ms, f)) })
      private val observableMods = mods.map(mod => ObservableMod.fromMod(mod))


      private val modData: ObservableBuffer[ObservableMod] = ObservableBuffer(observableMods)
      private val mData: ObservableBuffer[ObservableMod] = ObservableBuffer(
        ObservableMod.fromMod(Mod("bigger text", enabled = true, new File("faketestfile"))),
        ObservableMod.fromMod(Mod("run always", enabled = false, new File("faketestfile2")))
      )
      modData.foreach(oMod => {
        oMod.enabled.onChange {
          println("Changed" + oMod.name)
        }
      })

      private val modTableView: TableView[ObservableMod] = new TableView(modData) {
        editable = true
        columnResizePolicy = TableView.ConstrainedResizePolicy


        private val nameColumn = new TableColumn[ObservableMod, String](StringUtils.titleWordCap(Mod.nameFieldName)) {
          cellValueFactory = cdf => cdf.value.name
        }
        private val enabledColumn: TableColumn[ObservableMod, Jbool] = new TableColumn[ObservableMod, Jbool](StringUtils.titleWordCap(Mod.enabledFieldName)) {
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
      root = modTableView

      if (failedModDefinitionFiles.nonEmpty) {
        val msg = "These directories in the mods directory do not have a " + Constants.modDefinitionFilename + " file:" + System.lineSeparator() +
          errorFilesMessage(failedModDefinitionFiles)

        new Alert(AlertType.Error, msg, ButtonType.OK).showAndWait()
      }
      if (failedModRead.nonEmpty) {
        val msg = "Could not read " + Constants.modDefinitionFilename + " file from the following mods:" + System.lineSeparator() +
          errorFilesMessage(failedModRead)
        new Alert(AlertType.Error, msg, ButtonType.OK).showAndWait()
      }
      if (failedModParse.nonEmpty) {
        val msg = "The " + Constants.modDefinitionFilename + " file for these mods could not be parsed:" + System.lineSeparator() +
          errorFilesMessage(failedModParse)
        new Alert(AlertType.Error, msg, ButtonType.OK).showAndWait()
      }
    }
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