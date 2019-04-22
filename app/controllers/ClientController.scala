package controllers

import java.io.File
import java.net.URI

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.parboiled2.RuleTrace.Named
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Source}
import akka.util.Timeout
import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.{SiqParser, Util}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import javax.inject._
import akka.NotUsed
import akka.actor._
import akka.pattern.ask
import akka.stream.scaladsl._
import akka.util.Timeout
import models.actors.MyWebSocketActor
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.duration._
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
    Ok(Json.toJson(s))
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
//    case rh if sameOriginCheck(rh) =>
//      val k = wsFutureFlow(rh).map { flow =>
//        Right(flow)
//      }.recover {
//        case e: Exception =>
//          logger.error("Cannot create websocket", e)
//          val jsError = Json.obj("error" -> "Cannot create websocket")
//          val result = InternalServerError(jsError)
//          Left(result)
//      }
//      logger.info("Creating socket " + k)
//      k
//    case rejected =>
//      logger.error(s"Request ${rejected} failed same origin check")
//      Future.successful {
//        Left(Forbidden("forbidden"))
//      }
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
