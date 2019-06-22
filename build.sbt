name := """si"""
organization := "chip"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
PlayKeys.devSettings += "play.server.http.idleTimeout" -> "infinite"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.19",
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  "com.typesafe.akka" %% "akka-remote" % "2.5.19",
  "org.webjars" %% "webjars-play" % "2.7.0",
  "org.webjars" % "flot" % "0.8.3-1",
  "org.webjars" % "bootstrap" % "3.3.6"
)
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "chip.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "chip.binders._"
