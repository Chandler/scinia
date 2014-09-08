package com.scinia

object Source extends Enumeration {
  type Source  = Value  
  val SMS      = Value(1) // parsed
  val GVOICE   = Value(2) // need to figure out xml parsing
  val LATITUDE = Value(3) // not important yet
  val HANGOUTS = Value(4) // parsed
  val TWEETS   = Value(5) // not important yet
  val FACEBOOK = Value(6) // sigh, need to figure out html parsing
  val RDIO     = Value(7) // todo
  val SKYPE    = Value(8) // need to find data from computer
}
