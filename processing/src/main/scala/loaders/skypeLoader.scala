package com.scinia

import com.github.tototoshi.csv._
import com.scinia.Tables._
import java.io._
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._
// import scala.io.{ Source => IOSource }
import scala.util.control.Breaks._
import scala.util.parsing.input.CharSequenceReader
import scala.util.Try
import org.joda.time.format.{DateTimeFormat => Date }

case class NoRecipientFoundException(msg: String) extends Exception(msg)
case class NoParticipantsFoundException(msg: String) extends Exception(msg)

/**
 * Takes a path to a CSV file full of skype hchat istory and produces Seq[ChatRecord]
 * The format of the CSV file is the output of this old skype backup tool
 * http://www.ni
 * demo record: List(12271, Chat Message, 2/20/2008 11:11:28 PM, shticko, Joel, , watching a video, #shticko/$cbabraham;a5cdccf946726ca8, )
 */
object SkypeLoader 
  extends loadDelimitedRecords[ChatRecord]
  with ParseCSV[ChatRecord]
{
  def transform(record: Seq[String]): Option[ChatRecord] =
    (for {
      sender       <- Try(record(3))
      date         <- Try(parseDate(record(2)))
      text         <- Try(record(6))
      log          <- Try(record(7))
      participants <- Try(getParticipants(record, log))
      recipient    <- Try(getRecipient(sender, participants))
    } yield {
      ChatRecord(
        date   = date,
        from   = sender,
        to     = recipient,
        text   = text,
        source = LoaderId.SKYPE
      )
    }).toOption

  /**
   * if sender doesn't match the participants this is
   * probably a group chat, throw an exception
   */
  def getRecipient(
    sender: String,
    participants: (String, String)
  ): String =
    sender match {
      case participants._1 => participants._2
      case participants._2 => participants._1
      case _               => throw new NoRecipientFoundException("Sender doesn't match either participant")
  }
 
  /**
   * any 8th field (log) which doesn't split into "",name,"",name
   * is bogus and we should drop the record
   */
  def getParticipants(record: Seq[String], log: String): (String, String) =
    log.split("[#/$;]") match {
      case Array("", participant1, "", participant2, _*) => (participant1, participant2)
      case _ => throw new NoParticipantsFoundException("Log cannot be split")
    }

  def parseDate(date: String) = Date.forPattern("M/d/YYYY HH:mm:ss a").parseDateTime(date).toString()
}
