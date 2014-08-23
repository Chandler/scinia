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
  def toMessage(record: SmsBackupRecord)(implicit session: Session): Option[Message] =
    for {
      toContact   <- Contact.findOrCreate(record.to, SMS)
      fromContact <- Contact.findOrCreate(record.from, SMS)
    } yield {
      Message(0, 0, record.text, record.date, SMS.id)
    }
}
