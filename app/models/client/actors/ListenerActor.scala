package models.client.actors

import java.io.File

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import models.common.si.{SiMessage, SiText, SiUser}
import play.api.libs.json.Json

object ListenerActor {
  def props(clientActor: ActorRef): Props = {
    Props.create(classOf[ListenerActor], clientActor)
  }

}

class ListenerActor(clientActor: ActorRef) extends Actor {

  val logger = Logging(context.system, self)
  var webSocketActor: Option[ActorRef] = None

  def receive: PartialFunction[Any, Unit] = {
    case msg: String =>
      Json.parse(msg).validate[SiMessage].asOpt match {
        case Some(message) => receiveJsonMessage(message)
        case None => receiveTextMessage(msg)
      }
    case actor: ActorRef =>
      webSocketActor = Some(actor) //has to be webSocket
      webSocketActor.foreach(_ ! true)
    case _ =>
      logger.info(":::::::::::::::::::::nothing")
  }
/*
      msg match {

        case "start" =>
          clientActor ! msg
        case _ =>
          webSocketActor.foreach(_ ! msg)
      }
 */


  def receiveTextMessage(msg: String) = msg match {
    case "registered" => logger.info("Registered in listener")
    case _ => webSocketActor.foreach(_ ! msg)
  }

  def receiveJsonMessage(message: SiMessage): Unit = message.dataObjectType match {
    case "SiText" =>
      receiveSiText(message)
    case _ => logger.info("nothing")
  }


  def receiveSiText(textMessage: SiMessage): Unit = textMessage.message match {
    case "start" =>
      clientActor ! "start"
    case "register" =>
      logger.info("MyWebSocketActor::::::registering")
      clientActor ! textMessage.toJsonAndStringify
    case _ => logger.info("Empty in listener actor")
  }
}
