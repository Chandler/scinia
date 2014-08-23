package com.scinia

object Source extends Enumeration {
  type Source  = Value  
  val SMS      = Value(1)
  val GVOICE   = Value(2)
  val LATITUDE = Value(3)
  val HANGOUTS = Value(4)
}
