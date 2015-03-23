/** 
 * useful record representations that are generic enough to be used by more than one
 * component of the library (loaders, models, tables ect)
 */
package com.scinia

import com.scinia.LoaderId.LoaderId

/**
 * A completely generic model of an instant message
 * Applicable to any IM client.
 */
case class ChatRecord(
  date: String, // 2014-08-22 15:55:39 (or any ISO8601 timestring)
  from: String, // Whatever the primary identifier string is, e.g. phone # or name
  to:   String, // ditto
  text: String,
  source: LoaderId // every chat has to come from somewhere
)
