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
import java.nio.file.Files
import scalax.file.Path
/**
 * A DataSource is a unqiue set of data that we want the system to be 
 * able to process. DataSource defines how the data goes from the source
 * file to the database. 
 * 
 * You can have multiple DataSources of the same data format. e.g if you have two iphones 
 * you would have two DataSources of different names representing each 
 * iphone's backup file
 *
 * Each DataSource gets a folder in /archive and optionally a folder in /dropZone
 * 
 * Here's what a DataSource looks like
 *
 * object GoogleVoiceSource extends DataSource {
 *   override val name          = "googleVoice"
 *   override val table         = Tables.messages
 *   override val useDropZone   = true
 *   override val loader        = GoogleVoiceLoader
 *   override val preprocessor  = Preprocessors.googleVoice
 *   override val loadAndStore  = (file: File, db: Database) =>
 *      Try {
 *         db.withSession { implicit session =>
 *          table ++= loader(file.toString).flatMap(toMessage)
 *        }
 *      }
 *   }
 * For examples of a bunch more DataSources, see ChandlersConfig.scala
 */
trait DataSource {
  import DataSource._

  // Name of the data source, e.g. Google Voice
  val name:         String

  // Indicate whether or not this data source should be have
  // a dropZone folder where new files can be placed for processing
  // If your datasource is historical and never going to be updated
  // set this to false
  val useDropZone:  Boolean

  // See [[Loader]], this is what converts a file full of records
  // into an in-memory Sequence of some internal representation of those records
  // e.g most message loaders return Seq[ChatRecord]
  val loader:       Loader

  // The database table that this data should be loaded into 
  val table:        TableQuery[_]

  // See [[Preprocessor]] an optional preprocessor that allows you to
  // transform your data with any bash command before you start the loading
  // process. e.g. if your data is significantly easier to process in python
  // than scala you probably want a python processing step
  val preprocessor: Preprocessor = NoPreprocessing
  
  // A method to take the output of this sources loader and 
  // store it in this sources table.
  val loadAndStore: LoadAndStore = DoNothing

  // Given a file and a database this method will create a run directory
  // in /archive/<source.name> that logs the attempt to parse the file and load its
  // contents into the database
  def apply(
    filePath: String,
    db: Database
  ): Unit = {
    val results = (
     for {
       runDir           <- createRunDir(name)
       srcFile          <- moveToRunDir(filePath, runDir)
       _                <- checkForDuplicate(srcFile)
       preprocessedFile <- preprocessor(srcFile, runDir)
       _                <- loadAndStore(preprocessedFile, db)
       _                <- symlinkLatest(name, srcFile)
       _                <- writeSuccessFile(runDir) 
     } yield() )

   results match {
      case Failure(ex) =>
        val failedPath = runDir.getAbsolutePath() + "/FAILED"
        Path.fromString(failedPath).write(ex.toString)
    }
  }
}

trait MessageSource extends DataSource {
  override val table = Tables.messages
  override val loadAndStore  = (file: File, db: Database) =>
   Try {
      db.withSession { implicit session =>
       table ++= loader(file.toString).flatMap(toMessage)
     }
     "success"
   }
}

object DataSource {
  type Preprocessor = (File, File) => Try[File]
  type LoadAndStore = (File, Database) => Try[String]

  val NoPreprocessing: Preprocessor = (infile, _) => Try(infile)
  val DoNothing:       LoadAndStore = (f, _) => Try("did nothing!")

  val archive     = "archive"
  val dropZone    = "dropZone"
  val latest      = "latest"
  val dirNames    = List(archive, dropZone, latest)
  val archivePath = Config.sourcePath + "/" + archive
  
  /**
   * Construct the base directory structure for Scinia
   * based on the sources registered in Config.registeredSources
   *  /path/to/scinia/fs
   *   /archive  # directory which contains the history of all runs
   *   /dropZone # monitored directory where you can place new source files to be processed
   *   /latest   # symlinks to the src of latest successful run
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

  def symlinkLatest(name: String, target: File): Try[Unit] =
    Try {
      val src = new File(Config.sourcePath + "/" + latest + "/" + name)
      Files.createSymbolicLink(src.toPath, target.toPath)
    }

  def writeSuccessFile(runDir: File): Try[Unit] = 
    Try {
      val successPath = runDir.getAbsolutePath() + "/SUCCESS"
      Path.fromString(successPath).write(new DateTime().toString)
    }

  def checkForDuplicate(file: File): Try[Unit] = Try(Unit)
}


