package models.server.actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger

object ServerWebSocketActor {
  def props(out: ActorRef) = Props(new ServerWebSocketActor(out))
}
class ServerWebSocketActor(out: ActorRef) extends Actor {
  val logger = Logger

  def receive = {
    case msg: String => logger.info("string: " + msg)
    case _ => logger.info("hi")
  }
}
