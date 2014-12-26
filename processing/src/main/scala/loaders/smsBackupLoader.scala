package com.scinia

import com.scinia.Tables._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError

/**
 * {
 *   "date": "2014-08-22 15:55:39",
 *   "from": "9778907433",
 *   "text": "secrets and things",
 *   "to": "Me"
 * }
 * 
 * Reads a JSON data source produced by /tools/sms-iphone.py
 * Converts it into a Seq[ChatRecord] for insertion into the DB.
 * 
 * There's not much in here because this is pretty much the most
 * simple loader. The JSON being loaded is already in the ChatRecord
 * format. This is just plumbing.
 * 
 * See HangoutsLoader for a more complicated MessageLoader
 */
object SmsLoader extends JsonMessageLoader[IphoneSmsRecord] {
  
  override def toChatRecords(records: List[IphoneSmsRecord]) 
    = records.map { r =>
      ChatRecord(
        date = parseDate(r.date),
        from = r.from,
        to   = r.to,
        text = r.text,
        source = LoaderId.SMS
      )
    }

  override val reader = list[IphoneSmsRecord](Json.reads[IphoneSmsRecord])

  def parseDate(date: String) = date.stripPrefix("'").stripSuffix("'")
}

// This happens to be pretty much ChatRecord minus Source
case class IphoneSmsRecord(
  date: String, 
  from: String, 
  to:   String, 
  text: String
)

