package controllers.data

import javax.inject._

import model.{IdTelephoneNumberSeq, _}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class ContactInformationDataController @Inject()(cc: ControllerComponents, data: Data) extends AbstractController(cc) {
  //class ContactInformationServiceController @Inject()(cc: ControllerComponents, data: Data) extends AbstractController(cc) {

  val logger = Logger(this.getClass.getCanonicalName)

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

  //  def formContactDetailsRequestJson(id: Int): String = s"{ \"id\": ${id} }"

  def formUrl(pathElement: String) = s"http://localhost:9000/study/${pathElement}"

  /*
  def getName(wsClient: WSClient, id: Int): Future[String] = {
    //    val logger = getLogger()
    val url = formUrl("name")
    logger.debug(s"WS: url: ${url}")
    val jsonData = formContactDetailsRequestJson(id)
    val text = wsClient.url(url).post(jsonData).map { response =>
      val statusText: String = response.statusText
      logger.debug(s"Got a response $statusText")
      logger.debug(s"Got a response ${response.body}")
      response.body
    }
    text
  }
*/

  def postRequestName = Action {
    request => {
      logger.debug("postRequestName")
      val idName = formIdName(request)

      val jsValueResponse = data.toJsValue(idName)
      logger.debug(s"jsValueResponse: ${jsValueResponse}")

      Ok(jsValueResponse)
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

  def postRequestNameTelephoneNumbers = Action {
    request => {
      logger.debug("postRequestNameTelephoneNumbers")

      val idName = formIdName(request)

      val telephoneNumberIdList: Seq[Int] = data.nameTelephoneRelationMap.getOrElse(idName.id, List())

      logger.debug(s"telephoneNumberIdList: ${telephoneNumberIdList}")

      val idTelephoneNumbers: Seq[IdTelephoneNumber] = telephoneNumberIdList.map(id => data.telephoneMap.get(id).get)

      logger.debug(s"idTelephoneNumbers: ${idTelephoneNumbers}")

      val idNameIdTelephoneNumber: IdNameIdTelephoneNumber = IdNameIdTelephoneNumber(idName, idTelephoneNumbers)

      logger.debug(s"idNameIdTelephoneNumber: ${idNameIdTelephoneNumber}")

      val jsValueResponse = data.toJsValue(idNameIdTelephoneNumber)

      logger.debug(s"jsValueResponse: ${jsValueResponse}")

      Ok(jsValueResponse)
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
