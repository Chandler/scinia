package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import java.util.NoSuchElementException
import scala.util.Try

object Console {
  import DbHelper._
  import Message._

  val smsPath      = "/Users/cabraham/code/sources/imessage/2013-12-08_to_2014-08-22.json"
  val hangoutsPath = "/Users/cabraham/code/sources/hangouts/Hangouts.json"
  val skypePath    = "/Users/cabraham/code/sources/skype/SkypeCSV.csv"
  val lastFMPath   = "/Users/cabraham/code/sources/last.fm/data/scrobbles.tsv"
  val voicePath    = "gvoice.json"
  
  val db           = connect(Config.sqlitePath)
  
  def main(args: Array[String]): Unit =
    args.head match {
      case "recreate"     => recreateDatabase()
      case "loadSms"      => loadSms()
      case "loadHangouts" => loadHangouts()
      case "loadVoice"    => loadVoice()
      case "loadSkype"    => loadSkype()
      case "loadSongs"    => loadLastFM()
      case "setup" => DataSource.setupDirs(Config.sourcePath)
      case "test"  => GoogleVoiceSource("/Users/cabraham/scinia/dropZone/googleVoice/voice", db)
      // case "all"          => all()
    }

  def loadSms() =
    db.withSession { implicit session =>
      println("Loading SMS")
      SmsLoader(smsPath) match {
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

  def loadVoice() =
    db.withSession { implicit session =>
      println("Loading Voice")
      Tables.messages ++= GoogleVoiceLoader(voicePath).flatMap(toMessage)
    }

  def loadSkype() =
    db.withSession { implicit session =>
      println("Loading Skype")
      Tables.messages ++= SkypeLoader(skypePath).flatMap(toMessage)
    }

  def loadLastFM() = 
    db.withSession { implicit session =>
      println("Loading LastFM")
      Tables.songPlays ++= LastFMLoader(lastFMPath)
    }

  def recreateDatabase() =
    db.withSession { implicit session =>
      println("Rebuilding DB")
      DbHelper().recreateTables(Tables.ddls)
    }

  // def all() = {
  //   recreateDatabase()
  //   loadSms()
  //   loadHangouts()
  // }
}

