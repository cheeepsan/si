package controllers.server

import java.net.InetSocketAddress

import akka.pattern.gracefulStop
import akka.util.Timeout
import javax.inject.Inject
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import services.ClientServerInit
import java.util.concurrent.TimeUnit.SECONDS
import scala.concurrent.duration._
import models.server.actors.ServerActor
import play.api.Logger

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future, duration}
import scala.util.{Failure, Success}


class ServerController @Inject()(cc: ControllerComponents, csi: ClientServerInit)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  private val logger = Logger
  val conn = new InetSocketAddress("127.0.0.1", 9090)
  implicit val timeout = new Timeout(duration.FiniteDuration(500, SECONDS))

  def init = Action { implicit request: Request[AnyContent] =>
    Await.ready(resolveActor("serverActor"), Duration.Inf)
    logger.info("inited")
    Ok("created")
  }

  def resolveActor(name: String) = csi.server.actorSelection("/user/" + name).resolveOne().recoverWith {
    case e: Exception =>
      logger.info("not found ServerActor, new")
      Future.successful(csi.server.actorOf(ServerActor.props(conn), name))
  }

  def killServerActor =  Action.async { implicit request: Request[AnyContent] =>
    csi.server.actorSelection("/user/serverActor").resolveOne().flatMap {
      a =>
        gracefulStop(a, 1 minute).flatMap {
          b =>
            Future(Ok("Killed: " + b))
        }
    }
  }
}
