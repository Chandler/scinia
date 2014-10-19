package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import java.util.NoSuchElementException
import scala.util.Try

// Work in progress console for kicking off runs and database operations
object Console extends Log {
  import DbHelper._

  val db = connect(Config.sqlitePath)

  def main(args: Array[String]): Unit =
    try {
      args.head match {
        case "recreate" => recreateDatabase()
        case "setup"    => DataSource.setupDirs(Config.sourcePath)
        case "process"  => processSourceFile(args(1), args(2))
      }
    } catch {
      case ex: Exception => println(ex)
    }

  def processSourceFile(sourceName: String, filePath: String) = {
    Config.registeredSources
      .find(_.name == sourceName.trim)
      .map(source => source(filePath, db))
  }

  def recreateDatabase() =
    db.withSession { implicit session =>
      DbHelper().recreateTables(Tables.ddls)
    }
}


