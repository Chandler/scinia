package com.scinia

import com.google.common.io.Files.{move, copy}
import com.scinia.Tables.MessagesTable
import java.io.File
import java.nio.file.Files
import java.util.Date
import Message._
import org.apache.commons.io.FileUtils.forceMkdir
import org.joda.time.DateTime
import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.lifted.TableQuery
import scala.util.{Failure, Success, Try}
import scalax.file.Path

// TODO aug 2015, file is getting pretty stringy and inconsistant when it comes
// to passing around strings vs files, need to work on that. Also trying to undo some
// nasty global config stuff


/**
 * A DataSource is a unqiue set of data that we want the system to be 
 * able to process. DataSource defines how the data goes from the source
 * file to the database. 
 * 
 * You can have multiple DataSources of the same data format. e.g if you have two iphones 
 * you would register two DataSources of different names representing each 
 * iphone's backup file
 *
 * Each DataSource gets a folder in /dropZone and optionally a folder in /runs
 * 
 * See commonSources.scala for many examples of configured DataSources
 */
trait DataSource extends Log {
  import DataSource._

  // Name of the data source, e.g. Google Voice
  val name: String

  // relative path to the root folder of the scinia project
  val projectPath: String

  // Indicate whether or not this data source should be processed
  // using the run history directory structure
  //
  // If this is set to false you're saying you only want to be able to process
  // data on an ad-hoc basis
  // 
  // If your datasource is historical and never going to be updated
  // set this to false
  val manageRuns: Boolean

  // See [[Loader]], this is what converts a file full of records
  // into an in-memory Sequence of some internal representation of those records
  // e.g most message loaders return Seq[ChatRecord]
  val loader: Loader[_]

  // The database table that this data should be loaded into 
  val table: TableQuery[_]

  // See [[Preprocessor]] an optional preprocessor that allows you to
  // transform your data with any bash command before you start the loading
  // process. e.g. if your data is significantly easier to process in python
  // than scala you probably want a python processing step
  val preprocessor: Preprocessor = NoPreprocessing
  
  // A method to take the output of this sources loader and 
  // store it in this sources table.
  val loadAndStore: Processor = DoNothing

  /**
   * Entry point for starting a run. At the highlest level a run takes a 
   * file and loads that file into the database. If manageRuns=true 
   * the run will only take place if it contains new data, and the
   * details of the run will be logged in a runDir.
   *
   * the details of unmanaged runs can be found in /tmp
   */
  def apply(
    filePath: String,
    db: Database
  ): Unit = {
    Log(s"Begining processing for $filePath")
    val run: Run = (srcFile, runDir) =>
      preprocessor(srcFile, runDir).flatMap { preprocessedFile => 
        loadAndStore(preprocessedFile, db)
      }
    
    if(manageRuns) {
      managedRun(filePath) { run }
    } else {
      adhocRun(filePath) { run }
    }
 }

  /**
   * For each managed run, a runDir will be placed in /runs.
   * If a run suceeds the runDir will contain:
   *   - A copy of the source file that was processed
   *   - Any intermediate data produced by a preprocessing step
   *   - A SUCCESS file
   * If a run fails it will contain a FAILURE file which contains
   * the failure reason.
   */
  def managedRun(filePath: String)(doRun: Run): Unit = {
    val runDir  = newRunDir
    
    Log("Starting managed run: " + runDir.toString)
    
    (for {
      _       <- Try(forceMkdir(runDir))
      srcFile <- moveSourceFile(projectPath, filePath, runDir)
      _       <- checkForDuplicate(srcFile)
      _       <- doRun(srcFile, runDir)
      _       <- symlinkLatest(projectPath, name, srcFile)
    } yield ()) match {
      case Success(_)  => writeSuccessFile(runDir)
      case Failure(ex) => writeFailureFile(runDir, ex)
    }
  }

  /**
   * For adHoc runs, the source data is parsed
   * and loaded directly into the db without any checks
   * Details of the adHoc run can be found in /tmp
   */
  def adhocRun(filePath: String)(doRun: Run): Unit = {
    val tmpDir = newTmpDir
    
    Log("starting adhoc run")
    Log("tmpDir: " + tmpDir.toString)
    
    doRun(new File(filePath), tmpDir) match {
      case Success(_)  => 
        moveSourceFile(projectPath, filePath, tmpDir)
        writeSuccessFile(tmpDir)
      case Failure(ex) => 
        writeFailureFile(tmpDir, ex) 
    }
  }

  def newTmpDir = new File(s"$projectPath/$tmp/$name-" + now("dd-MMM-yy-HH-mm-ss"))
  def newRunDir = new File(s"$projectPath/$runs/$name/" + now("dd-MMM-yy-HH-mm-ss"))

}

/**
 * A DataSource for message type datasources.
 */
trait MessageSource extends DataSource {
  import DataSource._
  
  override val loader: Loader[ChatRecord]

  override val table: TableQuery[MessagesTable] = Tables.messages

  /**
   * 1. get ChatRecords (generic chat object) from the loader
   * 2. convert them into Messages (model object)
   * 3. store in database
   */
  override val loadAndStore: Processor = (file: File, db: Database) =>
    Try {
      db.withSession { implicit session =>
        val records = loader(file.toString).flatMap(toMessage)
        Log("about to store " + records.size + " ChatRecords into the messages table")
        table ++= records
        Log("completed db store")
      }
    }
}

/**
 * Helpers, constants and types for DataSource
 * Contains a lot of file manipulation methods
 */
object DataSource extends Log {
  import Path._
  
  // a function that represents the work of a run. Pass one of these
  // to setupRun, which will call Run(srcFile, runDir) when it's ready
  type Run = (File, File) => Try[Unit]

  // A function that takes input/output files and returns
  // the output, so you can do more processing on it.
  type Preprocessor = (File, File) => Try[File]

  // A function that takes input/output files
  // and stores them somewhere, returning Nothing
  type Processor = (File, Database) => Try[Unit]

  // default processing methods that don't do anything
  val NoPreprocessing: Preprocessor = (infile, _) => Try(infile)
  val DoNothing:       Processor    = (f, _) => Try(Unit)

  val runs        = "runs"
  val dropZone    = "dropZone"
  val latest      = "latest"
  val tmp         = "tmp"
  val parent      = "-scinia"

  // don't put anything in this array that happens to be the same name as other folders
  // in your scinia working dir, unless you didn't want those folders. Ok?
  val dirNames = List(runs, dropZone, latest, tmp)

  def now(fmt: String = "dd-MMM-yy HH:mm:ss") = new DateTime().toString(fmt)
  
  /**
   * Construct the base directory structure for Scinia
   * based on the sources registered in Config.registeredSources
   *   /runs  # directory which contains the history of all runs
   *   /dropZone # monitored directory where you can place new source files to be processed
   *   /latest   # symlinks to the src of latest successful run
   */
   def setupDirs(config: SciniaConfig, registeredSources: List[DataSource]) {
     val projectPath = config.projectPath

     val rootDir = new File(projectPath)

     val fixedDirs = dirNames.map { name => new File(projectPath, name) }            
     
     val dropZoneDirs =
       registeredSources
         .map { source => new File(fixedDirs(1), source.name) }
     
     val runDirs =
       registeredSources
         .filter(_.manageRuns)
         .map { source => new File(fixedDirs(0), source.name) }

     (Seq(rootDir) ++ fixedDirs ++ dropZoneDirs ++ runDirs).foreach(forceMkdir)
   }

   /** be careful, deletes things */
  def cleanDirs(config: SciniaConfig) = {
    import org.apache.commons.io.FileUtils
    val projectPath = config.projectPath

    // delete inner dirs explicitly for clarity
    val dirsToDelete =
      dirNames.map { name =>
        s"$projectPath/$name"
      } :+ projectPath

    dirsToDelete.foreach { dir =>
      Log("deleting " + dir)
      FileUtils.deleteDirectory(new File(dir))
    }
  }

  /**
   * If the source file comes from a dropZone folder, move it
   * Otherwise, if for some reason we're processing a 3rd party file
   * Just copy it. We wouldn't want to move an itunes backup ect.
   */
  def moveSourceFile(projectPath: String, filePath: String, destDir: File): Try[File] =
    Try {
      val fileToMove = new File(filePath)
      val targetFile = new File(destDir, "src")
      
      if(IsInDropZone(projectPath, fileToMove)) {
        move(fileToMove, targetFile)
        Log("move file")
      } else {
        copy(fileToMove, targetFile)
        Log("copied file")
      }

      Log(fileToMove.toString + " => " + targetFile.toString)
      targetFile
    }

  /**
   * Create the symlink /latest/<name> directory that points towards
   * the source file for the last successful run
   */
  def symlinkLatest(projectPath: String, name: String, target: File): Try[Unit] =
    Try {
      val srcPath = new File(s"$projectPath/$latest/$name").toPath
      Files.deleteIfExists(srcPath)
      Files.createSymbolicLink(
        srcPath,
        target.toPath
      )
      Log("created symlink")
      Log(srcPath + " => " + target.toPath)
    }

  def writeFailureFile(runDir: File, ex: Throwable): Unit = {
    Log("Run failed: ", ex)
    Path.fromString(runDir.getAbsolutePath() + "/FAILURE")
      .write(now() + ex.toString)
  }

  def writeSuccessFile(runDir: File): Unit = {
    Log("Run succeeded")
    Path.fromString(runDir.getAbsolutePath() + "/SUCCESS")
      .write(now())
  }
  
  // TODO: make this
  def checkForDuplicate(file: File): Try[Unit] = Try(Unit)

  def IsInDropZone(projectPath: String, file: File): Boolean = file.getAbsolutePath().startsWith(s"$projectPath/$dropZone")
}

