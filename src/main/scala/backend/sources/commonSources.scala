package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import java.io.File
import scala.util.Try
import Message._
import com.scinia.DataSource.LoadAndStore

//TODO need to figure best way to abstract out loadAndStores, too much code in the config right now

object SkypeSource extends DataSource {
  override val name             = "skype"
  override val useRunProcessing = false
  override val loader           = SkypeLoader
  override val table            = Tables.messages
  override val loadAndStore: LoadAndStore = (file: File, db: Database) =>
    Try {
      db.withSession { implicit session =>
       table ++= loader(file.toString).flatMap(toMessage)
     } 
   }
}

object GoogleVoiceSource extends DataSource {
  override val name          = "googleVoice"
  override val useRunProcessing   = true
  override val loader        = GoogleVoiceLoader
  override val preprocessor  = Preprocessors.googleVoice
  override val table         = Tables.messages
  override val loadAndStore: LoadAndStore = (file: File, db: Database) =>
    Try {
      db.withSession { implicit session =>
       table ++= loader(file.toString).flatMap(toMessage)
     } 
   }
}

object HangoutsSource extends DataSource {
  override val name             = "hangouts"
  override val useRunProcessing = true
  override val loader           = HangoutsLoader
  override val table            = Tables.messages
  override val loadAndStore: LoadAndStore = (file: File, db: Database) =>
    Try {
      db.withSession { implicit session =>
        HangoutsLoader(file.toString) match {
          case Some(records) => table ++= records.flatMap(toMessage)
          case _ => { println("loadHangouts failed")}
        }
     } 
   }
}

object IPhoneBackupSource extends DataSource {
  override val name             = "sms"
  override val useRunProcessing = true
  override val loader           = SmsLoader
  override val table            = Tables.messages
  override val preprocessor     = Preprocessors.iphoneBackup
}

object LastFMSource extends DataSource {
  override val name             = "lastfm"
  override val useRunProcessing = true
  override val loader           = LastFMLoader
  override val table            = Tables.songPlays
  override val loadAndStore: LoadAndStore = (file: File, db: Database) =>
    Try {
      db.withSession { implicit session =>
        table ++= loader(file.toString)
      } 
   }
}

object Preprocessors {
  val googleVoice =
    BuildPreprocessor( (input: String, output: String) => 
      Seq(
        "python", 
        "src/main/python/backend/preprocessors/voiceTransformer.py",
        input,
        output
      )
    )

  val iphoneBackup =
    BuildPreprocessor( (input: String, output: String) => 
      Seq(
        "python", 
        "tools/sms-backup.py",
        "--format",
        "json",
        "--date-format",
        "'%Y-%m-%dT%H:%M:%S'",
        "--input",
        input,
        "--output",
        output
      )
    )
}
