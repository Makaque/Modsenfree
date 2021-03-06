import scala.sys.process._
import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

name := "modsenfree"

// Name of jar file generated by SBT run
artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  "Modsenfree_" + module.revision + "." + artifact.extension
}

version := "0.1"

scalaVersion := "2.13.1"

// Name of fat jar file
assemblyJarName in assembly := "Modsenfree_Standalone_" + version.value + ".jar"

// Project Main
mainClass in (Compile, run) := Some("ca.mktsk.modsenfree.app.Main")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8")

libraryDependencies += "com.lihaoyi" %% "upickle" % "0.8.0" // SBT

// If duplicate files are found on the relative path during build, handle them in this manner.
// This resolves issues with JavaFX.
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.rename
  case x => MergeStrategy.first
}

// Directory to place fat jar file
target in assembly := file(target.value.getPath + Path.sep + "modsenfree")

// Include fxml files from fxml directory when compiling.
// They're placed at the top level of the jar.
// The hope was to have these placed in a directory within the jar, or alongside the class files
// but this solution works.
Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "fxml"
Compile / unmanagedResources / includeFilter := "*.fxml"

// Creates the fxml directory to target, but does not copy the files inside
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

lazy val copyProps = taskKey[Unit]("Copy properties file to compiled build")

copyProps :=  {
  Files.copy(
    (baseDirectory.value / "modsenfree.properties").toPath,
    (target.value / "modsenfree" / "modsenfree.properties").toPath,
    REPLACE_EXISTING
  )
}

// Compile C# and copy properties file for SBT run
(run in Compile) := (run in Compile).dependsOn(buildCS, copyProps).toTask("").value

// Compile C# and copy properties file for SBT compile
(compile in Compile) := (compile in Compile).dependsOn(buildCS, copyProps).value