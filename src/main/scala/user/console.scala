package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import java.util.NoSuchElementException
import scala.util.Try

// This is a debugging console specific to Chandler's Scinia configuration

object Console {
  import DbHelper._
  import Message._

  val db = connect(Config.sqlitePath)
  
  def main(args: Array[String]): Unit =
    args.head match {
      case "recreate" => recreateDatabase()
      case "setup"  => DataSource.setupDirs(Config.sourcePath)
      case "voice"  => GoogleVoiceSource("/Users/cabraham/scinia/dropZone/googleVoice/voice", db)
    }

  def recreateDatabase() =
    db.withSession { implicit session =>
      println("Rebuilding DB")
      DbHelper().recreateTables(Tables.ddls)
    }
}
  // def loadSms() =
  //   db.withSession { implicit session =>
  //     println("Loading SMS")
  //     SmsLoader(smsPath) match {
  //       case Some(records) => Tables.messages ++= records.flatMap(toMessage)
  //       case _ => { println("loadSMS failed")}
  //     }
  //   }

  // def loadHangouts() =
  //   db.withSession { implicit session =>
  //     println("Loading Hangouts")
  //     HangoutsLoader(hangoutsPath) match {
  //       case Some(records) => Tables.messages ++= records.flatMap(toMessage)
  //       case _ => { println("loadHangouts failed")}
  //     }
  //   }

  // def loadVoice() =
  //   db.withSession { implicit session =>
  //     println("Loading Voice")
  //     Tables.messages ++= GoogleVoiceLoader(voicePath).flatMap(toMessage)
  //   }

  // def loadSkype() =
  //   db.withSession { implicit session =>
  //     println("Loading Skype")
  //     Tables.messages ++= SkypeLoader(skypePath).flatMap(toMessage)
  //   }

  // def loadLastFM() = 
  //   db.withSession { implicit session =>
  //     println("Loading LastFM")
  //     Tables.songPlays ++= LastFMLoader(lastFMPath)
  //   }

  // def recreateDatabase() =
  //   db.withSession { implicit session =>
  //     println("Rebuilding DB")
  //     DbHelper().recreateTables(Tables.ddls)
  //   }

  // def all() = {
  //   recreateDatabase()
  //   loadSms()
  //   loadHangouts()
  // }
