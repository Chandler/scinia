package com.scinia

import play.api.libs.json._
import com.scinia.Tables._
import com.scinia.Source._
/**
 * {
 *   "date": "2014-08-22 15:55:39",
 *   "from": "9778907433",
 *   "text": "secrets and things",
 *   "to": "Me"
 * }
 */
case class SmsBackupRecord(
  date: String,
  from: String,
  to:   String,
  text: String
)

/**
 * Reads a Json data source produced by /tools/sms-iphone.py
 * Converts it into a Seq[Message] for insertion into the DB.
 */
object SmsBackupLoader {
  implicit val reads = Json.reads[SmsBackupRecord]

  def apply(path: String): Option[Seq[SmsBackupRecord]] =
    Json.parse(
      io.Source.fromFile(path).getLines.mkString
    )
    .validate[List[SmsBackupRecord]] match {
      case JsSuccess(records, _) => Some(records)
      case JsError(errors)     => { println(errors); None }
    }
}


