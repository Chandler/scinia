package com.scinia

import com.scinia.LoaderId.LoaderId
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError
import com.scinia.Tables._
import com.typesafe.scalalogging.LazyLogging
import scala.io.Source

import com.github.tototoshi.csv._
import com.scinia.Tables._
import java.io._
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._
import scala.util.control.Breaks._
import scala.util.parsing.input.CharSequenceReader
import scala.util.Try
import org.joda.time.format.{DateTimeFormat => Date }
/**
 * Ids for types of data sources we support or want to support
 */
object LoaderId extends Enumeration {
  type LoaderId = Value
  val SMS      = Value(1)
  val GVOICE   = Value(2)
  val LATITUDE = Value(3)
  val HANGOUTS = Value(4)
  val TWEETS   = Value(5)
  val FACEBOOK = Value(6)
  val RDIO     = Value(7)
  val SKYPE    = Value(8)
  val GCHAT    = Value(9)
  val GSEARCH  = Value(10)
}

/**
 * A Loader is something that takes a file path and returns a sequence of items.
 * 
 * This is the base type for constructing more specific loaders, for example something
 * that reads a file of CSV records and returns a seq of some case class representation
 * of those records.
 */
trait Loader[T] extends Log {
  def apply(path: String): Seq[T] = handleSingleFile(pathModifier(path))

  def handleSingleFile(path: String): Seq[T]

  // example:
  //   override def pathModifer(path: String): String = s"$path/data/scrobbles.tsv"
  def pathModifier(path: String): String = path
}

trait MultiFileLoader[T] extends Loader[T] {
  /**
   * A filter to apply to all the file paths recursively found
   * in the directory being loaded
   * 
   * example:
   *  override def fileNameFilter(path: String) = path.getName.endsWith(".json")
   */
  def fileNameFilter(path: String): Boolean = true

  override def apply(path: String): Seq[T] = 
    IOSource.getFileTreePaths(pathModifier(path))
      .filter(fileNameFilter)
      .flatMap(handleSingleFile)
}

/**
 * mix this into your Loader if the complete text of a file represents a complicated type
 * (like a JSON list) that contains many records. The parse method will evaluate the object
 * and extract the records.
 * 
 * Don't use this if your file contains many delimited records and you want the parse method
 * to apply to each record. That's what [[loadDelimitedRecords]] is for.
 */
trait LoadRecordContainingObject[OutputRecord] extends Loader[OutputRecord] {
  def handleSingleFile(path: String): Seq[OutputRecord] = parse(read(path))

  def read(path: String): String = Source.fromFile(path).getLines.mkString

  def parse(input: String): List[OutputRecord]
}

/** 
 * mix this into your Loader if the file contains many delimited records
 * and you want the parse method to be applied to each record
 */
trait loadDelimitedRecords[OutputRecord] extends Loader[OutputRecord] {
  // val delimiter = '\n'

  def handleSingleFile(path: String): Seq[OutputRecord] = read(pathModifier(path)).flatMap(parse)

  def parse(input: String): Option[OutputRecord]

  def read(path: String) = Source.fromFile(path).getLines().toSeq
}

trait ParseCSV[OutputRecord] {
  // can override for different formats like TSVFormat
  // https://github.com/tototoshi/scala-csv/blob/master/src/main/scala/com/github/tototoshi/csv/Formats.scala
  val format: CSVFormat = new DefaultCSVFormat {}

  // The tototoshi.csv doesn't support reading strings, it only supports reading in files
  // I wanted to read the CSV file myself line by line and parse the strings, so I'm using the
  // internal CSVParser.parseLine myself
  val parser = new CSVParser(format)
  
  def parseCSVLine(line: String): Try[List[String]] = Try { parser.parseLine(new CharSequenceReader(line, 0)).get }

  def parse(line: String): Option[OutputRecord] = parseCSVLine(line).toOption.flatMap(transform)

  def transform(lineItems: Seq[String]): Option[OutputRecord]
 } 

trait ParseTSV[OutputRecord] extends ParseCSV[OutputRecord] {
  override val format = new TSVFormat {}
}

/*
 * trait you can mix into your [[Loader]] if you expect to be parsing Json objects that represent
 * single record.
 * 
 * OutputRecord is the type your loader will return a sequence of.
 */
trait ParseJson[OutputRecord] {
  /** 
   * Individual string records will be mapped directly to this type using the play JSON library.
   * Must have an unapply method (just use a case class)
   * 
   * It's perfectly acceptable to set this equal to type OutputRecord if your string records
   * can be mapped directly to the final OutputRecord type.
   * 
   * If JsonRecord and Output record are different you must define the mapping in the
   * transform method.
   */
  type JsonRecord

  /** The component of the Play JSON library that maps strings to scala objects. */
  val reader: Reads[JsonRecord]

  /** 
   * A conversion from JsonRecord to OutputRecord.
   * 
   * If there is no difference just do:
   * def transform(record: JsonRecord): OutputRecord = record
   */
  def transform(record: JsonRecord): OutputRecord

  /**
   * Convert a JSON object representing a single record into the desired Scala type for your record.
   */
  def parse(obj: String): Option[OutputRecord] = ParseJson.genericParseJson(obj, reader).map(transform)
}

/**
 * trait you can mix into your [[Loader]] if you expect to be parsing A JSON list of JSON objects
 * where each object in the list is an individual record.
 *
 * Similar to [[ParseJson]]
 */
trait ParseJsonList[OutputRecord]{
  type JsonRecord

  def transform(record: JsonRecord): OutputRecord

  val reader: Reads[List[JsonRecord]]

  def parse(obj: String): List[OutputRecord] = ParseJson.parseJsonList(obj, reader).map(transform)
}

object ParseJson {
  /** Convert a string into Option[A] */
  def genericParseJson[A](obj: String, reader: Reads[A]): Option[A] =
    Json.parse(obj)
      .validate[A](reader) match {
        case JsSuccess(result, _) => Some(result)
        case JsError(errors)      => None
      }

  /** Convert a string into List[A]*/
  def parseJsonList[A](obj: String, reader: Reads[List[A]]): List[A] = 
    genericParseJson[List[A]](obj, reader).getOrElse(Nil)
}
