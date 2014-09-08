package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import Source._
class MultipleContactsFoundException extends Exception

case class Contact(
  val id:         Option[Int] = None,
  val identifier: String,
  val firstName:  Option[String] = None,
  val lastName:   Option[String] = None,
  val realId:     Option[Int]    = None,
  val sourceId:   Int
)

// Completely static methods go in this companion object.
object Contact {

}


// Helper methods related to queries go into the TableQuery companion object
object contacts extends TableQuery(new Tables.ContactsTable(_)) {
  val findByIdentifier = this.findBy(_.identifier)

  def findOrCreate(
    identifier: String,
    source: Source
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
