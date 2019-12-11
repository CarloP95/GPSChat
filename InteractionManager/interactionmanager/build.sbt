name := "akka-quickstart-scala"

version := "1.0"

scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.0"
lazy val cfversion   = "2.0.0-RC1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.eclipse.californium" % "californium-core" % cfversion,
  "org.eclipse.californium" % "element-connector" % cfversion,
  "org.eclipse.californium" % "element-connector-tcp" % cfversion,
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.7.0",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test
)
