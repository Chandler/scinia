package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import java.util.NoSuchElementException
import scala.util.Try

// Work in progress console for kicking off runs and database operations
abstract class SciniaApp extends Log {
  import DbHelper._

  val env: String
  
  val config: SciniaConfig
  
  val registeredSources: List[DataSource]

  // This is the only place in the application that a db connection is made.
  // The connection is passed into 
  lazy val db = connect(config.databasePath)

  def main(args: Array[String]): Unit =
    try {
      args.head match {
        case "createTables" => createTables()
        case "recreate" => recreateDatabase()
        case "setup"    => DataSource.setupDirs(config, registeredSources)
        case "process"  => processSourceFile(args(1), args(2))
        case "clean"  =>  {
          if(env.contains("prod")) {
            Log("cannot cleanup production data, do that manually, for god's sake.")
          } else {
            DataSource.cleanDirs(config)
          }
        }

      }
    } catch {
      case ex: Exception => Log("console failure", ex)
    }
 
 /**
  * Kick off a run if the requested source is registered.
  */
  def processSourceFile(sourceName: String, filePath: String) = {
    registeredSources
      .find(_.name == sourceName.trim)
      .map(source => source(filePath, db))
  }

  def createTables() =
    db.withSession { implicit session =>
      DbHelper().createTables(Tables.ddls)
    } 

  def recreateDatabase() =
    db.withSession { implicit session =>
      DbHelper().recreateTables(Tables.ddls)
    }
}


