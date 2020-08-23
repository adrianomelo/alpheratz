
enablePlugins(JavaAppPackaging)

name := "alpheratz"
version := "0.1"
dockerUsername := Some("adrianomelo")

scalaVersion := "2.12.12"

val akkaVersion = "2.6.8"
val akkaHttpVersion = "10.2.0"
val akkaHttpCorsVersion = "1.1.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "ch.megard" %% "akka-http-cors" % akkaHttpCorsVersion
)
