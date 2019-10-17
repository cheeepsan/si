package controllers.client

import java.net.{InetSocketAddress, URI}
import java.util.concurrent.TimeUnit.SECONDS

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import akka.util.Timeout
import javax.inject.Inject
import models.client.actors.{ClientActor, MyWebSocketActor}
import play.api.Logger
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.ClientServerInit

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future, duration}

class ClientController @Inject()(cc: ControllerComponents, csi: ClientServerInit)(
  implicit system: ActorSystem,
  mat: Materializer,
  implicit val executionContext: ExecutionContext)
  extends AbstractController(cc) {

  implicit val timeout = new Timeout(duration.FiniteDuration(500, SECONDS))

  private val logger = Logger

  def index(connString: String) = Action { implicit request: Request[AnyContent] =>
    val webSocketUrl = routes.ClientController.ws().webSocketURL()
    val conn = new InetSocketAddress(connString.trim, 9090)
    logger.info("IN CLIENT INDEX")

    Await.ready(createClientActor(conn), Duration.Inf)
    Ok(views.html.client.index(webSocketUrl))
  }

  def createClientActor(conn: InetSocketAddress) = csi.client.actorSelection("/user/clientActor").resolveOne().recoverWith {
    case e: Exception =>
      logger.info("not found ClientActor, new")
      Future.successful(csi.client.actorOf(ClientActor.props(conn), "clientActor"))
  }


  def getListenerActor = csi.client.actorSelection("/user/listener").resolveOne()

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

      getListenerActor.flatMap {
        listener =>
          Future.successful(Right(webSocketActorFromActorFlow(listener)))
      }

    case rejected =>
      logger.error(s"Request ${rejected} failed same origin check")
      Future.successful {
        Left(Forbidden("forbidden"))
      }
  }

  def webSocketActorFromActorFlow(clientListener: ActorRef) = ActorFlow.actorRef { out =>
    MyWebSocketActor.props(out, clientListener)
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
