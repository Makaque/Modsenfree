package ca.mktsk.modsenfree.app

import java.nio.file.Paths

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

object Main {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Main], args:_*)
  }
}

class Main extends Application{
  override def start(primaryStage: Stage): Unit = {

    val fxmlFile = Paths.get("src", "main", "scala", "ca/mktsk/modsenfree/app/ModsenfreeGUI.fxml").toUri.toURL
    val root: Parent = FXMLLoader.load(fxmlFile)
    primaryStage.setTitle("Modsenfree")
    primaryStage.setScene(new Scene(root, 500, 400))
    primaryStage.show()

  }
}
