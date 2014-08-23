package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import Source._

case class Contact(
  val id:         Option[Int] = None,
  val identifier: String,
  val firstName:  Option[String] = None,
  val lastName:   Option[String] = None,
  val realId:     Option[Int]    = None,
  val sourceId:   Int
)

// object query extends TableQuery(new Tables.ContactsTable(_)) {
//   val findByIdentifier = this.findBy(_.identifier)
  
//   def findOrCreate(identifier: String, source: Source) {

//     val contact = Contact(
//       identifier = identifier,
//       sourceId   = source.id
//     )

//     (this returning this.map(_.id)
//        into ((user,id) => contact.copy(id=Some(id)))
//     )// += contact
//   }
// }

object Contact {

  def findOrCreate(identifier: String, source: Source)(implicit session: Session): Option[Contact] = {
    val contact = Contact(
      identifier = identifier,
      sourceId   = source.id
    )
    Tables.contactsQuery += contact
    Some(contact)
  }
}
