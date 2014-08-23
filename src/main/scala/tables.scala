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

    //this <> (Message.apply _).tupled stuff is black magic
    def *  = (toId, fromId, content, utcReceivedTime, sourceId) <> ((Message.apply _).tupled, Message.unapply)
  }

  val contactsQuery = TableQuery[ContactsTable]

  class ContactsTable(tag: Tag) extends Table[Contact](tag, "contacts") {
    def id         = column[Int]("id", O.AutoInc)
    def identifier = column[String]("identifier")
    def firstName  = column[String]("firstName", O.Nullable)
    def lastName   = column[String]("lastName", O.Nullable)
    def realId     = column[Int]("realId", O.Nullable)
    def sourceId   = column[Int]("sourceId")
    
    //primary key was throwing an error so using unique index
    // def idx        = index("idx_a", (identifier, sourceId), unique = true)

    //.? means the field is an option
    def *  = (id.?, identifier, firstName.?, lastName.?, realId.?, sourceId) <> ((Contact.apply _).tupled, Contact.unapply)
  }

  val ddls: Seq[DDL] = Seq(messagesQuery.ddl, contactsQuery.ddl)
}
