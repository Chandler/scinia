package com.scinia

import com.typesafe.scalalogging.LazyLogging

// It's my party and I can use whatever logging syntax I like
// including capitalizing a def.
trait Log extends LazyLogging {
  def Log(msg: String)                   = logger.info(msg)
  def Log(msg: String, cause: Throwable) = logger.info(msg, cause) 
}
