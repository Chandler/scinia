name := "scinia"

version := "0.1"

scalaVersion := "2.10.0"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "com.typesafe.slick" %% "slick-codegen" % "2.1.0-RC3",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "org.xerial" % "sqlite-jdbc" % "3.8.0-SNAPSHOT",
  "com.typesafe.play" %% "play-json" % "2.2.1",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "com.github.nscala-time" %% "nscala-time" % "1.4.0"
)

resolvers ++= Seq(
  "SQLite-JDBC Repository" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
)

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")
