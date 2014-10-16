package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import java.io.File
import scala.util.Try
import Message._

// This file contains all the configuration for my personal
// scinia. Replace this to do your own thing.

object Config extends BaseConfig {
  override val sqlitePath        = "/Users/cabraham/code/scinia/dev.db"
  override val sourcePath        = "/Users/cabraham/scinia"
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

object SkypeSource extends MessageSource {
  override val name        = "skype"
  override val useDropZone = false
  override val loader      = SkypeLoader
}

object GoogleVoiceSource extends MessageSource {
  override val name          = "googleVoice"
  override val useDropZone   = true
  override val loader        = GoogleVoiceLoader
  override val preprocessor  = Preprocessors.googleVoice
}

object HangoutsSource extends MessageSource {
  override val name        = "hangouts"
  override val useDropZone = true
  override val loader      = HangoutsLoader
}

object IPhone4Messages extends MessageSource {
  override val name        = "sms"
  override val useDropZone = false
  override val loader      = SmsLoader
}

object IPhone5Messages extends MessageSource {
  override val name        = "sms"
  override val useDropZone = true
  override val loader      = SmsLoader
}

object LastFMSource extends DataSource {
  override val name        = "lastfm"
  override val useDropZone = true
  override val loader      = LastFMLoader
  override val table       = Tables.songPlays
}

object Preprocessors {
  val googleVoice = {
    val path = "/Users/cabraham/code/scinia/src/main/python/preprocessors/voiceTransformer.py"
    BuildPreprocessor(Seq("python", path))
  }
}
