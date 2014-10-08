package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import com.scinia.LoaderId._

class MultipleContactsFoundException extends Exception

case class Contact(
  id:         Option[Int]    = None,
  identifier: String,
  firstName:  Option[String] = None,
  lastName:   Option[String] = None,
  realId:     Option[Int]    = None,
  sourceId:   Int
)

// Helper methods related to queries go into the TableQuery companion object
object contacts extends TableQuery(new Tables.ContactsTable(_)) {
  val findByIdentifier = this.findBy(_.identifier)

  def findOrCreate(
    identifier: String,
    source: LoaderId
  )(implicit session: Session): Contact = {
    this.filter( c => 
      (
        c.identifier === identifier &&
        c.sourceId   === source.id
      )
    ).list match {
      // Nothing returned, so create a new contact
      case Nil => { 
        val contact = Contact(
          identifier = identifier,
          sourceId   = source.id
        )
        
        // similar to this += contact
        // except this returns the contact
        // with the auto inc id
        (
          this returning this.map(_.id)
          into ((_,id) => contact.copy(id = Some(id)))
        ) += contact
      }
      // found 1 contact
      case list if (list.length == 1) => list.head      
      case _ => throw new MultipleContactsFoundException()
    }
  }
}
