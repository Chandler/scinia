package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.driver.SQLiteDriver.DDL

object Tables {
  val messagesQuery = TableQuery[MessagesTable]

  class MessagesTable(tag: Tag) extends Table[Message](tag, "messages") {
    def toId            = column[Int]("toId")
    def fromId          = column[Int]("fromId")
    def content         = column[String]("content")
    def utcReceivedTime = column[String]("utcReceivedTime")
    def sourceId        = column[Int]("sourceId")

    //this <> (Message.apply _).tupled stuff is black magic, TODO what is going on here?
    def *  = (toId, fromId, content, utcReceivedTime, sourceId) <> ((Message.apply _).tupled, Message.unapply)
  }

  val contactsQuery = TableQuery[ContactsTable]

  class ContactsTable(tag: Tag) extends Table[Contact](tag, "contacts") {
    def id         = column[Int]("id")
    def identifier = column[String]("identifier")
    def firstName  = column[String]("firstName")
    def lastName   = column[String]("lastName")
    def realId     = column[Int]("realId")
    def sourceID   = column[Int]("sourceID")
    def *  = (id, identifier, firstName, lastName, realId, sourceID) <> ((Contact.apply _).tupled, Contact.unapply)
  }

  val ddls: Seq[DDL] = Seq(messagesQuery.ddl, contactsQuery.ddl)
}
