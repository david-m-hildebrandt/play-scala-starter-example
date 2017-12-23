package model

import javax.inject._

import data.FileUtility
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.concurrent.Future

@Singleton
class DataManager @Inject() (applicationLifecycle: ApplicationLifecycle, data: Data) {

  val logger = Logger(this.getClass.getCanonicalName)

  applicationLifecycle.addStopHook(() => {
    logger.debug("Unload all service")
    Future.successful(())
  })

}

// model
case class NameRequest(id: Int)

case class Name(first: String, middle: String, last: String)

case class IdName(id: Int, name: Name)

case class IdNameSeq(entries: Seq[IdName])

case class TelephoneNumber(areaCode: Int, localExchange: Int, lineNumber: Int)

case class IdTelephoneNumber(id: Int, telephoneNumber: TelephoneNumber)

case class IdTelephoneNumberSeq(entries: Seq[IdTelephoneNumber])

case class IdNameIdTelephoneNumberRelation(nameId: Int, telephoneNumberIds: Seq[Int])

case class IdNameIdTelephoneNumberSeq(entries: Seq[IdNameIdTelephoneNumberRelation])

case class TelephoneNumberRequest(nameId: Int)

case class IdNameIdTelephoneNumber(idName: IdName, idTelephoneNumbers: Seq[IdTelephoneNumber])


@Singleton
class Data {

  val logger = Logger(this.getClass.getCanonicalName)

  implicit val telephoneNumberRequest: Reads[TelephoneNumberRequest] =
    (__ \ "id").read[Int].map(TelephoneNumberRequest)

  implicit val telephoneNumberRequestWrites: Writes[TelephoneNumberRequest] =
    (__ \ "id").write[Int].contramap(unlift(TelephoneNumberRequest.unapply))

  implicit val telephoneNumberReads: Reads[TelephoneNumber] = (
    (__ \ "areaCode").read[Int] ~
      (__ \ "localExchange").read[Int] ~
      (__ \ "lineNumber").read[Int]
    ) (TelephoneNumber)

  implicit val telephoneNumberWrites: Writes[TelephoneNumber] = (
    (__ \ "areaCode").write[Int] ~
      (__ \ "localExchange").write[Int] ~
      (__ \ "lineNumber").write[Int]
    ) (unlift(TelephoneNumber.unapply))

  implicit val idTelephoneNumberReads: Reads[IdTelephoneNumber] = (
    (__ \ "id").read[Int] ~
      (__ \ "telephoneNumber").read[TelephoneNumber]
    ) (IdTelephoneNumber)

  implicit val idTelephoneNumberWrites: Writes[IdTelephoneNumber] = (
    (__ \ "id").write[Int] ~
      (__ \ "telephoneNumber").write[TelephoneNumber]
    ) (unlift(IdTelephoneNumber.unapply))

  implicit val idTelephoneNumberSeqReads: Reads[IdTelephoneNumberSeq] =
    (__ \ "entries").read[Seq[IdTelephoneNumber]].map(IdTelephoneNumberSeq)

  implicit val idTelephoneNumberSeqWrites: Writes[IdTelephoneNumberSeq] =
    (__ \ "entries").write[Seq[IdTelephoneNumber]].contramap(unlift(IdTelephoneNumberSeq.unapply))


  implicit val idNameIdTelephoneNumberRelationReads: Reads[IdNameIdTelephoneNumberRelation] = (
    (__ \ "nameId").read[Int] ~
      (__ \ "telephoneNumberIds").read[Seq[Int]]
    ) (IdNameIdTelephoneNumberRelation)

  implicit val idNameIdTelephoneNumberRelationWrites: Writes[IdNameIdTelephoneNumberRelation] = (
    (__ \ "nameId").write[Int] ~
      (__ \ "telephoneNumberIds").write[Seq[Int]]
    ) (unlift(IdNameIdTelephoneNumberRelation.unapply))

  implicit val nameReads: Reads[Name] = (
    (__ \ "first").read[String] ~
      (__ \ "middle").read[String] ~
      (__ \ "last").read[String]
    ) (Name)

  implicit val nameWrites: Writes[Name] = (
    (__ \ "first").write[String] ~
      (__ \ "middle").write[String] ~
      (__ \ "last").write[String]
    ) (unlift(Name.unapply))

  implicit val idNameReads: Reads[IdName] = (
    (__ \ "id").read[Int] ~
      (__ \ "name").read[Name]
    ) (IdName)

  implicit val idNameWrites: Writes[IdName] = (
    (__ \ "id").write[Int] ~
      (__ \ "name").write[Name]
    ) (unlift(IdName.unapply))

  implicit val idNameIdTelephoneNumberReads: Reads[IdNameIdTelephoneNumber] = (
    (__ \ "nameId").read[IdName] ~
      (__ \ "telephoneNumberIds").read[Seq[IdTelephoneNumber]]
    ) (IdNameIdTelephoneNumber)

  implicit val idNameIdTelephoneNumberWrites: Writes[IdNameIdTelephoneNumber] = (
    (__ \ "nameId").write[IdName] ~
      (__ \ "telephoneNumberIds").write[Seq[IdTelephoneNumber]]
    ) (unlift(IdNameIdTelephoneNumber.unapply))

  implicit val idNameIdTelephoneNumberSeqReads: Reads[IdNameIdTelephoneNumberSeq] =
    (__ \ "entries").read[Seq[IdNameIdTelephoneNumberRelation]].map(IdNameIdTelephoneNumberSeq)

  implicit val idNameIdTelephoneNumberSeqWrites: Writes[IdNameIdTelephoneNumberSeq] =
    (__ \ "entries").write[Seq[IdNameIdTelephoneNumberRelation]].contramap(unlift(IdNameIdTelephoneNumberSeq.unapply))




  implicit val nameRequestReads: Reads[NameRequest] =
    (__ \ "id").read[Int].map(NameRequest)

  implicit val nameRequestWrites: Writes[NameRequest] =
    (__ \ "id").write[Int].contramap(unlift(NameRequest.unapply))


  implicit val idNameSeqReads: Reads[IdNameSeq] = (__ \ "entries").read[Seq[IdName]].map(IdNameSeq)

  implicit val idNameSeqWrites: Writes[IdNameSeq] = (__ \ "entries").write[Seq[IdName]].contramap(unlift(IdNameSeq.unapply))

  logger.debug("Start service loading")

  val nameMap: Map[Int, IdName] = loadIdNameMap
  logger.debug(s"nameMap: ${nameMap}")

  val telephoneMap: Map[Int, IdTelephoneNumber] = loadIdTelephoneNumberMap
  logger.debug(s"telephoneMap: ${telephoneMap}")

  val nameTelephoneRelationMap: Map[Int, Seq[Int]] = loadIdNameIdTelephoneNumberRelationMap
  logger.debug(s"nameTelephoneRelationMap: ${nameTelephoneRelationMap}")

  val nameTelephoneMap: Map[Int, IdNameIdTelephoneNumber] = loadIdNameIdTelephoneNumberMap
  logger.debug(s"nameTelephoneMap: ${nameTelephoneMap}")


  def loadIdNameMap: Map[Int, IdName] = {
    loadIdNames.validate[IdNameSeq](idNameSeqReads) match {
      case idNameSeq: JsSuccess[IdNameSeq] => {
        logger.debug(s"Found: ${idNameSeq.get.toString}")
        idNameSeq.value.entries.foldLeft(Map(): Map[Int, IdName]) {
          (map, idName) => {
            map + ((idName.id) -> idName)
          }
        }
      }
      case e: JsError => {
        logger.debug("Errors: " + JsError.toJson(e).toString())
        Map()
      }
    }
  }

  def loadIdNames: JsValue = {
    logger.debug(getClass.getResource(".").toString)
    val url = getClass.getResource(s"../data/names.json")
    FileUtility.loadData(url)
  }


  def loadIdTelephoneNumberMap: Map[Int, IdTelephoneNumber] = {
    loadIdTelephoneNumbers.validate[IdTelephoneNumberSeq](idTelephoneNumberSeqReads) match {
      case idTelephoneNumberSeq: JsSuccess[IdTelephoneNumberSeq] => {
        idTelephoneNumberSeq.value.entries.foldLeft(Map(): Map[Int, IdTelephoneNumber]) {
          (map, idTelephoneNumber) => {
            map + ((idTelephoneNumber.id) -> idTelephoneNumber)
          }
        }
      }
      case e: JsError => {
        logger.debug("Errors: " + JsError.toJson(e).toString())
        Map()
      }
    }
  }

  def loadIdTelephoneNumbers: JsValue = {
    logger.debug(getClass.getResource(".").toString)
    val url = getClass.getResource(s"../data/telephoneNumbers.json")
    FileUtility.loadData(url)
  }

  def loadIdNameIdTelephoneNumberRelationMap: Map[Int, Seq[Int]] = {
    loadIdNameIdTelephoneNumbers.validate[IdNameIdTelephoneNumberSeq](idNameIdTelephoneNumberSeqReads) match {
      case idNameIdTelephoneNumberSeq: JsSuccess[IdNameIdTelephoneNumberSeq] => {
        idNameIdTelephoneNumberSeq.value.entries.foldLeft(Map(): Map[Int, Seq[Int]]) {
          (map, idNameIdTelephoneNumbers) => {
            map + ((idNameIdTelephoneNumbers.nameId -> idNameIdTelephoneNumbers.telephoneNumberIds))
          }
        }
      }
      case e: JsError => {
        logger.debug("Errors: " + JsError.toJson(e).toString())
        Map()
      }
    }
  }

  def loadIdNameIdTelephoneNumbers: JsValue = {
    logger.debug(getClass.getResource(".").toString)
    val url = getClass.getResource(s"../data/nameTelephoneNumbers.json")
    FileUtility.loadData(url)
  }

  def loadIdNameIdTelephoneNumberMap: Map[Int, IdNameIdTelephoneNumber] = {
    nameTelephoneRelationMap.foldLeft(Map(): Map[Int, IdNameIdTelephoneNumber]) {
      (map, idNameIdTelephoneNumberSeq) => {
        map + (idNameIdTelephoneNumberSeq._1 ->
          IdNameIdTelephoneNumber(
            nameMap.get(idNameIdTelephoneNumberSeq._1).get,
            idNameIdTelephoneNumberSeq._2.map(id => telephoneMap.get(id).get)))
      }
    }
  }


  def fromJsValue[T](reads: Reads[T])(jsValue: JsValue): T = {
    jsValue.validate[T](reads) match {
      case t: JsSuccess[T] => t.value
      case e: JsError => throw new IllegalArgumentException(s"Cannot be read.  \nJsValue: ${jsValue} \nJsError: ${e}")
    }
  }

  def fromJsValue_NameRequest(jsValue: JsValue): NameRequest = {
    fromJsValue[NameRequest](nameRequestReads)(jsValue)
  }

  def toJsValue(nameRequest: NameRequest): JsValue = {
    Json.toJson(nameRequest)
  }

  def fromJsValue_IdName(jsValue: JsValue): IdName = {
    fromJsValue[IdName](idNameReads)(jsValue)
  }

  def toJsValue(idName: IdName): JsValue = {
    Json.toJson(idName)
  }

  def fromJsValue_IdTelephoneNumberSeq(jsValue: JsValue): IdTelephoneNumberSeq = {
    fromJsValue[IdTelephoneNumberSeq](idTelephoneNumberSeqReads)(jsValue)
  }

  def toJsValue(idTelephoneNumberSeq: IdTelephoneNumberSeq): JsValue = {
    Json.toJson(idTelephoneNumberSeq)
  }

  def fromJsValue_TelephoneNumber(jsValue: JsValue): TelephoneNumber = {
    fromJsValue[TelephoneNumber](telephoneNumberReads)(jsValue)
  }

  def toJsValue(telephoneNumber: TelephoneNumber): JsValue = {
    Json.toJson(telephoneNumber)
  }


  def fromJsValue_IdNameSeq(jsValue: JsValue): IdNameSeq = {
    fromJsValue[IdNameSeq](idNameSeqReads)(jsValue)
  }

  def toJsValue(idNameSeq: IdNameSeq): JsValue = {
    Json.toJson(idNameSeq)
  }

  def toJsValue(idNameIdTelephoneNumber: IdNameIdTelephoneNumber): JsValue = {
    Json.toJson(idNameIdTelephoneNumber)
  }

  def toJsValue(idTelephoneNumber: IdTelephoneNumber): JsValue = {
    Json.toJson(idTelephoneNumber)
  }

}


