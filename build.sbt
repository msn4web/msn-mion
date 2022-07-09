name := "MSNGateway"

version := "0.1"

scalaVersion := "2.13.8"

Compile / unmanagedClasspath += baseDirectory.value / "resources"
Runtime / unmanagedClasspath += baseDirectory.value / "resources"

libraryDependencies += "commons-logging" % "commons-logging" % "1.1.3"
libraryDependencies += "org.apache.httpcomponents" % "httpcore" % "4.0.1"
libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.18.0"
libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.18.0"
libraryDependencies += "org.apache.logging.log4j" %% "log4j-api-scala" % "12.0"

val circeVersion = "0.14.1"
libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
).map(_ % circeVersion)