name := "scinia"

version := "0.1"

scalaVersion := "2.11.0"

libraryDependencies += "com.typesafe.slick" %% "slick" % "2.1.0"

libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % "2.1.0-RC3"


libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.8.0-SNAPSHOT"

libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "1.4.0"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

libraryDependencies +=  "com.typesafe.play" %% "play-json" % "2.3.0"

libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.0.0"

// libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.6.4"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

libraryDependencies += "joda-time" % "joda-time" % "2.0"

// lol yes I'm using three io libraries. for now. step off me.
libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies += "com.google.guava" % "guava" % "18.0"

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3"

resolvers ++= Seq(
  "org.catch22" at "http://marklister.github.io/product-collections/",
  "SQLite-JDBC Repository" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
)

