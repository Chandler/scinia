package com.scinia

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.lifted.TableQuery
import scala.slick.profile.BasicProfile
import scala.slick.driver.SQLiteDriver.DDL
import slick.driver.SQLiteDriver.backend.DatabaseDef
// import slick.driver.SQLiteDriver.backend.SessionDef

//the only thing that actually talks to the db..when it's passed a session of course
class DbHelper(implicit session: Session) {
  import DbHelper._

  def execute[T, C[_]](query: Query[T,_, C]) = query.list

  def createTables(ddls: Seq[DDL]): Unit =
    ddls.foreach(d => trySql(d.create))

  def dropTables(ddls: Seq[DDL]): Unit =
    ddls.foreach(d => trySql(d.drop)) 

  def recreateTables(ddls: Seq[DDL]): Unit = {
    dropTables(ddls)
    createTables(ddls)
  }
}


object DbHelper {
  def apply()(implicit session: Session) = new DbHelper()(session)

  def connect(path: String) =
    Database.forURL(
      "jdbc:sqlite:%s".format(path), 
      driver = "org.sqlite.JDBC"
    )

  // run a sql side effect
  def trySql[A](a: => A): Unit =
    try(a) catch {
      case e: java.sql.SQLException => println(e.toString())
    }
}
