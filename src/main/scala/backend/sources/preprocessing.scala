package com.scinia

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
