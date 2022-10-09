import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "stock-analyzer",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.0.1",
      "com.github.tototoshi" %% "scala-csv" % "1.3.10",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
      "com.softwaremill.sttp.client3" %% "core" % "3.8.2",
      "com.softwaremill.sttp.client3" %% "json4s" % "3.8.2",
      "com.softwaremill.sttp.client3" %% "slf4j-backend" % "3.8.2",
      "org.typelevel"  %% "squants"  % "1.6.0",
      "org.json4s" %% "json4s-native" % "4.0.6",
      scalaTest % Test
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
