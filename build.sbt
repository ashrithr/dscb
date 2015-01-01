name := "dscb"

version := "1.0"

scalaVersion := "2.11.4"

// disable using the Scala version in output paths and artifacts
crossPaths := false

// set the main Scala source directory to be <base>/src
scalaSource in Compile := baseDirectory.value / "src"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xlint")

// library dependencies
libraryDependencies ++= Seq(
   "com.typesafe.akka" %% "akka-actor" % "2.3.8",
   "org.scala-lang" % "scala-swing" % "2.11.0-M7",
   "com.esotericsoftware.kryo" %  "kryo" % "2.22"
)