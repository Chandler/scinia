package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import Source._

case class Contact(
  val id:         Int,
  val identifier: String,
  val firstName:  String,
  val lastName:   String,
  val realId:     Int,
  val sourceID:   Int
)

object Contact {
  def findOrCreate(identifier: String, source: Source)(implicit session: Session): Option[Contact] = {
    Some(Contact(1,"A","a","a",2,3))
  }
}
