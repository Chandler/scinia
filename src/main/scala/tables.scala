package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.driver.SQLiteDriver.DDL

object SciniaTables {
  val messages = TableQuery[Messages]
  val ddls: Seq[DDL] = Seq(messages.ddl)
}

class Messages(tag: Tag) extends Table[(Int, Int, String, Long, Int)](tag, "messages") {
  def toId            = column[Int]("toId")
  def fromId          = column[Int]("fromId")
  def content         = column[String]("content")
  def utcReceivedTime = column[Long]("utcReceivedTime")
  def sourceId        = column[Int]("sourceId")
  def *  = (toId, fromId, content, utcReceivedTime, sourceId)
}
