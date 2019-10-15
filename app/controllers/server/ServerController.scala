package controllers.server

import java.net.{InetSocketAddress, URI}

import akka.pattern.gracefulStop
import akka.util.Timeout
import javax.inject.Inject
import play.api.mvc._
import services.ClientServerInit
import java.util.concurrent.TimeUnit.SECONDS

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer

import scala.concurrent.duration._
import models.server.actors.{ServerActor, ServerWebSocketActor}
import play.api.Logger
import play.api.libs.streams.ActorFlow

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future, duration}
import scala.util.{Failure, Success}


class ServerController @Inject()(cc: ControllerComponents, csi: ClientServerInit)(
  implicit system: ActorSystem,
  mat: Materializer,
  implicit val executionContext: ExecutionContext)
  extends AbstractController(cc) {

  private val logger = Logger
  val conn = new InetSocketAddress("127.0.0.1", 9090)
  implicit val timeout = new Timeout(duration.FiniteDuration(500, SECONDS))

  def index = Action { implicit request: Request[AnyContent] =>
    val webSocketUrl = routes.ServerController.ws().webSocketURL()
    Ok(views.html.server.index(webSocketUrl))
  }

  def init = Action { implicit request: Request[AnyContent] =>
    Await.ready(resolveActor("serverActor"), Duration.Inf)
    logger.info("inited")
    Ok("created")
  }

  def resolveActor(name: String) = csi.server.actorSelection("/user/" + name).resolveOne().recoverWith {
    case e: Exception =>
      logger.debug("not found ServerActor, new")
      Future.successful(csi.server.actorOf(ServerActor.props(conn), name))
  }

  def killServerActor = Action.async { implicit request: Request[AnyContent] =>
    csi.server.actorSelection("/user/serverActor").resolveOne().flatMap {
      a =>
        gracefulStop(a, 1 minute).flatMap {
          b =>
            Future(Ok("Killed: " + b))
        }
    }
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

      //      getListenerActor.flatMap {
      //        listener =>
      //          Future.successful(Right(webSocketActorFromActorFlow(listener)))
      //      }
      Future(Right(webSocketActorFromActorFlow))
    case rejected =>
      logger.error(s"Request ${rejected} failed same origin check")
      Future.successful {
        Left(Forbidden("forbidden"))
      }
  }

  def webSocketActorFromActorFlow = ActorFlow.actorRef { out =>
    ServerWebSocketActor.props(out)
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
