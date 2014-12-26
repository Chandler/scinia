package com.scinia

import com.scinia.DataSource.Preprocessor
import java.io.File
import scala.sys.process._
import scala.util.Try
import scalax.file.Path

object BuildPreprocessor extends Log {
  type Preprocessor = (File, File) => Try[File]

  def apply(
    buildCommand: (String,String) => Seq[String]
  ): Preprocessor = (srcFile, outputDir) => {

    // TODO don't mix java.io.File and scala.file.Path
    val outFile = Path.fromString(outputDir + "/processed")
    
    // TODO hacky fix - we need to create the file from scala
    // because sometimes the python preprocessors can't create the /processed file
    // suspect permissions issues
    outFile.write("")
    val command = buildCommand(srcFile.toString, outFile.path)
    Log(command.mkString(" "))
    Try {
      (command).! match {
        case 0 => new File(outputDir, "/processed")
        case _ => throw new Exception("Preprocessing failed") 
      }
    }
  }

  def simplePythonPreprocessor(filePath: String) =
    BuildPreprocessor((input: String, output: String) => 
      Seq("python", file, input, output))
}

object Preprocessors {
 
 /**
  * The google voice source file is HTML so It's easier
  * To use a python preprocessor with the beautiful soup 
  * library to parse it
  */
  val googleVoice = simplePythonPreprocessor("src/main/python/preprocessors/voiceTransformer.py")

  val gchat = simplePythonPreprocessor("src/main/python/preprocessors/gchatTransformer.py")

 /**
  * Use an existing open source iphone database extractor
  * To convert an iphone sms backup database into JSON
  */
  val iphoneBackup =
    BuildPreprocessor( (input: String, output: String) => 
      Seq(
        "python", 
        "src/main/python/3rdparty/sms-backup.py",
        "--format",
        "json",
        "--date-format",
        "'%Y-%m-%dT%H:%M:%S'",
        "--input",
        input,
        "--output",
        output
      )
    )
}
