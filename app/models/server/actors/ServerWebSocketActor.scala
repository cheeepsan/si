package models.server.actors

import akka.actor.{Actor, ActorRef, Props}
import models.common.si.SiMessage
import play.api.Logger
import play.api.libs.json.Json

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
    case msg: String =>
      Json.parse(msg).validate[SiMessage].asOpt match {
        case Some(message) => receiveJsonMessage(message)
        case None => receiveTextMessage(msg)
      }
    case _ => logger.info("hi")
  }

  def receiveTextMessage(msg: String) = msg match {
    case "registered" => logger.info("Registered in listener")
    case _ => logger.info("nothing")
  }

  def receiveJsonMessage(message: SiMessage): Unit = message.dataObjectType match {
    case "SiText" =>
      receiveSiText(message)
    case _ => logger.info("nothing")
  }

  def receiveSiText(textMessage: SiMessage): Unit = textMessage.message match {
    case "start" =>
      logger.info("start message")
    case "register" =>
      logger.info("ServerWebSocketActor:::::registering")
      out ! textMessage.toJsonAndStringify
    case "selectQuestion" =>
    case _ =>
  }
}
