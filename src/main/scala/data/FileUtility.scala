package data

import java.net.URL

import play.api.Logger
import play.api.libs.json.{JsValue, Json}

import scala.io.Source

object FileUtility {

  val logger = Logger(this.getClass.getCanonicalName)

  def loadData(url: URL): JsValue = {
    logger.debug(getClass.getResource(".").toString)

    logger.debug(s"Loading file.  URL: ${url}")
    val reader = Source.fromURL(url).bufferedReader
    // bufferedReader reads only once
    val source = reader.lines.toArray.mkString("")
    Json.parse(source)
  }


}
