import com.scinia.DataSource.Preprocessor
import java.io.File
import scala.sys.process._
import scala.util.Try
package com.scinia

object BuildPreprocessor {
  def apply(
    command: Seq[String]
  ): Preprocessor = (srcFile, outputDir) => {
    val outFile = new File(outputDir, "/processed")
    val args = Seq(srcFile.toString, outFile.toString)
    Try {
      (command ++ args).! match {
        case 0 => outFile
        case _ => throw new Exception("Preprocessing failed") 
      }
    }
  }
}
