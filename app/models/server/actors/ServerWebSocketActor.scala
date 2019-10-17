package models.server.actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger

//out = browser
object ServerWebSocketActor {
  def props(out: ActorRef, serverActor: ActorRef) = Props(new ServerWebSocketActor(out, serverActor))
}
class ServerWebSocketActor(out: ActorRef, serverActor: ActorRef) extends Actor {
  val logger = Logger
  serverActor ! self

  def receive = {
    case actorRef: ActorRef =>
      logger.info("LISTENER FOR HANDLER: " + actorRef.path)
    case msg: String => logger.info("string: " + msg)
    case _ => logger.info("hi")
  }
}
