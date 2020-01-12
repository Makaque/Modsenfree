import scala.sys.process._

name := "modsenfree"

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  "Modsenfree_" + module.revision + "." + artifact.extension
}

version := "0.1"

scalaVersion := "2.13.1"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8")

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "12.0.2-R18"

// https://mvnrepository.com/artifact/com.typesafe.play/play-json
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.7.4"

libraryDependencies += "com.lihaoyi" %% "upickle" % "0.8.0" // SBT

// Add OS specific JavaFX dependencies
val javafxModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux") => "linux"
  case n if n.startsWith("Mac") => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}
libraryDependencies ++= javafxModules.map(m => "org.openjfx" % s"javafx-$m" % "12.0.2" classifier osName)

// Run shell script to compile C# files
lazy val buildCS = taskKey[Unit]("Compile C# files")


buildCS := {
  Process(Seq("sh", "./buildcs.sh")).!
}

(run in Compile) := (run in Compile).dependsOn(buildCS).toTask("").value