package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import java.io.File
import scala.util.Try
import Message._
import com.scinia.DataSource.LoadAndStore

// This file contains all the configuration for my personal
// scinia. Replace this to do your own thing.

object Config extends BaseConfig {
  override val sqlitePath        = "/Users/cabraham/code/scinia/dev.db"
  override val sourcePath        = "/Users/cabraham/scinia/"
  override val registeredSources 
    = List(
        SkypeSource,
        GoogleVoiceSource,
        HangoutsSource,
        IPhone4Messages,
        IPhone5Messages,
        HangoutsSource,
        LastFMSource
      )
}

// sources specific to my setup, most sources in in commonSources.scala

object IPhone5Messages extends DataSource {
  override val name             = "sms"
  override val useRunProcessing = true
  override val loader           = SmsLoader
  override val table            = Tables.messages
  override val preprocessor     = Preprocessors.iphoneBackup
  override val loadAndStore: LoadAndStore = (file: File, db: Database) =>
    Try {
      db.withSession { implicit session =>
        SmsLoader(file.toString) match {
          case Some(records) => table ++= records.flatMap(toMessage)
          case _ => { println("loading sms failed")}
        }
     } 
   }
}

object IPhone4Messages extends DataSource {
  override val name             = "iphone4Backup"
  override val useRunProcessing = false
  override val loader           = SmsLoader
  override val table            = Tables.messages
  override val loadAndStore: LoadAndStore = (file: File, db: Database) =>
    Try {
      db.withSession { implicit session =>
        SmsLoader(file.toString) match {
          case Some(records) => table ++= records.flatMap(toMessage)
          case _ => { println("loading sms failed")}
        }
     } 
   }
}

