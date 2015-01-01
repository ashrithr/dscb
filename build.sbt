import sbt.Keys._
import sbt.Keys.baseDirectory
import sbt.Keys.crossPaths
import sbt.Keys.libraryDependencies
import sbt.Keys.name
import sbt.Keys.scalaVersion
import sbt.Keys.scalacOptions
import sbt.Keys.unmanagedSourceDirectories
import sbt.Keys.version

name := "dscb"

version := "1.0"

scalaVersion := "2.10.4"

// disable using the Scala version in output paths and artifacts
crossPaths := false

// scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xlint")

// library dependencies
libraryDependencies ++= Seq(
   "org.slf4j" % "slf4j-api" % "1.7.9",
   "com.twitter" %% "scrooge-core" % "3.14.1" exclude("org.scala-lang", "scala-library"),
   "org.apache.thrift" % "libthrift" % "0.9.1" exclude("org.apache.httpcomponents", "httpclient") exclude("org.apache.httpcomponents", "httpcore") exclude("org.slf4j", "slf4j-api") exclude("org.apache.commons", "commons-lang3"),
   "com.twitter" %% "finagle-core" % "6.6.2" exclude("com.twitter", "util-logging_2.10") exclude("com.twitter", "util-app_2.10"),
   "com.twitter" %% "finagle-thrift" % "6.6.2" exclude("org.scala-lang", "scala-library") exclude("org.apache.thrift", "libthrift"),
   "com.typesafe" % "config" % "1.0.2",
   "org.mapdb" % "mapdb" % "0.9.13",
   "com.typesafe.akka" %% "akka-actor" % "2.3.8",
   "org.scala-lang" % "scala-swing" % "2.10.4",
   "com.esotericsoftware.kryo" %  "kryo" % "2.22",
   "com.twitter" %% "finagle-http" % "6.6.2" % "test",
   "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.1.3" % "test",
   "org.scalatest" % "scalatest_2.10" % "2.0.M6" % "test",
   "ch.qos.logback" % "logback-classic" % "1.1.1" % "test",
   "junit" % "junit" % "4.8.1" % "test"
)

unmanagedSourceDirectories in Compile <++= baseDirectory { base =>
   Seq(
      base / "src/main/resources",
      base / "src/main/thrift"
   )
}

unmanagedSourceDirectories in Test <++= baseDirectory { base =>
   Seq(
      base / "src/test/resources"
   )
}

com.twitter.scrooge.ScroogeSBT.newSettings

scroogeThriftOutputFolder in Compile  := file("src/main/scala")