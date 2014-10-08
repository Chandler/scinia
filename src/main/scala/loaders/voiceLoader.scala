package com.scinia

import com.scinia.Tables._
// import com.scinia.CommonReads._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError

// We use python preprocessing to on the google voice data, so
// it doesn't need any special logic, it maps straight to ChatRecord
object GoogleVoiceLoader extends Loader {

  def apply(path: String): Seq[ChatRecord] =
    io.Source.fromFile(path).getLines.toSeq.flatMap { line =>
      Json.parse(line)
        .validate[ChatRecord](reader) match {
          case JsSuccess(record, _) => Some(toChatRecord(record))
          case JsError(errors)      => { println(errors); None}
        }
    }

  def toChatRecord(record: ChatRecord) = record

  val reader = Json.reads[ChatRecord]
}


