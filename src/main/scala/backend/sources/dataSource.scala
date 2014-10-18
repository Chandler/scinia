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
import com.scinia.BuildPreprocessor.Preprocessor
import com.scinia.Config.sourcePath
/**
 * A DataSource is a unqiue set of data that we want the system to be 
 * able to process. DataSource defines how the data goes from the source
 * file to the database. 
 * 
 * You can have multiple DataSources of the same data format. e.g if you have two iphones 
 * you would have two DataSources of different names representing each 
 * iphone's backup file
 *
 * Each DataSource gets a folder in /dropZone and optionally a folder in /runs
 * 
 * Here's what a DataSource looks like
 *
 * object GoogleVoiceSource extends DataSource {
 *   override val name          = "googleVoice"
 *   override val table         = Tables.messages
 *   override val useDropZone   = true
 *   override val loader        = GoogleVoiceLoader
 *   override val preprocessor  = Preprocessors.googleVoice
 *   ...
 * For examples of a bunch more DataSources, see ChandlersConfig.scala
 */
trait DataSource {
  import DataSource._

  // Name of the data source, e.g. Google Voice
  val name:         String

  // Indicate whether or not this data source should be processed
  // using the run history directory structure
  //
  // If this is set to false you're saying you want to be able to process
  // data on an ad-hoc basis, all output will be written to sourcePath/tmp
  // 
  // If your datasource is historical and never going to be updated
  // set this to false
  val useRunProcessing:  Boolean

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

  def apply(
    filePath: String,
    db: Database
  ): Unit = {
    val process = (srcFile: File, runDir: File) =>
      preprocessor(srcFile, runDir).flatMap { preprocessedFile => 
        loadAndStore(preprocessedFile, db)
      }
    
    if(useRunProcessing) {
      setupRun(filePath) { process }
    } else {
      processWithoutRun(filePath) { process }
    }
 }

  def setupRun(filePath: String)(doRun: Processor): Unit = {
    val runDir  = new File(s"$sourcePath/$runs/$name/" + now("dd-MMM-yy HH-mm-ss"))
    (for {
      _       <- Try(forceMkdir(runDir))
      srcFile <- moveToRunDir(filePath, runDir)
      _       <- checkForDuplicate(srcFile)
      _       <- doRun(srcFile, runDir)
      _       <- symlinkLatest(name, srcFile)
    } yield ()) match {
      case Success(_)  => writeSuccessFile(runDir)
      case Failure(ex) => writeFailureFile(runDir, ex)
    }
  }

  def processWithoutRun(filePath: String)(doRun: Processor): Unit = {
    val tmpDir =  new File(s"$sourcePath/$tmp/$name-" + now())
    doRun(new File(filePath), tmpDir) match {
      case Success(_)  => writeSuccessFile(tmpDir)
      case Failure(ex) => writeFailureFile(tmpDir, ex) 
    }
  }
}

object DataSource {
  import Path._

  type Processor    = (File, File) => Try[Unit]
  type LoadAndStore = (File, Database) => Try[Unit]

  val NoPreprocessing: Preprocessor = (infile, _) => Try(infile)
  val DoNothing:       LoadAndStore = (f, _) => Try(Unit)

  val runs        = "runs"
  val dropZone    = "dropZone"
  val latest      = "latest"
  val tmp         = "tmp"
  val logs        = "logs"
  val dirNames    = List(runs, dropZone, latest, tmp, logs)

  def now(fmt: String = "dd-MMM-yy HH:mm:ss") = new DateTime().toString(fmt)
  
  /**
   * Construct the base directory structure for Scinia
   * based on the sources registered in Config.registeredSources
   *   /runs  # directory which contains the history of all runs
   *   /dropZone # monitored directory where you can place new source files to be processed
   *   /latest   # symlinks to the src of latest successful run
   */
   def setupDirs(rootPath: String) {
     val fixedDirs = dirNames.map { name => new File(rootPath, name) }            
     
     val dropZoneDirs =
       Config.registeredSources
         .map { source => new File(fixedDirs(1), source.name) }
     
     val runDirs =
       Config.registeredSources
         .filter(_.useRunProcessing)
         .map { source => new File(fixedDirs(0), source.name) }

     (fixedDirs ++ dropZoneDirs ++ runDirs).foreach(forceMkdir)
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
      val srcPath = new File(s"$sourcePath/$latest/$name").toPath
      Files.deleteIfExists(srcPath)
      Files.createSymbolicLink(
        srcPath,
        target.toPath
      )
    }

  def writeFailureFile(runDir: File, ex: Throwable): Unit = 
    Path.fromString(runDir.getAbsolutePath() + "/FAILURE")
      .write(now() + ex.toString)// + exception

  def writeSuccessFile(runDir: File): Unit = 
    Path.fromString(runDir.getAbsolutePath() + "/SUCCESS")
      .write(now())
  
  // TODO: make this
  def checkForDuplicate(file: File): Try[Unit] = Try(Unit)
}

