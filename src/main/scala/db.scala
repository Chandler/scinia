package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.lifted.TableQuery
import scala.slick.profile.BasicProfile
import scala.slick.driver.SQLiteDriver.DDL
import slick.driver.SQLiteDriver.backend.DatabaseDef
import slick.driver.SQLiteDriver.backend.SessionDef

object DB {

  // def buildTables(tables: Seq[DDL])(implicit session: SessionDef) : Unit =
  //   tables.foreach(t => t.create)
  
  def connect(path: String) =
    Database.forURL(
      "jdbc:sqlite:%s".format(path), 
      driver = "org.sqlite.JDBC"
    )
}
