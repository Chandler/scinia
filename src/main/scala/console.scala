package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import java.util.NoSuchElementException

object Console {
  import DbHelper._
  import Message._

  val sqlitePath = "/Users/cabraham/code/scinia/dev.db"
  val smsPath   = "../sources/imessage/2013-12-08_to_2014-08-22.json"
  val hangoutsPath = "../sources/hangouts/Hangouts.json"

  val db        = connect(sqlitePath)
  def main(args: Array[String]): Unit =
    // try {
      args.head match {
        case "recreate"     => recreateDatabase()
        case "loadSms"      => loadSms()
        case "loadHangouts" => loadHangouts()
        case "all"          => all()
      }
    // } catch {
    //   case e: NoSuchElementException => println("You need to pass a command")
    //   case e: scala.MatchError       => println("invalid command")
    // }

  def loadSms() =
    db.withSession { implicit session =>
      println("Loading SMS")
      SmsBackupLoader(smsPath) match {
        case Some(records) => Tables.messages ++= records.flatMap(toMessage)
        case _ => { println("loadSMS failed")}
      }
    }

  def loadHangouts() =
    db.withSession { implicit session =>
      println("Loading Hangouts")
      HangoutsLoader(hangoutsPath) match {
        case Some(records) => Tables.messages ++= records.flatMap(toMessage)
        case _ => { println("loadHangouts failed")}
      }
    }


  def recreateDatabase() =
    db.withSession { implicit session =>
      println("Rebuilding DB")
      DbHelper().recreateTables(Tables.ddls)
    }

  def all() = {
   recreateDatabase()
   loadSms()
   loadHangouts()
  }
}






