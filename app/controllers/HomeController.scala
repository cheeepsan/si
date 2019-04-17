package controllers


import java.net.InetSocketAddress

import javax.inject._
import play.api.mvc._
import akka.actor.{ActorRef, ActorSystem}
import models.{ClientActor, ServerActor}
import services.ClientServerInit

import scala.concurrent.Await
import scala.concurrent.duration.Duration
/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */

class HomeController @Inject()(cs: ClientServerInit,
                               cc: ControllerComponents) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
//    cs.init
    Ok("")
  }

  def server = Action { implicit request: Request[AnyContent] =>
    cs.server
    Ok("server started")
  }
  def client = Action { implicit request: Request[AnyContent] =>
    cs.client
    Ok("client started")
  }
  def term = Action { implicit request: Request[AnyContent] =>
    cs.term
    Ok("terminated")
  }
}
