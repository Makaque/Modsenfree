import scala.sys.process._

name := "modsenfree"

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  "Modsenfree_" + module.revision + "." + artifact.extension
}

version := "0.1"

scalaVersion := "2.13.1"

assemblyJarName in assembly := "Modsenfree_Standalone_" + version.value + ".jar"

mainClass in (Compile, run) := Some("ca.mktsk.modsenfree.app.Main")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8")

libraryDependencies += "com.lihaoyi" %% "upickle" % "0.8.0" // SBT

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.rename
  case x => MergeStrategy.first
}

target in assembly := file(target.value.getPath + Path.sep + "modsenfree")

Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "fxml"
Compile / unmanagedResources / includeFilter := "*.fxml"

//unmanagedResources in Compile := Seq(baseDirectory.value / "src" / "main" / "fxml")


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

(compile in Compile) := (compile in Compile).dependsOn(buildCS).value