package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import java.io.File
import scala.util.Try
import Message._
import com.scinia.DataSource.Processor

object SkypeSource extends MessageSource {
  override val name       = "skype"
  override val manageRuns = false
  override val loader     = SkypeLoader
}

object GoogleVoiceSource extends MessageSource {
  override val name         = "googleVoice"
  override val manageRuns   = true
  override val loader       = GoogleVoiceLoader
  override val preprocessor = Preprocessors.googleVoice
}

object HangoutsSource extends MessageSource {
  override val name       = "hangouts"
  override val manageRuns = true
  override val loader     = HangoutsLoader
}

object IPhoneBackupSource extends MessageSource {
  override val name         = "sms"
  override val manageRuns   = true
  override val loader       = SmsLoader
  override val preprocessor = Preprocessors.iphoneBackup
}

object LastFMSource extends DataSource {
  override val name                     = "lastfm"
  override val manageRuns               = true
  override val loader: Loader[SongPlay] = LastFMLoader
  override val table                    = Tables.songPlays
  override val loadAndStore: Processor  = (file: File, db: Database) =>
    Try {
      db.withSession { implicit session =>
        val records = loader(file.toString)
        Log("about to store " + records.size + " ChatRecords into the messages table")
        table ++= records
        Log("completed db store")
      } 
   }
}
