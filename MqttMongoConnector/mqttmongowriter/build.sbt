name := "akka-quickstart-scala"

version := "1.0"

scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.8.0",
  "io.netty" % "netty-all" % "4.1.44.Final",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.10.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.1.1",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test
)

enablePlugins(JavaAppPackaging)
enablePlugins(AshScriptPlugin)
enablePlugins(DockerPlugin)
  
mainClass in Compile := Some("com.carlop.gpschat.MQTTMongoConnector")
dockerBaseImage      := "openjdk:jre-alpine"