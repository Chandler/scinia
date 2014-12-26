package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.driver.SQLiteDriver.DDL

// This file requires an insane amount of typing to setup a table
// maybe I should learn macros
object Tables {
  val messages = TableQuery[MessagesTable]

  class MessagesTable(tag: Tag) extends Table[Message](tag, "messages") {
    def toId            = column[Int]("toId")
    def fromId          = column[Int]("fromId")
    def content         = column[String]("content")
    def utcReceivedTime = column[String]("utcReceivedTime")
    def sourceId        = column[Int]("sourceId")


    //primary key was throwing an error so using unique index
    def idx             = index("idx_a", (toId, fromId, content, utcReceivedTime), unique = true)  

    //this <> (Message.apply _).tupled stuff is black magic
    def *  = (toId, fromId, content, utcReceivedTime, sourceId) <> ((Message.apply _).tupled, Message.unapply)
  }

  val contacts = TableQuery[ContactsTable]

  class ContactsTable(tag: Tag) extends Table[Contact](tag, "contacts") {
    def id         = column[Int]("id", O.AutoInc)
    def identifier = column[String]("identifier")
    def firstName  = column[String]("firstName", O.Nullable)
    def lastName   = column[String]("lastName", O.Nullable)
    def realId     = column[Int]("realId", O.Nullable)
    def sourceId   = column[Int]("sourceId")
    
    //primary key was throwing an error so using unique index
    def idx        = index("idx_a", (identifier, sourceId), unique = true)

    //.? means the field is an option
    def *  = (id.?, identifier, firstName.?, lastName.?, realId.?, sourceId) <> ((Contact.apply _).tupled, Contact.unapply)
  }


  val songPlays = TableQuery[SongPlaysTable]

  class SongPlaysTable(tag: Tag) extends Table[SongPlay](tag, "songPlays") {
    def time         = column[String]("time")
    def trackName    = column[String]("trackName")
    def trackMbid    = column[String]("trackMbid", O.Nullable)
    def artistName   = column[String]("artistName")
    def artistMbid   = column[String]("artistMbid", O.Nullable)
    def albumName    = column[String]("albumName", O.Nullable)
    def albumMbid    = column[String]("albumMbid", O.Nullable)
    def sourceId     = column[Int]("sourceId")
    
    //.? means the field is an option
    def *  = (time, trackName, trackMbid.?, artistName, artistMbid.?, albumName.?, albumMbid.?, sourceId) <> ((SongPlay.apply _).tupled, SongPlay.unapply)
  }

  val ddls: Seq[DDL] = Seq(messages.ddl, contacts.ddl, songPlays.ddl)
}
