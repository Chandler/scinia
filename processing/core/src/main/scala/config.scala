package com.scinia

import scala.io.Source._
import scala.util._
import play.api.libs.json._

abstract class SciniaConfig {
  val projectName: String
  val projectParentPath: String
  val projectRootDirName = s"$projectName-scinia"
  val projectPath = s"$projectParentPath/$projectRootDirName"
  val databasePath = s"$projectPath/$projectName.db"
}

class JsonConfig(configFile: String) extends SciniaConfig with Log {

  case class File(projectName: String, projectLocation: String)

  def text = fromFile(configFile).getLines.mkString

  def configTry =
    Try(Json.parse(text))
      .flatMap { json =>
        Json.reads[File].reads(json) match {
          case JsSuccess(config, _) => Success(config)
          case JsError(error) => Failure(new RuntimeException(error.toString))
        }
      }

  def config =
    configTry match {
      case Success(config) => config
      case Failure(e) => {
        Log(s"failed, could not parse config file at $configFile")
        throw e
      }
    }

  override lazy val projectName = config.projectName
  override lazy val projectParentPath = config.projectLocation
}
