package com.scinia

import com.scinia.DataSource.Preprocessor
import java.io.File
import scala.sys.process._
import scala.util.Try

object BuildPreprocessor {
  type Preprocessor = (File, File) => Try[File]

  def apply(
    command: (String,String) => Seq[String]
  ): Preprocessor = (srcFile, outputDir) => {
    val outFile = new File(outputDir, "/processed")
    Try {
      (command(srcFile.toString, outFile.toString)).! match {
        case 0 => outFile
        case _ => throw new Exception("Preprocessing failed") 
      }
    }
  }
}

object Preprocessors {
 
 /**
  * The google voice source file is HTML so It's easier
  * To use a python preprocessor with the beautiful soup 
  * library to parse it
  */
  val googleVoice =
    BuildPreprocessor( (input: String, output: String) => 
      Seq(
        "python", 
        "src/main/python/backend/preprocessors/voiceTransformer.py",
        input,
        output
      )
    )

 /**
  * Use an existing open source iphone database extractor
  * To convert an iphone sms backup database into JSON
  */
  val iphoneBackup =
    BuildPreprocessor( (input: String, output: String) => 
      Seq(
        "python", 
        "tools/sms-backup.py",
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
