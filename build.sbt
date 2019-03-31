name := "iot"

version := "0.1"

scalaVersion := "2.12.8"

lazy val root = (project in file("."))
  .settings(
    name := "iot",
    libraryDependencies ++= Seq(
      "com.rabbitmq" % "amqp-client" % "3.3.5",
      "ai.x" %% "play-json-extensions" % "0.10.0"
    )
  )
