package com.scinia

import com.scinia.Tables._
// import com.scinia.CommonReads._


import java.io._
import com.github.tototoshi.csv._
import scala.util.Try
import scala.util.control.Breaks._
import scala.io.{ Source => IOSource }
import scala.util.parsing.input.CharSequenceReader

object LastFMLoader extends Loader {
  val parser = new CSVParser(new TSVFormat {})
  def parseLine(line: String): Try[List[String]] = Try { parser.parseLine(new CharSequenceReader(line, 0)).get }

  def apply(path: String): Seq[SongPlay] =
     IOSource
      .fromFile(path)
      .getLines
      .toList
      .flatMap { line =>
        toSongPlay(line).toOption
      }

  def toSongPlay(line: String): Try[SongPlay] =
    for {
      record     <- parseLine(line)
      //required fields
      time       <- Try(record(0))
      trackName  <- Try(record(2))
      artistName <- Try(record(4))
      //optional fields
      trackMbid  = Try(record(3)).toOption
      artistMbid = Try(record(5)).toOption
      albumName  = Try(record(10)).toOption
      albumMbid  = Try(record(11)).toOption
    } yield {
      SongPlay(
        time,
        trackName,
        trackMbid,
        artistName,
        artistMbid,
        albumName,
        albumMbid,
        7 // LoaderId.LASTFM
      )
    }
}
