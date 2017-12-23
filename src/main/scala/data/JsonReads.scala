
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsValue, _}

import scala.io.Source

/**
  * one.json:
  * {
  * "one" : 1
  * }
  *
  * two.json:
  * {
  * "one" : 1,
  * "two" : 2
  * }
  *
  * two.single.json:
  * {
  * "a" : {
  * "one": 1,
  * "two": 2
  * }
  * }
  *
  * two.double.json
  * {
  * "a": {
  * "one": 1,
  * "two": 2
  * },
  * "b": {
  * "one": 1,
  * "two": 2
  * }
  * }
  *
  */

object JsonReads {

  case class One(one: Int)

  case class Two(one: Int, two: Int)

  case class SingleTwo(a: Two)

  case class DoubleTwo(a: Two, b: Two)

  // THIS WILL NOT COMPILE
  //  implicit val oneReads: Reads[One] = (
  //    (__ \ "one")).read[Int](One)

  implicit val twoReads: Reads[Two] = (
    (__ \ "one").read[Int] ~
      (__ \ "two").read[Int]) (Two)

  // THIS WILL NOT COMPILE
  //  implicit val singleTwoReads: Reads[SingleTwo] = (
  //    (__ \ "a").read[Two]) (SingleTwo)

  implicit val doubleTwoReads: Reads[DoubleTwo] = (
    (__ \ "a").read[Two] ~
      (__ \ "b").read[Two]
    ) (DoubleTwo)

  def load(name: String): JsValue = {
    val url = getClass.getResource(s"/service/${name}.json")
    val reader = Source.fromURL(url).bufferedReader
    val text = reader.lines.toArray.mkString("")
    Json.parse(text)
  }

  def main(a: Array[String]): Unit = {

    val oneJsValue: JsValue = load("one")
    /*    oneJsValue.validate[One](oneReads) match {
          case s: JsSuccess[One] => println(s"Found: ${s.get.toString}")
          case e: JsError => println("Errors: " + JsError.toJson(e).toString())
        } */

    val twoJsValue: JsValue = load("two")
    twoJsValue.validate[Two](twoReads) match {
      case s: JsSuccess[Two] => println(s"Found: ${s.get.toString}")
      case e: JsError => println("Errors: " + JsError.toJson(e).toString())
    }

    val singleTwoJsValue: JsValue = load("two.single")
    /*    twoJsValue.validate[SingleTwo](singleTwoReads) match {
          case s: JsSuccess[SingleTwo] => println(s"Found: ${s.get.toString}")
          case e: JsError => println("Errors: " + JsError.toJson(e).toString())
        } */

    val doubleTwoJsValue: JsValue = load("two.double")
    doubleTwoJsValue.validate[DoubleTwo](doubleTwoReads) match {
      case s: JsSuccess[DoubleTwo] => println(s"Found: ${s.get.toString}")
      case e: JsError => println("Errors: " + JsError.toJson(e).toString())
    }

  }


}
