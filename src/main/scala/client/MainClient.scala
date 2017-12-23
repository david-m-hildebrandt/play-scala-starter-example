package client

import java.net.URL
import java.time.Clock
import javax.inject._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.AbstractModule
import model._
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws._
import play.api.libs.ws.ahc.AhcWSClient
import services.{AtomicCounter, Counter}

import scala.concurrent.{Future, Promise}
import scala.io.Source
import scala.util.{Failure, Success}

class MainClient @Inject()(data: Data) extends App {

  val logger = Logger(this.getClass.getCanonicalName)

  def getLogger(): Logger = if (logger == null) Logger(this.getClass.getCanonicalName) else logger


  def formUrl(pathElement: String) = s"http://localhost:9000/study/${pathElement}"

  import scala.concurrent.ExecutionContext.Implicits._

  override def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    val wsClient = AhcWSClient()

    val logger = getLogger()
    logger.debug(s"calling web data")

    /*
    val text = getName(wsClient, 1)
    FutureRegistrar.add(text)
    text.onComplete({
      case Success(v) => println(s"v: ${v}")
      case Failure(e) => println(s"e: ${e}")
    })

    val idName = getIdName(wsClient, 1)
    FutureRegistrar.add(idName)
    idName.onComplete({
      case Success(v) => println(s"idName: ${v}")
      case Failure(e) => println(s"e: ${e}")
    })
*/

    val idNameIdTelephoneNumber = getIdNameIdTelephoneNumber(wsClient, 1)
    FutureRegistrar.add(idNameIdTelephoneNumber)
    idNameIdTelephoneNumber.onComplete({
      case Success(v) => println(s"idNameIdTelephoneNumber: ${v}")
      case Failure(e) => println(s"e: ${e}")
    })

/*
    callForTelephoneNumbers(wsClient, 1)
      .andThen { case _ => wsClient.close() }
      .andThen { case _ => system.terminate() }
*/


/*
    callForNameTelephoneNumbers(wsClient, 1)
      .andThen { case _ => wsClient.close() }
      .andThen { case _ => system.terminate() }
*/
    /*    for (id <- 1 to 3) {
          //      call(wsClient, id)
          //        .andThen { case _ => wsClient.close() }
          //        .andThen { case _ => system.terminate() }
          callForName(wsClient, id)
            .andThen { case _ => wsClient.close() }
            .andThen { case _ => system.terminate() }
          callForTelephoneNumbers(wsClient, id)
            .andThen { case _ => wsClient.close() }
            .andThen { case _ => system.terminate() }
          callForNameTelephoneNumbers(wsClient, id)
            .andThen { case _ => wsClient.close() }
            .andThen { case _ => system.terminate() }

        }
    */
    /*
    val idName : IdName = execute(wsClient).onComplete {
      case Success(v) => v
      case Failure(v) => IdName(0,null)
    }
*/

    //      .onComplete(x => if (x.isSuccess) x.get else null )
    //        .andThen{case _ => logger.debug("execute(wsClient) completed")}
    //      .andThen { case _ => wsClient.close }


    //      .andThen { case _ => system.terminate }

    FutureRegistrar.loopTillCompleted
    wsClient.close()
    system.terminate()
  }

  def execute(wsClient: WSClient): Future[IdName] = {

    val logger = getLogger()
    logger.debug("==================================================")
    logger.debug("execute - begin")
    logger.debug("==================================================")

    val jsonData = formContactDetailsRequestJson(1)

    val nameUrl = formUrl("name")

    val futureIdName: Future[IdName] = wsClient.url(nameUrl).post(jsonData).map { response =>
      val statusText: String = response.statusText
      logger.debug(s"Got a response $statusText")
      logger.debug(s"Got a response ${response.body}")
      response.body

      val jsValueResponse: JsValue = Json.parse(response.body)
      logger.debug(s"jsValueResponse: ${jsValueResponse}")

      val idName: IdName = data.fromJsValue_IdName(jsValueResponse)
      logger.debug(s"idName: 1 ${idName}")
      idName
    }

    //      .andThen( case _ => logger.debug(""))
    //      onComplete(x=> if (x.isSuccess) x.get else null)
    //    logger.debug(s"idName 2: ${idName}")

    //    val telephoneNumbersUrl = formUrl("telephoneNumbers")

    //   TODO -separate into futures
    // TODO - experiment with delays

    //    logger.debug("==================================================")
    //    logger.debug("execute - end")
    //    logger.debug("==================================================")

    //    Future.unit
    futureIdName
  }

  def getName(wsClient: WSClient, id: Int): Future[String] = {
    val logger = getLogger()
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

  def getIdName(wsClient: WSClient, id: Int): Future[IdName] = {
    val logger = getLogger()
    val url = formUrl("name")
    logger.debug(s"WS: url: ${url}")
    val jsonData = formContactDetailsRequestJson(id)
    val idName: Future[IdName] = wsClient.url(url).post(jsonData).map { response =>
      val statusText: String = response.statusText
      logger.debug(s"Got a response $statusText")
      logger.debug(s"Got a response ${response.body}")
      (new Data()).fromJsValue_IdName(Json.parse(response.body))
    }
    idName
  }

  def getIdNameIdTelephoneNumber(wsClient: WSClient, id: Int): Future[IdNameIdTelephoneNumber] = {
    val logger = getLogger()
    val idNameUrl = formUrl("name")
    logger.debug(s"WS: url: ${idNameUrl}")

    val jsonData = formContactDetailsRequestJson(id)
    val idName: Future[IdName] = wsClient.url(idNameUrl).post(jsonData).map { response =>
      val statusText: String = response.statusText
      logger.debug(s"Got a response $statusText")
      logger.debug(s"Got a response ${response.body}")
      (new Data()).fromJsValue_IdName(Json.parse(response.body))
    }

    val telephoneNumberUrl = formUrl("telephoneNumbers")

    val idTelephoneNumberSeq: Future[IdTelephoneNumberSeq] = wsClient.url(telephoneNumberUrl).post(jsonData).map { response =>
      val statusText: String = response.statusText
      logger.debug(s"Got a response $statusText")
      logger.debug(s"Got a response ${response.body}")
      (new Data()).fromJsValue_IdTelephoneNumberSeq(Json.parse(response.body))
    }

    val promise = Promise[IdNameIdTelephoneNumber]()

    idName.onComplete {
      case Success(idName) => {
        idTelephoneNumberSeq.onComplete {
          case Success(idTelephoneNumberSeq) => {
            promise.trySuccess(IdNameIdTelephoneNumber(idName, idTelephoneNumberSeq.entries))
          }
          case Failure(e) => {
            logger.debug(s"Failure getting IdTelephoneNumberSeq: ${e}")
            promise.tryFailure(e)
          }
        }
      }
      case Failure(e) => {
        logger.debug(s"Failure getting IdName: ${e}")
        promise.tryFailure(e)
      }
    }

    promise.future
  }


  def callForName(wsClient: WSClient, id: Int): Future[Unit] = {
    val logger = getLogger()
    val url = formUrl("name")
    logger.debug(s"WS: url: ${url}")
    val jsonData = formContactDetailsRequestJson(id)
    wsClient.url(url).post(jsonData).map { response =>
      val statusText: String = response.statusText
      logger.debug(s"Got a response $statusText")
      logger.debug(s"Got a response ${response.body}")
    }
  }

  def callForTelephoneNumbers(wsClient: WSClient, id: Int): Future[Unit] = {
    val logger = getLogger()
    val url = formUrl("telephoneNumbers")
    logger.debug(s"WS: url: ${url}")
    val jsonData = formContactDetailsRequestJson(id)
    logger.debug(s"jsonData for request: ${jsonData}")
    wsClient.url(url).post(jsonData).map { response =>
      val statusText: String = response.statusText
      logger.debug(s"Got a response $statusText")
      logger.debug(s"Got a response ${response.body}")
    }
  }

  def callForNameTelephoneNumbers(wsClient: WSClient, id: Int): Future[Unit] = {
    val logger = getLogger()
    val url = formUrl("nameTelephoneNumbers")
    logger.debug(s"WS: url: ${url}")
    val jsonData = formContactDetailsRequestJson(id)
    logger.debug(s"jsonData for request: ${jsonData}")
    wsClient.url(url).post(jsonData).map { response =>
      val statusText: String = response.statusText
      logger.debug(s"Got a response $statusText")
      logger.debug(s"Got a response ${response.body}")
    }
  }

  /*
    def call(wsClient: WSClient, id: Int): Future[Unit] = {
      val url = formUrl("showPortRequest")
      val jsonData = formContactDetailsRequestJson(id)
      logger.debug(s"jsonData for request: ${jsonData}")
      wsClient.url(url).post(jsonData).map { response =>
        val statusText: String = response.statusText
        logger.debug(s"Got a response $statusText")
        logger.debug(s"Got a response ${response.body}")
      }
    }
  */
  def formContactDetailsRequestJson(id: Int): JsValue = {
    val logger = getLogger()
    logger.debug(getClass.getResource(".").toString)
    val url = getClass.getResource(s"../requests/name.${id}.json")
    logger.debug(s"File: url: ${url}")
    val source = loadRequest(url)
    Json.parse(source)
  }


  def loadRequest(url: URL): String = {
    val logger = getLogger()
    logger.debug(s"loadRequest: url: ${url}")
    val reader = Source.fromURL(url).bufferedReader
    // bufferedReader reads only once
    reader.lines.toArray.mkString("")
  }

}


class LocalModule extends AbstractModule {

  override def configure() = {

    // Use the system clock as the default implementation of Clock
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    // Ask Guice to create an instance of ApplicationTimer when the
    // application starts.
    //    bind(classOf[ApplicationTimer]).asEagerSingleton()
    // Set AtomicCounter as the implementation for Counter.
    bind(classOf[Counter]).to(classOf[AtomicCounter])

    bind(classOf[Data]).asEagerSingleton()
    bind(classOf[MainClient]).asEagerSingleton()
  }
}

object MainObject {

  val logger = Logger(this.getClass.getCanonicalName)

  def main(a: Array[String]): Unit = {

    import com.google.inject.Guice

    logger.debug("main - started")

    val injector = Guice.createInjector(new LocalModule)

    val mainClient: MainClient = injector.getInstance(classOf[client.MainClient])

    logger.debug(s"main - mainClient: ${mainClient}")

    mainClient.main(Array())

  }

}
