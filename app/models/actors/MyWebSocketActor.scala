package models.actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger

object MyWebSocketActor {
  def props(out: ActorRef) = Props(new MyWebSocketActor(out))
}

class MyWebSocketActor(out: ActorRef) extends Actor {
  val logger = Logger
  def receive = {
    case msg: String =>
      logger.info("msg " + msg)
      out ! ("I received your message: " + msg)
  }

  override def postStop(): Unit = {
    logger.info("ACTOR STOPPED")
  }
}