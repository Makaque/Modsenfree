package ca.mktsk.modsenfree.app


import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

object Main {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Main], args: _*)
  }
}

class Main extends Application {
  override def start(primaryStage: Stage): Unit = {

    val fxmlFile = getClass.getResource("/ModsenfreeGUI.fxml")
    val root: Parent = FXMLLoader.load(fxmlFile)
    primaryStage.setTitle("Modsenfree")
    primaryStage.setScene(new Scene(root, 500, 400))
    primaryStage.show()

  }
}
