package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import java.util.NoSuchElementException

object Main {
  def main(args: Array[String]) =
    try {
      args.head match {
      case "rebuild" => rebuildDatabase()
      case "loadSms" => loadSms()

      }
    } catch {
      case e: NoSuchElementException => println("have a nice day")
    }


  def loadSms() = {

  }


  def rebuildDatabase() = {
    val sqlitePath = "/Users/cabraham/code/scinia/dev.db"

    DB.connect(sqlitePath) withSession { implicit session =>
      SciniaTables.ddls.foreach { t => 
        try {
          t.drop
          t.create
        } catch {
          case e: java.sql.SQLException => println(e)
        }
      }
    }
  }
}






