package controllers.service

import javax.inject._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import model._
import play.api.Logger
import play.api.libs.concurrent.CustomExecutionContext
import play.api.libs.json._
import play.api.libs.ws._
import play.api.libs.ws.ahc.AhcWSClient
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

trait MyExecutionContext extends ExecutionContext

class MyExecutionContextImpl @Inject()(system: ActorSystem)
  extends CustomExecutionContext(system, "my.executor") with MyExecutionContext


/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class ContactInformationServiceController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem, data: Data)(implicit exec: ExecutionContext) extends AbstractController(cc) {
  //class ContactInformationServiceController @Inject()(cc: ControllerComponents, data: Data) extends AbstractController(cc) {

  val logger = Logger(this.getClass.getCanonicalName)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val wsClient = AhcWSClient()


  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */

  // postRequestName
  // postRequestTelephoneNumbers
  // postRequestNameTelephoneNumbers

  //  The men virtue signal and psoition themselves.  They have been trained.

  def formContactDetailsRequestJson(id: Int): String = s"{ ${'"'}id${'"'}: ${id} }"

  def formUrl(pathElement: String) = s"http://localhost:9000/data/${pathElement}"

  def getName(wsClient: WSClient, id: Int): Future[String] = {
    //    val logger = getLogger()
    val url = formUrl("name")
    logger.debug(s"WS: url: ${url}")
    val jsonData = formContactDetailsRequestJson(id)
    logger.debug(s"WS: jsonData: ${jsonData}")
    val jsValue: JsValue = Json.parse(jsonData)
    val text = wsClient.url(url).post(jsValue).map { response =>
      val statusText: String = response.statusText
      logger.debug(s"Got a response $statusText")
      logger.debug(s"Got a response ${response.body}")
      response.body
    }
    text
  }

  def getIdName(wsClient: WSClient, id: Int): Future[IdName] = {
    //    val logger = getLogger()
    val url = formUrl("name")
    logger.debug(s"WS: url: ${url}")
    val jsonData = formContactDetailsRequestJson(id)
    logger.debug(s"WS: jsonData: ${jsonData}")
    val jsValue: JsValue = Json.parse(jsonData)
    val idName = wsClient.url(url).post(jsValue).map { response =>
      val statusText: String = response.statusText
      logger.debug(s"Got a response $statusText")
      logger.debug(s"Got a response ${response.body}")
      val idNameJson = Json.parse(response.body)
      val idName = data.fromJsValue_IdName(idNameJson)
      idName
    }
    idName
  }

  def getIdTelephoneNumberSeq(wsClient: WSClient, id: Int): Future[IdTelephoneNumberSeq] = {
    //    val logger = getLogger()
    val url = formUrl("telephoneNumbers")
    logger.debug(s"WS: url: ${url}")
    val jsonData = formContactDetailsRequestJson(id)
    logger.debug(s"WS: jsonData: ${jsonData}")
    val jsValue: JsValue = Json.parse(jsonData)
    val idTelephoneNumberSeq = wsClient.url(url).post(jsValue).map { response =>
      val statusText: String = response.statusText
      logger.debug(s"Got a response $statusText")
      logger.debug(s"Got a response ${response.body}")
      val idTelephoneNumberSeqJsValue = Json.parse(response.body)
      val idTelephoneNumberSeq = data.fromJsValue_IdTelephoneNumberSeq(idTelephoneNumberSeqJsValue)
      idTelephoneNumberSeq
    }
    idTelephoneNumberSeq
  }


  def postRequestName = Action.async {
    request => {
      //      Future {
      logger.debug("postRequestName")
      val idName = formIdName(request)

      val jsValueResponse = data.toJsValue(idName)

      getIdName(wsClient, idName.id).map {
        idName =>
          val idNameJsValue = data.toJsValue(idName)
          logger.debug(s"idNameJsValue: ${idNameJsValue}")
          Ok(idNameJsValue)
      }
    }
  }


  def postRequestTelephoneNumbers = Action {
    request => {
      logger.debug("postRequestTelephoneNumbers")

      val idName = formIdName(request)

      val telephoneNumberIdList: Seq[Int] = data.nameTelephoneRelationMap.getOrElse(idName.id, List())

      logger.debug(s"telephoneNumberIdList: ${telephoneNumberIdList}")

      val idTelephoneNumbers: Seq[IdTelephoneNumber] = telephoneNumberIdList.map(id => data.telephoneMap.get(id).get)

      logger.debug(s"idTelephoneNumbers: ${idTelephoneNumbers}")

      val idTelephoneNumberSeq: IdTelephoneNumberSeq = IdTelephoneNumberSeq(idTelephoneNumbers)

      logger.debug(s"idTelephoneNumberSeq: ${idTelephoneNumberSeq}")

      val jsValueResponse = data.toJsValue(idTelephoneNumberSeq)

      logger.debug(s"jsValueResponse: ${jsValueResponse}")

      Ok(jsValueResponse)
    }
  }

  def postRequestNameTelephoneNumbers = Action.async {
    request => {
      logger.debug("postRequestNameTelephoneNumbers")

      val idName = formIdName(request)

      val jsValueResponse = data.toJsValue(idName)

      val idNameId = idName.id

      getIdName(wsClient, idNameId).flatMap {
        idName => {
          getIdTelephoneNumberSeq(wsClient, idNameId).map {
            idTelephoneNumberSeq => {
              val idNameIdTelephoneNumber = IdNameIdTelephoneNumber(idName, idTelephoneNumberSeq.entries)
              val idNameIdTelephoneNumberJsValue = data.toJsValue(idNameIdTelephoneNumber)
              logger.debug(s"idNameIdTelephoneNumberJsValue: ${idNameIdTelephoneNumberJsValue}")
              Ok(idNameIdTelephoneNumberJsValue)
            }
          }
        }
      }
    }
  }

  def formIdName(request: Request[AnyContent]): IdName = {

    val jsValueRequest: JsValue = request.body.asJson.getOrElse(null)
    logger.debug(s"jsValueRequest: ${jsValueRequest}")

    val nameRequest = data.fromJsValue_NameRequest(jsValueRequest)
    logger.debug(s"nameRequest: ${nameRequest}")

    val idName: IdName = data.nameMap.getOrElse(nameRequest.id, IdName(0, null))
    logger.debug(s"idName: ${idName}")

    idName
  }

}
