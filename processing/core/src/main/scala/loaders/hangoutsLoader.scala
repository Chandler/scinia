package com.scinia

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError
import play.api.libs.json.Reads._
import com.github.nscala_time.time.Imports._

import HangoutsModels._
import HangoutsReaders._

class CouldntFindSenderException extends Exception

object HangoutsLoader extends LoadRecordContainingObject[ChatRecord] {

  val reader: Reads[List[Conversation]] = readConversations

  def parse(obj: String): List[ChatRecord] = transform(ParseJson.parseJsonList(obj, reader))

  def transform(conversations: List[Conversation]): List[ChatRecord] =
    conversations.flatMap { conversation =>
      // If there's no participants field drop record
      // not every event in chat is a valid text event and 
      // text is all we care about right now
      conversation.participants.map { participants =>
        
        val participant1 = participants._1
        val participant2 = participants._2
      
        // drop all the bad events        
        conversation.events.flatten.map { event =>

            val (from, to) = event.senderId match {
              case participant1.chatId => (participant1.name, participant2.name)
              case participant2.chatId => (participant2.name, participant1.name)
              case _                   => throw new CouldntFindSenderException()
            }

            ChatRecord(
              date = (event.timestamp.toLong/1000).toDateTime.toString,
              from = from,
              to   = to,
              text = event.text,
              source = LoaderId.HANGOUTS
            )
        }
      }
    }.flatten
    // flatten because we are turning a list of converstions (list of lists) into
    // a single list of records.
}

/**
 * Conversation, Event, Participant
 *
 * case classes that make a smiple model of the Hangouts JSON history 
 * object for the fields that we care about
 *
 * The Hangouts history can be converted into a List[Conversation]
 * 
 * A lot of fields are optional because this models the data we want, not
 * all the data in the json. Some conversations don't have these fields. Like
 * group video hangouts that we don't care about. Later we'll have to drop
 * anything with missing fields.
 */
object HangoutsModels {
   /** 
    * a conversation is a list of events (messages) between two people
    */
  case class Conversation(
   /* 
    * tuple contain both participants of a conversation
    * at this point it's unknown which one is the sender/receiver
    */
    participants: Option[(Participant, Participant)],
    
    events: List[Option[Event]]
  )

  case class Event(
    senderId: String,    // chatId of the participant who sent the message
    timestamp: String,   // time the message was sent
    text: String         // text of the message
  )

  case class Participant(chatId: String, name: String)
}

/**
 * JSON Reads that each take some full or partial
 * JSON abstract syntax tree (as represented by the JsPath type)
 * and convert it into a Scala type.
 *
 * Reads are composable, you can see that the higher typed
 * Reads like Reads[List[Event]] are made out of composed
 * simpiler Reads like Reads[String]
 *
 * I'm not sure I like this JSON api. It's late, I've been playing with it 
 * for an entire day and it just seems mad. The use of 
 * uses implicits and macros everywhere make it incredibly frustrating to
 * understand what's going on. The following code does away with all the implicits
 * you'll see in the the official docs.
 */
object HangoutsReaders {
 
  val readEvents: Reads[List[Option[Event]]] = 
    list[Option[Event]](
      Reads.optionNoError(
        (
          (__ \ "sender_id" \ "chat_id").read(StringReads) and
          (__ \ "timestamp")            .read(StringReads) and
          ((__ \ "chat_message" \ "message_content" \ "segment")(0) \ "text").read(StringReads)
        )(Event)
      )
    )

  val readParticipants: Reads[(Participant,Participant)] = {
    val readSingle = 
      (
        (__ \ "id" \ "chat_id").read(StringReads) and
        (__ \ "fallback_name").read(StringReads)
      )(Participant)

    (
      (__)(0).read(readSingle) and
      (__)(1).read(readSingle)
    ).tupled   
  }

 /**
  * (This reader has to go last in this oject
  * because something about macros, otherwise it throws an NPE)
  *
  * This is the Read that can be applied to the top
  * level hangouts object
  * like so `hangoutsJson.validate(readConversations)`
  */
  val readConversations: Reads[List[Conversation]] = 
    (__ \ "conversation_state").read { 
      list[Conversation] {
        (
          (__ \ "conversation_state" \ "conversation" \ "participant_data").read(Reads.optionNoError(readParticipants)) and
          (__ \ "conversation_state" \ "event").read(readEvents)
        )(Conversation)
      }
    }
}

