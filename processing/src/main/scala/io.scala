package com.scinia
import java.io.File

import io.Source
import scala.collection.JavaConversions._

//   class DelimitedLineIterator(delimiter: Char = '\n') extends Source$LineIterator {
//     override def isNewline(ch: Char) = ch == delimiter
//   }


// class IOSource extends io.Source {
//   // class DelimitedLineIterator(delimiter: Char = '\n') extends LineIterator {
//   //   override def isNewline(ch: Char) = ch == delimiter
//   // }

//   // def getLines(delimiter: Char = '\n'): Iterator[String]
//   //   = new DelimitedLineIterator(delimiter)
// }

object IOSource extends {
  /**
   * pretty slick function from stack overflow
   * http://stackoverflow.com/a/7264833/67166
   */
  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree) 
           else Stream.empty)

  def getFileTreePaths(path: String): Stream[String] = 
    getFileTree(new File(path)).map(_.getName)
}


