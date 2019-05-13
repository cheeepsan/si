package controllers

import java.io.File
import java.net.URI
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.libs.streams.ActorFlow
import services.{SiqParser, Util}
import models.actors.MyWebSocketActor
import models.si.{SiMessage, SiUser}
import play.api.Logger
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}

class ClientController @Inject()(cc: ControllerComponents)(
  implicit system: ActorSystem,
  mat: Materializer,
  implicit val executionContext: ExecutionContext)
  extends AbstractController(cc) {

  private val logger = Logger


  def index() = Action { implicit request: Request[AnyContent] =>
    val webSocketUrl = routes.ClientController.ws().webSocketURL()
    logger.info(s"index: ")
    Ok(views.html.index(webSocketUrl))
  }

  def process() = Action { implicit request: Request[AnyContent] =>
    val parser = new SiqParser
    val destDir = new File("F:\\workspace\\si\\siClient\\pack")

    val s = parser.process(destDir)
    val m = new SiMessage("message", new SiUser(1, "Vasserman"), "hz", s)
    Ok(Json.toJson(m))
  }

  /**
    * websockets
    */

  /**
    * Creates a websocket.  `acceptOrResult` is preferable here because it returns a
    * Future[Flow], which is required internally.
    *
    * @return a fully realized websocket.
    */
  def ws: WebSocket = WebSocket.acceptOrResult[String, String] {
    case rh if sameOriginCheck(rh) =>
      val k = ActorFlow.actorRef { out =>
        MyWebSocketActor.props(out)
      }
      logger.info("actorFlow " + k)
      Future.successful(Right(k))
    case rejected =>
      logger.error(s"Request ${rejected} failed same origin check")
      Future.successful {
        Left(Forbidden("forbidden"))
      }
  }

  /**
    * Checks that the WebSocket comes from the same origin.  This is necessary to protect
    * against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
    *
    * See https://tools.ietf.org/html/rfc6455#section-1.3 and
    * http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
    */
  private def sameOriginCheck(implicit rh: RequestHeader): Boolean = {
    // The Origin header is the domain the request originates from.
    // https://tools.ietf.org/html/rfc6454#section-7
    logger.info("Checking the ORIGIN ")

    rh.headers.get("Origin") match {
      case Some(originValue) if originMatches(originValue) =>
        logger.info(s"originCheck: originValue = $originValue")
        true

      case Some(badOrigin) =>
        logger.error(s"originCheck: rejecting request because Origin header value ${badOrigin} is not in the same origin")
        false

      case None =>
        logger.error("originCheck: rejecting request because no Origin header found")
        false
    }
  }

  /**
    * Returns true if the value of the Origin header contains an acceptable value.
    */
  private def originMatches(origin: String): Boolean = {
    try {
      val url = new URI(origin)
      url.getHost == "localhost" &&
        (url.getPort match {
          case 9000 | 19001 => true;
          case _ => true
        })
    } catch {
      case e: Exception => false
    }
  }

}
