package ca.mktsk.modsenfree.app


import java.lang.{Boolean => Jbool}

import ca.mktsk.modsenfree.mod.Mod
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.beans.value.ObservableValue
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.cell.CheckBoxTableCell
import scalafx.scene.control.{TableColumn, TableView}


case class ObservableMod(name: StringProperty, enabled: BooleanProperty)


object ObservableMod{
  def asMod(observableMod: ObservableMod): Mod = {
    Mod(observableMod.name.value, observableMod.enabled.value)
  }

  def fromMod(mod: Mod): ObservableMod = {
    val name = StringProperty(mod.name)
    val enabled = new BooleanProperty(mod.enabled.asInstanceOf[Jbool], "Enabled", mod.enabled.asInstanceOf[Jbool])
    ObservableMod(name, enabled)
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
  stage = new PrimaryStage {
    //    initStyle(StageStyle.Unified)
    title = "Modsenfree Mod Loader"
    scene = new Scene(600, 600) {
      //      fill = Color.rgb(38, 38, 38)
      private val modData: ObservableBuffer[ObservableMod] = ObservableBuffer(
        ObservableMod.fromMod(Mod("bigger text", enabled = true)),
        ObservableMod.fromMod(Mod("run always", enabled = false))
      )
      modData.foreach(oMod => {
        oMod.enabled.onChange{
          println("Changed" + oMod.name)
        }
      })

      private val modTableView: TableView[ObservableMod] = new TableView(modData){
        editable = true
        columnResizePolicy = TableView.ConstrainedResizePolicy


        private val nameColumn = new TableColumn[ObservableMod, String]("Name"){
          cellValueFactory = cdf => cdf.value.name
        }
        private val enabledColumn: TableColumn[ObservableMod, Jbool] = new TableColumn[ObservableMod, Jbool]("Enabled")
        {
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