package com.scinia

import com.scinia.DataSource.Processor
import java.io.File
import scala.slick.driver.SQLiteDriver.simple._
import scala.util.Try

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

object GChatSource extends MessageSource {
  override val name         = "gchat"
  override val manageRuns   = false
  override val loader       = GChatLoader
  override val preprocessor = Preprocessors.gchat
}

object FacebookSource extends MessageSource {
  override val name         = "facebook"
  override val manageRuns   = true
  override val loader       = FacebookLoader
  override val preprocessor = Preprocessors.facebook
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

object GoogleSearchSource extends DataSource {
  override val name         = "google_search"
  override val manageRuns   = true
  override val loader: Loader[SearchQuery] = GoogleSearchLoader
  override val preprocessor = Preprocessors.googleSearch
  override val table        = Tables.searches
  override val loadAndStore: Processor = (file: File, db: Database) =>
    Try {
      db.withSession { implicit session =>
        val records = loader(file.toString)
        Log("about to store " + records.size + " Google Searches into the searches table")
        table ++= records
        Log("completed db store")
      } 
   }
}
