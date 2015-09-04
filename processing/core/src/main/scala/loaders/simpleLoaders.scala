package com.scinia

import com.scinia.LoaderId.LoaderId
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError

import com.scinia.Tables._
import com.typesafe.scalalogging.LazyLogging
import scala.util.Try

trait ChatRecordLoader 
  extends loadDelimitedRecords[ChatRecord]
  with ParseJson[ChatRecord]
{ 
  type JsonRecord = ChatRecord

  def transform(record: JsonRecord) = record

  // an implicit reads for the type LoaderId which is the only non native field in a ChatRecord
  // http://stackoverflow.com/questions/14754092/how-to-turn-json-to-case-class-when-case-class-has-only-one-field
  // This is the most frustrating library I have ever used you have no idea how long it took me to write
  // this lasdkfjals;dkfjas;dlkfjasd;flk
  // EDIT: also this line must come before Json.reads[ChatRecord] or the JSON macro throws an NPE
  implicit val LoaderIdReader: Reads[LoaderId] = ((__).read[Int]).map { id => LoaderId(id)}
  
  val reader = Json.reads[ChatRecord]
}

object GoogleVoiceLoader extends ChatRecordLoader

object GChatLoader extends ChatRecordLoader

object FacebookLoader extends ChatRecordLoader

object GoogleSearchLoader
  extends loadDelimitedRecords[SearchQuery]
  with ParseJson[SearchQuery]
{
  type JsonRecord = SearchQuery

  def transform(record: SearchQuery) = record
  
  val reader = Json.reads[SearchQuery]
}

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
 */
object SmsLoader
  extends LoadRecordContainingObject[ChatRecord]
  with ParseJsonList[ChatRecord]
{
  case class JsonRecord(
    date: String, 
    from: String, 
    to:   String, 
    text: String)
  
  val reader = list[JsonRecord](Json.reads[JsonRecord])
  
  def transform(r: JsonRecord): ChatRecord =
    ChatRecord(
      date = parseDate(r.date),
      from = r.from,
      to   = r.to,
      text = r.text,
      source = LoaderId.SMS)

  def parseDate(date: String) = date.stripPrefix("'").stripSuffix("'")
}

object LastFMLoader
  extends loadDelimitedRecords[SongPlay]
  with ParseTSV[SongPlay]
{  
  override def pathModifier(path: String) = s"$path/data/scrobbles.tsv"

  def transform(record: Seq[String]): Option[SongPlay] =
    (for {
      //required fields
      time       <- Try(record(0))
      trackName  <- Try(record(2))
      artistName <- Try(record(4))
      //optional fields
      trackMbid  = Try(record(3)).toOption
      artistMbid = Try(record(5)).toOption
      albumName  = Try(record(10)).toOption
      albumMbid  = Try(record(11)).toOption
    } yield {
      SongPlay(
        time,
        trackName,
        trackMbid,
        artistName,
        artistMbid,
        albumName,
        albumMbid,
        7 // LoaderId.LASTFM
      )
    }).toOption
}
