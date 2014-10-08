package com.scinia

import com.scinia.Tables.MessagesTable
import org.apache.commons.io.FileUtils._
import java.io.File

import scala.slick.lifted.TableQuery


object DataSource {
 
 /**
  * Construct the base directory structure for Scinia
  * based on the sources registered in Config.registeredSources
  */
  def setupDirs(rootPath: String) {

    val fixedDirs = 
      List("dropZone", "archive", "latest")
        .map { name => new File(rootPath, name) }
                          
    val dropZoneDirs = 
      Config.registeredSources
        .filter(_.useDropZone)
        .map { source => new File(fixedDirs(0), source.name) }

    val archiveDirs = 
      Config.registeredSources
        .map { source => new File(fixedDirs(1), source.name) }

    (fixedDirs ++ dropZoneDirs ++ archiveDirs).foreach(forceMkdir)
  }
}



trait DataSource {
  val name:        String  
  val useDropZone: Boolean
  val loader:      Loader
  val table:       TableQuery[_]

  // def process(filePath: String, db: DB): Unit = {

  //   // move the file into the archive
  //   // check if the SHA matches the latest
  //   // if it does, write an error
  //   // if it doesn't begin processing
  //   // write out errors to the archive
  //   db.withSession { implicit session =>
  //     table ++= loader(filePath).flatMap(toMessage)
  //   }
  // }
}

trait MessageSource extends DataSource {
  override val table = Tables.messages
}


