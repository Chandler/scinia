package com.scinia

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError
import com.scinia.Source._

/**
 * A completely generic model of an instant message
 * Applicable to any IM client.
 */
case class ChatRecord(
  date: String, // 2014-08-22 15:55:39 (or any ISO8601 timestring)
  from: String, // Whatever the primary identifier string is, e.g. phone # or name
  to:   String, // ditto
  text: String,
  source: Source // every chat has to come from somewhere
)

/**
 * Base class for building loaders that convert JSON
 * from various types of IM history files into Seq[ChatRecord]
 */
abstract class MessageLoader[A] {

  /**
   * @param The path to a JSON file containing IM history
   * @return a bunch of ChatRecords
   */
  def apply(path: String): Option[Seq[ChatRecord]] =
    Json.parse(
      io.Source.fromFile(path).getLines.mkString
    )
    .validate[List[A]](reader) match {
      case JsSuccess(records, _) => Some(toChatRecords(records))
      case JsError(errors)     => { println(errors); None }
    }

  /**
   * A Play Scala Reads capable of parsing the JSON object located at path
   */
  val reader: Reads[List[A]]

  /**
   * A method to convert from reader's output (List[A]) to a Seq[ChatRecords]
   */
  def toChatRecords(records: List[A]): Seq[ChatRecord]
}
