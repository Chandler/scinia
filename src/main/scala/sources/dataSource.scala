package com.scinia

import com.scinia.Tables.MessagesTable
import org.apache.commons.io.FileUtils.forceMkdir
import com.google.common.io.Files.move
import java.io.File
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import org.joda.time.DateTime
import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.lifted.TableQuery
import java.util.Date


trait DataSource {
  import DataSource._
  val name:         String
  val useDropZone:  Boolean
  val loader:       Loader
  val table:        TableQuery[_]
  val preprocessor: Preprocessor = NoPreprocessing
  val loadAndStore: LoadAndStore = DoNothing

  def apply(
    filePath: String,
    db: Database
  ): Unit = {
    val processingResults = (
     for {
       runDir           <- createRunDir(name)
       srcFile          <- moveToRunDir(filePath, runDir)
       _                <- checkForDuplicate(srcFile)
       preprocessedFile <- preprocessor(srcFile, runDir)
       summary          <- loadAndStore(preprocessedFile, db)
     } yield(summary))

   processingResults match {
      case Failure(ex) =>
        throw ex
        // println(ex)
      
      case Success(stats) =>
        println(stats)
    }
  }
}

trait MessageSource extends DataSource {
  override val table = Tables.messages
}

object DataSource {
  type Preprocessor = (File, File) => Try[File]
  type LoadAndStore = (File, Database) => Try[String]

  val NoPreprocessing: Preprocessor = (infile, _) => Try(infile)
  val DoNothing: LoadAndStore = (f, _) => Try("did nothing!")

  val archive     = "archive"
  val dropZone    = "dropZone"
  val latest      = "latest"
  val dirNames    = List(archive, dropZone, latest)
  val archivePath = Config.sourcePath + "/" + archive
  
  /**
   * Construct the base directory structure for Scinia
   * based on the sources registered in Config.registeredSources
   */
   def setupDirs(rootPath: String) {

     val fixedDirs = dirNames.map { name => new File(rootPath, name) }
                          
     val dropZoneDirs =
       Config.registeredSources
         .filter(_.useDropZone)
         .map { source => new File(fixedDirs(0), source.name) }

     val archiveDirs =
       Config.registeredSources
         .map { source => new File(fixedDirs(1), source.name) }

     (fixedDirs ++ dropZoneDirs ++ archiveDirs).foreach(forceMkdir)
   }

  /**
   * 
   */
   def createRunDir(name: String): Try[File] =
     Try {
       val date    = new DateTime().toString("dd-MMM-yy HH-mm-ss")
       val runDir  = new File(Config.sourcePath + "/" + archive + "/" + name + "/" + date)
       forceMkdir(runDir)
       runDir
     }

   def moveToRunDir(filePath: String, destDir: File): Try[File] =
     Try {
       val fileToMove = new File(filePath)
       val targetFile = new File(destDir, "src")
       move(fileToMove, targetFile)
       targetFile
     }

   def checkForDuplicate(file: File): Try[Boolean] = Try(true)
}


