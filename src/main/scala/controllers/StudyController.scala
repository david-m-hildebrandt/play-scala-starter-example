package controllers

import javax.inject._

import model.{Data, IdName}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class StudyController @Inject()(cc: ControllerComponents, data: Data) extends AbstractController(cc) {

  val logger = Logger(this.getClass.getCanonicalName)

  val nameMap = data.nameMap

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def showGetRequest = Action { request => {
    logger.debug("showGetRequest")
    Ok(extractRequestDetails(request) +
      s"request.queryString.getOrElse('a', 'Nothing found'): ${request.queryString.getOrElse("a", "Nothing found")}")
  }
  }

  def showPostRequest = Action {
    request => {
      logger.debug("showPostRequest")
      val jsValueRequest: JsValue = request.body.asJson.getOrElse(null)
      logger.debug(s"jsValueRequest: ${jsValueRequest}")

      val nameRequest = data.fromJsValue_NameRequest(jsValueRequest)

      val idName: IdName = nameMap.getOrElse(nameRequest.id, IdName(0, null))

      val jsValueResponse = data.toJsValue(idName)
      logger.debug(s"jsValueResponse: ${jsValueResponse}")

      Ok(jsValueResponse)
    }
  }

  def extractRequestDetails(request: Request[AnyContent]): String = {
    s"Request: ${request} \n" +
      s" request.path: ${request.path} \n" +
      s"request.queryString: ${request.queryString} \n"
  }

}
