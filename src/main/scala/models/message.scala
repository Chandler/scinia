package com.scinia

import Source._
import scala.slick.driver.SQLiteDriver.simple._

case class Message(
  val toId:            Int,
  val fromId:          Int,
  val content:         String,
  val utcReceivedTime: String,
  val sourceId:        Int
)

object Message {
  def toMessage(record: ChatRecord)(implicit session: Session): Option[Message] =
    for {
      toId   <- contacts.findOrCreate(record.to, record.source).id
      fromId <- contacts.findOrCreate(record.from, record.source).id
    } yield {
      println(record.date + " " + record.text)
      Message(toId, fromId, record.text, record.date, record.source.id)
    }
}
