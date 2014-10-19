package com.scinia

import com.scinia.Tables._
// import com.scinia.CommonReads._
import com.scinia.LoaderId._
import com.typesafe.scalalogging.LazyLogging

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError

// We use python preprocessing to on the google voice data, so
// it doesn't need any special logic, it maps straight to ChatRecord
object GoogleVoiceLoader extends Loader[ChatRecord] {

  override def apply(path: String): Seq[ChatRecord] =
    io.Source.fromFile(path).getLines.toSeq.flatMap { line =>
      Json.parse(line)
        .validate[ChatRecord](reader) match {
          case JsSuccess(record, _) => Some(toChatRecord(record))
          case JsError(errors) =>
            logger.debug(s"could not parse $path: $errors")
            None
        }
    }

  def toChatRecord(record: ChatRecord) = record

  // an implicit reads for the type LoaderId which is the only non native field in a ChatRecord
  // http://stackoverflow.com/questions/14754092/how-to-turn-json-to-case-class-when-case-class-has-only-one-field
  // This is the most frustrating library I have ever used you have no idea how long it took me to write
  // this lasdkfjals;dkfjas;dlkfjasd;flk
  // EDIT: also this line must come before Json.reads[ChatRecord] or the JSON macro throws an NPE
  implicit val LoaderIdReader: Reads[LoaderId] = ((__).read[Int]).map { id => LoaderId(id)}
  
  val reader = Json.reads[ChatRecord]
}


