package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import java.io.File
import scala.util.Try
import Message._

// This file contains all the configuration for my personal
// scinia. Replace this to do your own thing.

object Config extends BaseConfig {
  // override val sqlitePath        = "/Users/cabraham/code/scinia/dev.db"
  // override val sourcePath        = "/Users/cabraham/scinia"
  override val sqlitePath        = "/Users/cabraham/devscinia/dev.db"
  override val sourcePath        = "/Users/cabraham/devscinia"

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

object IPhone5Messages extends MessageSource {
  override val name             = "sms"
  override val manageRuns       = true
  override val loader           = SmsLoader
  override val preprocessor     = Preprocessors.iphoneBackup
}

object IPhone4Messages extends MessageSource {
  override val name             = "iphone4Backup"
  override val manageRuns       = false // This phone is gone, we won't be seeing any updates to the data
  override val loader           = SmsLoader
  override val table            = Tables.messages
}

